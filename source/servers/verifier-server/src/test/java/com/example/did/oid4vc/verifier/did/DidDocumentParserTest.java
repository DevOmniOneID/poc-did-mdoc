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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Verifies the verifier-server's remaining responsibility for DID-native mDoc: resolving the issuer
 * public key from a DID Document. (COSE/digest verification now lives in the mdoc-core SDK.)
 */
class DidDocumentParserTest {

  private static final String KID = "did:omn:issuer?versionId=1#assert";

  private String read(String p) throws Exception {
    return new String(new ClassPathResource(p).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  @Test
  void parsesIssuerPublicKeyFromDidDocument() throws Exception {
    Optional<DidResolution> res = new DidDocumentParser().parse(read("fixtures/did_omn_issuer.json"), KID);
    assertTrue(res.isPresent(), "verificationMethod should match the kid");
    assertEquals("assert", res.get().fragment());
    assertFalse(res.get().publicKeyBase64().isBlank());
  }

  @Test
  void parsesMultibaseKeyFromUniversalResolverResult() throws Exception {
    // OpenDID resolver returns { didDocument, ... } with publicKeyMultibase (z + base58btc).
    Optional<DidResolution> res = new DidDocumentParser()
        .parse(read("fixtures/resolution_did_omn_issuer.json"), KID);
    assertTrue(res.isPresent(), "should unwrap didDocument and decode publicKeyMultibase");
    assertEquals("assert", res.get().fragment());
    // multibase must decode to exactly the known compressed EC point.
    assertEquals("Ay/5wNs8D1oX+FDRYgnJUmZ/Ovnff+/73G8LD53+m1tk", res.get().publicKeyBase64());
  }
}
