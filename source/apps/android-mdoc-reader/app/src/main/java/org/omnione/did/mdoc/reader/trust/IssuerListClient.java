/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.mdoc.reader.trust;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.omnione.did.sdk.mdoc.proximity.reader.did.OkHttpFetcher;

/** Fetches the trusted issuer DID list from the mock server: {"dids":["did:omn:issuer", …]}. */
public final class IssuerListClient {
    private static final String TAG = "MDR/TrustRefresh";
    private final String url;
    private final OkHttpFetcher fetcher = new OkHttpFetcher();
    private final ObjectMapper mapper = new ObjectMapper();

    public IssuerListClient(String url) {
        this.url = url;
    }

    /** @return trusted issuer DIDs, or empty list on failure. */
    public List<String> fetchDids() {
        List<String> dids = new ArrayList<>();
        try {
            String body = fetcher.fetch(url);
            JsonNode arr = mapper.readTree(body).get("dids");
            if (arr != null && arr.isArray()) {
                for (JsonNode n : arr) dids.add(n.asText());
            }
        } catch (Exception e) { Log.e(TAG, "fetchDids failed url=" + url, e); }
        return dids;
    }
}
