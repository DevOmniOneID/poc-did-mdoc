/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.mdoc.reader.trust;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.omnione.did.sdk.mdoc.proximity.reader.did.OkHttpFetcher;

/**
 * Fetches a DID Document from the API-gateway. Response is {"didDoc":"m<base64-no-pad of JSON>"};
 * strips the 'm' multibase prefix and base64-decodes to raw DID Document JSON.
 */
public final class DidDocClient {
    private static final String TAG = "MDR/TrustRefresh";
    private final String gatewayBase;
    private final OkHttpFetcher fetcher = new OkHttpFetcher();
    private final ObjectMapper mapper = new ObjectMapper();

    public DidDocClient(String gatewayBase) {
        this.gatewayBase = stripTrailingSlash(gatewayBase);
    }

    /** @return raw DID Document JSON for the did, or null on failure. */
    public String fetchDidDoc(String did) {
        try {
            String url = gatewayBase + "/api-gateway/api/v1/did-doc?did="
                + URLEncoder.encode(did, StandardCharsets.UTF_8);
            String body = fetcher.fetch(url);
            JsonNode root = mapper.readTree(body);
            JsonNode didDoc = root.get("didDoc");
            if (didDoc == null || didDoc.isNull()) return null;
            String mb = didDoc.asText();
            if (mb.isEmpty() || mb.charAt(0) != 'm') return null; // multibase base64
            byte[] decoded = Base64.getDecoder().decode(pad(mb.substring(1)));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "fetchDidDoc failed did=" + did, e);
            return null;
        }
    }

    private static String pad(String b64) {
        int p = (4 - (b64.length() % 4)) % 4;
        return b64 + "===".substring(0, p);
    }

    private static String stripTrailingSlash(String s) {
        return s != null && s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
