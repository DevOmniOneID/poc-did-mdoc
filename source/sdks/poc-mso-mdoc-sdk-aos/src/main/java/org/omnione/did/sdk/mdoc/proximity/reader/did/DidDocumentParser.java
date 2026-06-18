/*
 * Copyright 2026 OmniOne. Apache-2.0. Ported from verifier-server DidDocumentParser.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Optional;

/**
 * Parses a W3C DID Document (raw, or Universal-Resolver-wrapped) and extracts the issuer signing
 * key for a given kid. Matches verificationMethod[].id == kid, reads publicKeyMultibase
 * (z + base58btc of a compressed EC point) or publicKeyJwk (EC P-256 x/y). Output is a base64-std
 * EC point that EcPointDecoder turns into an ECPublicKey.
 */
public final class DidDocumentParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<DidResolution> parse(String didDocumentJson, String kid) {
        try {
            JsonNode root = objectMapper.readTree(didDocumentJson);
            JsonNode doc = root.has("didDocument") && !root.get("didDocument").isNull()
                ? root.get("didDocument") : root;
            JsonNode methods = doc.get("verificationMethod");
            if (methods == null || !methods.isArray()) return Optional.empty();
            for (JsonNode vm : methods) {
                String id = text(vm, "id");
                if (id == null || !idMatches(id, kid)) continue;
                Optional<String> pub = extractPublicKeyBase64(vm);
                if (pub.isPresent()) {
                    return Optional.of(new DidResolution(baseDid(kid), fragment(kid), pub.get(), "Secp256r1"));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean idMatches(String vmId, String kid) {
        if (vmId.equals(kid)) return true;
        String kidFrag = fragment(kid);
        if (kidFrag == null) return false;
        String vmFrag = fragment(vmId);
        // full-URL id("...#assert")는 fragment끼리, bare-fragment id("assert")는 직접 비교
        return vmFrag != null ? vmFrag.equals(kidFrag) : vmId.equals(kidFrag);
    }

    private Optional<String> extractPublicKeyBase64(JsonNode vm) {
        String multibase = text(vm, "publicKeyMultibase");
        if (multibase != null && multibase.length() > 1 && multibase.charAt(0) == 'z') {
            try {
                byte[] point = Base58.decode(multibase.substring(1)); // compressed EC point (33B)
                if (point.length > 0) return Optional.of(Base64.getEncoder().encodeToString(point));
            } catch (Exception ignored) { /* fall through to JWK */ }
        }
        JsonNode jwk = vm.get("publicKeyJwk");
        if (jwk != null) {
            String x = text(jwk, "x");
            String y = text(jwk, "y");
            if (x != null && y != null) {
                byte[] xb = Base64.getUrlDecoder().decode(pad(x));
                byte[] yb = Base64.getUrlDecoder().decode(pad(y));
                byte[] point = new byte[1 + xb.length + yb.length];
                point[0] = 0x04; // uncompressed
                System.arraycopy(xb, 0, point, 1, xb.length);
                System.arraycopy(yb, 0, point, 1 + xb.length, yb.length);
                return Optional.of(Base64.getEncoder().encodeToString(point));
            }
        }
        return Optional.empty();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    static String baseDid(String kid) {
        int q = kid.indexOf('?');
        int h = kid.indexOf('#');
        int end = kid.length();
        if (q >= 0) end = Math.min(end, q);
        if (h >= 0) end = Math.min(end, h);
        return kid.substring(0, end);
    }

    private static String fragment(String didUrl) {
        int h = didUrl.indexOf('#');
        return h >= 0 ? didUrl.substring(h + 1) : null;
    }

    private static String pad(String b64url) {
        int pad = (4 - (b64url.length() % 4)) % 4;
        // "=".repeat(int) is API 33+; substring of a 3-'=' literal is minSdk-26 safe (pad is 0..3).
        return b64url + "===".substring(0, pad);
    }
}
