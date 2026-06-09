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

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves {@code did:omn:*} kids directly against the LSS DID Document endpoint
 * ({@code GET /lss/api/v1/did-doc?did={url-encoded-did}}) and caches the result.
 *
 * <p>The endpoint base URL is configurable via {@code did.resolver.endpoint} (the LSS host). Unlike a
 * Universal Resolver gateway, LSS returns the raw W3C DID Document (no {@code didDocument} wrapper).
 * The fetched document is parsed by {@link DidDocumentParser}, which matches the verification method by
 * fragment and decodes {@code publicKeyMultibase}.
 *
 * <p>Trust model: LSS returns ledger-anchored DID Documents. An explicit issuer-DID allowlist
 * (trust anchor) is still future work.
 */
@Slf4j
public class DidOmnResolver implements DidResolver {

  private static final String PREFIX = "did:omn:";

  private final String resolverEndpoint;
  private final DidDocumentCache cache;
  private final DidDocumentParser parser;
  private final HttpClient httpClient;

  public DidOmnResolver(String resolverEndpoint, DidDocumentCache cache, DidDocumentParser parser) {
    this.resolverEndpoint = stripTrailingSlash(resolverEndpoint);
    this.cache = cache;
    this.parser = parser;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  }

  @Override
  public boolean supports(String kid) {
    return kid != null && kid.startsWith(PREFIX);
  }

  @Override
  public Optional<DidResolution> resolve(String kid) {
    if (!supports(kid)) {
      return Optional.empty();
    }
    try {
      String did = baseDid(kid);
      String json = cache.get(did).orElseGet(() -> fetchAndCache(did));
      if (json == null) {
        return Optional.empty();
      }
      return parser.parse(json, kid);
    } catch (Exception e) {
      log.warn("did:omn resolution failed for {}: {}", kid, e.getMessage());
      return Optional.empty();
    }
  }

  private String fetchAndCache(String did) {
    try {
      URI uri = URI.create(resolverEndpoint + "/lss/api/v1/did-doc?did="
          + URLEncoder.encode(did, StandardCharsets.UTF_8));
      log.info("Resolving DID Document: {}", uri);
      HttpRequest req = HttpRequest.newBuilder(uri)
          .timeout(Duration.ofSeconds(5))
          .header("Accept", "application/json")
          .GET()
          .build();
      HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        log.warn("DID resolution returned HTTP {}", resp.statusCode());
        return null;
      }
      cache.put(did, resp.body());
      return resp.body();
    } catch (Exception e) {
      log.warn("DID Document fetch error for {}: {}", did, e.getMessage());
      return null;
    }
  }

  private static String baseDid(String kid) {
    int q = kid.indexOf('?');
    int h = kid.indexOf('#');
    int end = kid.length();
    if (q >= 0) end = Math.min(end, q);
    if (h >= 0) end = Math.min(end, h);
    return kid.substring(0, end);
  }

  private static String stripTrailingSlash(String s) {
    return s != null && s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }
}
