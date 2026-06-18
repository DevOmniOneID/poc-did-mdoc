/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.did.oid4vc.verifier.did;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.crypto.util.MultiBaseUtils;

/**
 * Parses a W3C DID Document (raw, or wrapped in a Universal Resolver resolution result) and
 * extracts the issuer signing key for a given kid.
 *
 * <p>Matches {@code verificationMethod[].id == kid} and reads {@code publicKeyMultibase}
 * (OpenDID: {@code z} + base58btc of a compressed EC point) or {@code publicKeyJwk} (EC P-256 x/y).
 * Output is a base64-std EC point (compressed or uncompressed) which the controller passes to the
 * SDK ({@code MDocVerifier.verifyWithIssuerPublicKey}) to verify the IssuerAuth COSE_Sign1 signature.
 */
@Slf4j
public class DidDocumentParser {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Extracts the issuer public key for {@code kid} from a DID Document JSON.
   *
   * @param didDocumentJson the DID Document as JSON text
   * @param kid the verification method id to match (e.g. {@code did:omn:issuer?versionId=1#assert})
   * @return resolution holding the base64 public key, or empty if not found
   */
  public Optional<DidResolution> parse(String didDocumentJson, String kid) {
    try {
      JsonNode root = objectMapper.readTree(didDocumentJson);
      // Universal Resolver returns { didDocument, didResolutionMetadata, ... }; accept raw docs too.
      JsonNode doc = root.has("didDocument") && !root.get("didDocument").isNull()
          ? root.get("didDocument") : root;
      JsonNode methods = doc.get("verificationMethod");
      if (methods == null || !methods.isArray()) {
        log.warn("DID Document has no verificationMethod array");
        return Optional.empty();
      }
      for (JsonNode vm : methods) {
        String id = text(vm, "id");
        if (id == null || !idMatches(id, kid)) {
          continue;
        }
        Optional<String> pub = extractPublicKeyBase64(vm);
        if (pub.isPresent()) {
          return Optional.of(new DidResolution(baseDid(kid), fragment(kid), pub.get(), "Secp256r1"));
        }
      }
      log.warn("No matching verificationMethod for kid={}", kid);
      return Optional.empty();
    } catch (Exception e) {
      log.warn("Failed to parse DID Document for kid={}: {}", kid, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Matches a verificationMethod id against the kid. The id may be the full kid
   * ({@code did:omn:issuer?versionId=1#assert}), a relative DID URL ({@code #assert}), or — as LSS
   * returns — a bare fragment ({@code assert}). All compare on the fragment.
   */
  private boolean idMatches(String vmId, String kid) {
    if (vmId.equals(kid)) {
      return true;
    }
    String kidFragment = fragment(kid);
    if (kidFragment == null) {
      return false;
    }
    // vmId may carry its own '#fragment'; a bare id (no '#') is itself the fragment (LSS style).
    String vmFragment = fragment(vmId);
    if (vmFragment == null) {
      vmFragment = vmId;
    }
    return kidFragment.equals(vmFragment);
  }

  private Optional<String> extractPublicKeyBase64(JsonNode vm) {
    // OpenDID DID Documents use publicKeyMultibase (z + base58btc of a compressed EC point).
    String multibase = text(vm, "publicKeyMultibase");
    if (multibase != null && !multibase.isBlank()) {
      try {
        byte[] point = MultiBaseUtils.decode(multibase); // compressed EC point (P-256: 33 bytes)
        if (point != null && point.length > 0) {
          return Optional.of(Base64.getEncoder().encodeToString(point));
        }
      } catch (Exception e) {
        log.warn("Failed to decode publicKeyMultibase: {}", e.getMessage());
      }
    }
    // Fallback: publicKeyJwk (EC P-256 x/y) — used by local mock/test fixtures.
    JsonNode jwk = vm.get("publicKeyJwk");
    if (jwk != null) {
      String x = text(jwk, "x");
      String y = text(jwk, "y");
      if (x != null && y != null) {
        byte[] xb = Base64.getUrlDecoder().decode(pad(x));
        byte[] yb = Base64.getUrlDecoder().decode(pad(y));
        byte[] point = new byte[1 + xb.length + yb.length];
        point[0] = 0x04; // uncompressed
        System.arraycopy(xb, 0, point, 1, xb.length);
        System.arraycopy(yb, 0, point, 1 + xb.length, yb.length);
        return Optional.of(Base64.getEncoder().encodeToString(point));
      }
    }
    return Optional.empty();
  }

  private static String text(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v == null || v.isNull() ? null : v.asText();
  }

  private static String baseDid(String kid) {
    int q = kid.indexOf('?');
    int h = kid.indexOf('#');
    int end = kid.length();
    if (q >= 0) end = Math.min(end, q);
    if (h >= 0) end = Math.min(end, h);
    return kid.substring(0, end);
  }

  private static String fragment(String didUrl) {
    int h = didUrl.indexOf('#');
    return h >= 0 ? didUrl.substring(h + 1) : null;
  }

  private static String pad(String b64url) {
    int pad = (4 - (b64url.length() % 4)) % 4;
    return b64url + "=".repeat(pad);
  }
}
