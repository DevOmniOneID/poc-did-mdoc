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
package com.example.did.oid4vc.verifier.config;

import com.example.did.oid4vc.verifier.did.CompositeDidResolver;
import com.example.did.oid4vc.verifier.did.DidDocumentCache;
import com.example.did.oid4vc.verifier.did.DidDocumentParser;
import com.example.did.oid4vc.verifier.did.DidOmnResolver;
import com.example.did.oid4vc.verifier.did.DidResolver;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the DID resolution stack for DID-native mDoc verification.
 *
 * <p>This PoC enables {@code did:omn} only. To experiment with did:key / did:web, register their
 * resolvers here and add the method to {@code did.resolver.enabled-methods}.
 */
@Slf4j
@Configuration
public class DidResolverConfig {

  @Value("${did.resolver.endpoint:http://localhost:8098}")
  private String resolverEndpoint;

  @Value("${did.resolver.cache-ttl-millis:86400000}")
  private long cacheTtlMillis;

  @Value("${did.resolver.enabled-methods:did:omn}")
  private List<String> enabledMethods;

  @Bean
  public CompositeDidResolver compositeDidResolver() {
    DidDocumentCache cache = new DidDocumentCache(cacheTtlMillis, 64);
    DidDocumentParser parser = new DidDocumentParser();
    List<DidResolver> resolvers = new ArrayList<>();

    if (enabledMethods.contains("did:omn")) {
      resolvers.add(new DidOmnResolver(resolverEndpoint, cache, parser));
      log.info("DID resolver enabled: did:omn (resolver endpoint: {})", resolverEndpoint);
    }
    // did:key / did:web resolvers can be added here when needed.

    return new CompositeDidResolver(resolvers);
  }
}
