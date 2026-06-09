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

/**
 * Result of resolving a credential {@code kid} (a DID URL) to an issuer signing key.
 *
 * @param did the base DID (e.g. {@code did:omn:issuer})
 * @param fragment the verification method fragment (e.g. {@code assert})
 * @param publicKeyBase64 the EC public key as base64 (uncompressed point, 04||X||Y)
 * @param algorithm the key algorithm (e.g. {@code Secp256r1})
 */
public record DidResolution(String did, String fragment, String publicKeyBase64, String algorithm) {
}
