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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory TTL cache of DID Document JSON keyed by DID.
 *
 * <p>The verifier is a long-lived server, so an LRU + TTL in memory is sufficient (no disk cache).
 * On expiry the entry is dropped; the resolver re-fetches. This mirrors the Reader's pro-active
 * caching strategy in spirit but without the offline fallback (the verifier is always online).
 */
@Slf4j
public class DidDocumentCache {

  private record Entry(String json, long expiresAt) {
  }

  private final long ttlMillis;
  private final Map<String, Entry> cache;

  public DidDocumentCache(long ttlMillis, int maxEntries) {
    this.ttlMillis = ttlMillis;
    this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
        return size() > maxEntries;
      }
    });
  }

  public Optional<String> get(String did) {
    Entry e = cache.get(did);
    if (e == null) {
      return Optional.empty();
    }
    if (System.currentTimeMillis() > e.expiresAt()) {
      cache.remove(did);
      log.debug("DID Document cache expired for {}", did);
      return Optional.empty();
    }
    return Optional.of(e.json());
  }

  public void put(String did, String json) {
    cache.put(did, new Entry(json, System.currentTimeMillis() + ttlMillis));
  }
}
