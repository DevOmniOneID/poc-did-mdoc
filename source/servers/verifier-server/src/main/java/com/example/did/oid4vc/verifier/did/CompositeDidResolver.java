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

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Dispatches a kid to the first {@link DidResolver} that supports its DID method.
 *
 * <p>This PoC registers {@code DidOmnResolver} only (see {@code DidResolverConfig}). Adding did:key /
 * did:web later is a matter of registering more resolvers — no changes here.
 */
@Slf4j
public class CompositeDidResolver implements DidResolver {

  private final List<DidResolver> resolvers;

  public CompositeDidResolver(List<DidResolver> resolvers) {
    this.resolvers = resolvers;
  }

  @Override
  public boolean supports(String kid) {
    return resolvers.stream().anyMatch(r -> r.supports(kid));
  }

  @Override
  public Optional<DidResolution> resolve(String kid) {
    for (DidResolver r : resolvers) {
      if (r.supports(kid)) {
        return r.resolve(kid);
      }
    }
    log.warn("No DID resolver supports kid={}", kid);
    return Optional.empty();
  }
}
