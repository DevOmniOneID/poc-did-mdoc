/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** OkHttp-backed HttpFetcher (Android has no java.net.http.HttpClient on minSdk 26). */
public final class OkHttpFetcher implements HttpFetcher {
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build();

    @Override
    public String fetch(String url) throws Exception {
        Request req = new Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .get()
            .build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new IllegalStateException("HTTP " + resp.code());
            }
            return resp.body().string();
        }
    }
}
