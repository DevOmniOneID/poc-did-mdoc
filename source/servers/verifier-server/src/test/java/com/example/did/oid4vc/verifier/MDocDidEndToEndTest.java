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
package com.example.did.oid4vc.verifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.did.oid4vc.verifier.did.DidDocumentParser;
import com.example.did.oid4vc.verifier.did.DidResolution;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.dto.IdentifierResult;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.MDocVPVerifier;
import org.springframework.core.io.ClassPathResource;

/**
 * Integration test for the DID-native mso_mdoc verification seam owned by verifier-server:
 * VP token -> SDK classifies as {@code MSO_MDOC_KID} -> verifier-server resolves the issuer DID
 * to a public key -> SDK verifies the IssuerAuth signature + IssuerSignedItem digests.
 *
 * <p>This exercises the full chain minus the HTTP/session transport (which requires a wallet and a
 * live issuer DID endpoint — see Task 7 manual E2E in the migration plan). It uses the offline
 * fixtures: a captured kid-bearing mDoc and the matching {@code did:omn:issuer} DID Document.
 */
class MDocDidEndToEndTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String read(String path) throws Exception {
    return new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  private String credentialFrom(String vpTokenResource) throws Exception {
    return objectMapper.readTree(read(vpTokenResource)).get("query_0").get(0).asText();
  }

  @Test
  void didNativeMdocVerifiesEndToEnd() throws Exception {
    String credential = credentialFrom("fixtures/vp_token_kid_only.json");
    MDocVPVerifier verifier = new MDocVPVerifier();

    // 1. SDK classifies the mso_mdoc as DID-native and surfaces the issuer kid (DID URL).
    IdentifierResult issuer = verifier.extractIssuerIdentifier(credential);
    assertEquals(IdentifierResult.Type.MSO_MDOC_KID, issuer.getType());
    assertEquals("did:omn:issuer?versionId=1#assert", issuer.getValue());

    // 2. verifier-server resolves the DID to the issuer public key (offline DID Document fixture).
    DidResolution res = new DidDocumentParser()
        .parse(read("fixtures/did_omn_issuer.json"), issuer.getValue())
        .orElseThrow(() -> new AssertionError("DID resolution failed"));

    // 3. SDK verifies IssuerAuth COSE_Sign1 signature + IssuerSignedItem digests with the resolved key.
    assertTrue(verifier.validateSignature(credential, res.publicKeyBase64(), null),
        "DID-resolved key must verify the kid-bearing mDoc end-to-end");
  }

  @Test
  void verifiesViaUniversalResolverResult() throws Exception {
    String credential = credentialFrom("fixtures/vp_token_kid_only.json");
    MDocVPVerifier verifier = new MDocVPVerifier();
    IdentifierResult issuer = verifier.extractIssuerIdentifier(credential);
    // verifier-server resolves the issuer key from a Universal-Resolver-style result (publicKeyMultibase).
    DidResolution res = new DidDocumentParser()
        .parse(read("fixtures/resolution_did_omn_issuer.json"), issuer.getValue())
        .orElseThrow(() -> new AssertionError("DID resolution failed"));
    assertTrue(verifier.validateSignature(credential, res.publicKeyBase64(), null),
        "multibase-resolved key (resolver path) must verify the kid-bearing mDoc");
  }

  @Test
  void hybridMdocAlsoClassifiedAsKid() throws Exception {
    String credential = credentialFrom("fixtures/vp_token_kid_hybrid.json");
    IdentifierResult issuer = new MDocVPVerifier().extractIssuerIdentifier(credential);
    assertEquals(IdentifierResult.Type.MSO_MDOC_KID, issuer.getType(),
        "a credential carrying both kid and x5chain must take the kid (DID) path");
  }

  /**
   * Regression guard: making {@code extractIssuerIdentifier} kid-first must NOT change the
   * classification of a legacy x5chain-only mDoc (no kid). It must still take the certificate-chain
   * (PKIX) path, otherwise the existing x5c verification flow silently breaks.
   */
  @Test
  void x5chainOnlyMdocClassifiedAsX5c() throws Exception {
    String credential = credentialFrom("fixtures/vp_token_x5chain_only.json");
    IdentifierResult issuer = new MDocVPVerifier().extractIssuerIdentifier(credential);
    assertEquals(IdentifierResult.Type.MSO_MDOC_X5C, issuer.getType(),
        "an mDoc with x5chain and no kid must take the x5c (certificate chain) path");
  }
}
