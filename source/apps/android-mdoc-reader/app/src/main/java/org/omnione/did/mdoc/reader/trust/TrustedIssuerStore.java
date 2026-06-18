/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.mdoc.reader.trust;

import android.content.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.omnione.did.sdk.mdoc.proximity.reader.did.IssuerDidDocSupplier;
import org.omnione.did.sdk.mdoc.proximity.reader.did.TrustListProvider;

/**
 * Disk-backed trusted-issuer cache. {@code trusted_issuers.json} is a JSON array of raw DID Document
 * JSON strings; loaded into a baseDid→didDocJson map. Implements both SDK injection interfaces.
 */
public final class TrustedIssuerStore implements IssuerDidDocSupplier, TrustListProvider {
    private static final String FILE = "trusted_issuers.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file;
    private volatile Map<String, String> byBaseDid = new HashMap<>();

    public TrustedIssuerStore(Context context) {
        this.file = new File(context.getApplicationContext().getFilesDir(), FILE);
        load();
    }

    /** Reloads the map from disk (no-op if file absent). */
    public synchronized void load() {
        Map<String, String> map = new HashMap<>();
        try {
            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                JsonNode arr = mapper.readTree(bytes);
                if (arr.isArray()) {
                    for (JsonNode node : arr) {
                        String didDocJson = node.asText();
                        String baseDid = idOf(didDocJson);
                        if (baseDid != null) map.put(baseDid, didDocJson);
                    }
                }
            }
        } catch (Exception ignored) {
            return; // keep last good map
        }
        this.byBaseDid = map;
    }

    /** Overwrites the cache with the given DID Document JSON strings, then reloads. */
    public synchronized void save(List<String> didDocJsons) throws Exception {
        File tmp = new File(file.getParent(), FILE + ".tmp");
        mapper.writeValue(tmp, didDocJsons);
        tmp.renameTo(file);
        load();
    }

    /** DID Document "id" field = base DID (e.g. "did:omn:issuer"). */
    private String idOf(String didDocJson) {
        try {
            JsonNode root = mapper.readTree(didDocJson);
            JsonNode id = root.get("id");
            return id == null ? null : id.asText();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String didDocJsonFor(String baseDid) {
        return byBaseDid.get(baseDid);
    }

    @Override
    public Set<String> trustedIssuerDids() {
        return byBaseDid.keySet();
    }
}
