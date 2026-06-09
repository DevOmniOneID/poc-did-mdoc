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

import java.util.Optional;

/**
 * Resolves a credential {@code kid} (DID URL) to an issuer signing key.
 *
 * <p>Implementations are method-specific (did:omn, did:web, did:key …). A {@code CompositeDidResolver}
 * dispatches by DID method prefix. This PoC activates {@code did:omn} only.
 */
public interface DidResolver {

  /** True if this resolver handles the given kid's DID method. */
  boolean supports(String kid);

  /** Resolves the kid to an issuer public key, or empty if not resolvable. */
  Optional<DidResolution> resolve(String kid);
}
