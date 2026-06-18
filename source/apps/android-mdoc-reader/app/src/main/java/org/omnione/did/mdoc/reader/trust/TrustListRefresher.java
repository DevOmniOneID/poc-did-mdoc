/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.mdoc.reader.trust;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Refreshes the trusted-issuer cache: mock server → did list → API-gateway → each did-doc →
 * {@link TrustedIssuerStore#save}. Call off the main thread.
 */
public final class TrustListRefresher {
    private static final String TAG = "MDR/TrustRefresh";

    private final IssuerListClient listClient;
    private final DidDocClient docClient;
    private final TrustedIssuerStore store;

    public TrustListRefresher(IssuerListClient listClient, DidDocClient docClient,
                              TrustedIssuerStore store) {
        this.listClient = listClient;
        this.docClient = docClient;
        this.store = store;
    }

    /** @return number of did-docs cached (0 on full failure). */
    public int refresh() {
        List<String> dids = listClient.fetchDids();
        if (dids.isEmpty()) {
            Log.w(TAG, "no trusted dids from mock server; keeping existing cache");
            return 0;
        }
        List<String> didDocs = new ArrayList<>();
        for (String did : dids) {
            String json = docClient.fetchDidDoc(did);
            if (json != null) {
                didDocs.add(json);
            } else {
                Log.w(TAG, "did-doc fetch failed for " + did + "; keeping existing cache to avoid issuer loss");
                return 0;
            }
        }
        if (didDocs.isEmpty()) {
            Log.w(TAG, "no did-docs fetched; keeping existing cache");
            return 0;
        }
        try {
            store.save(didDocs);
            Log.i(TAG, "trusted-issuer cache refreshed: " + didDocs.size() + " docs");
            return didDocs.size();
        } catch (Exception e) {
            Log.w(TAG, "store save failed", e);
            return 0;
        }
    }
}
