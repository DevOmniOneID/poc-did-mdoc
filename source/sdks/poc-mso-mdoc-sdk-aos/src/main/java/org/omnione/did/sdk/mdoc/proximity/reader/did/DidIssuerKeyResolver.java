/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;

/**
 * Resolves the issuer signing key for a DID-native IssuerAuth — a COSE_Sign1 that references its key
 * by {@code kid} (DID URL, COSE unprotected header label 4) instead of an embedded X.509 chain.
 *
 * <p>This is the minimal, offline, single-issuer path used by the proximity (BLE/NFC) reader: no
 * network, no cache, no Android {@code Context}. The issuer DID Document is shipped inside the SDK
 * as a classpath resource. Pipeline:
 * <pre>
 *   coseSign1
 *     -&gt; {@link IssuerKidExtractor#extractKid}  (kid)
 *     -&gt; bundled DID Document JSON
 *     -&gt; {@link DidDocumentParser#parse}        (base64 EC point)
 *     -&gt; {@link EcPointDecoder#decode}          (ECPublicKey)
 * </pre>
 */
public final class DidIssuerKeyResolver {

    /** Trusted-issuer DID Document shipped with the SDK (classpath resource). */
    private static final String BUNDLED_DID_DOCUMENT = "/dids/did_omn_issuer.json";

    /** App-supplied trusted DID documents (cache). Null = bundled-only (first run). */
    private static volatile IssuerDidDocSupplier supplier;

    /** Registers the trusted-issuer cache; pass null to revert to bundled-only. */
    public static void setSupplier(IssuerDidDocSupplier s) {
        supplier = s;
    }

    private DidIssuerKeyResolver() {}

    /**
     * Resolves the issuer EC public key from a DID-native IssuerAuth COSE_Sign1.
     *
     * @param coseSign1 the IssuerAuth COSE_Sign1 array
     * @return the issuer public key, or empty if the COSE_Sign1 carries no kid, the bundled DID
     *         Document does not describe that kid, or the key cannot be decoded
     */
    public static Optional<ECPublicKey> resolve(CBORObject coseSign1) {
        String kid = IssuerKidExtractor.extractKid(coseSign1);
        if (kid == null) {
            return Optional.empty();
        }
        String baseDid = DidDocumentParser.baseDid(kid);
        // 1) app-supplied trusted-issuer cache (trusted_issuers.json)
        IssuerDidDocSupplier s = supplier;
        if (s != null) {
            String json = s.didDocJsonFor(baseDid);
            if (json != null) {
                Optional<ECPublicKey> key = new DidDocumentParser().parse(json, kid)
                    .map(r -> decode(r.getPublicKeyBase64()));
                if (key.isPresent()) {
                    return key;
                }
            }
        }
        // 2) bundled fallback (first run before any refresh)
        String bundled = loadBundledDidDocument();
        if (bundled == null) {
            return Optional.empty();
        }
        return new DidDocumentParser().parse(bundled, kid)
            .map(r -> decode(r.getPublicKeyBase64()));
    }

    private static ECPublicKey decode(String publicKeyBase64) {
        try {
            return EcPointDecoder.decode(publicKeyBase64);
        } catch (Exception e) {
            return null; // Optional.map turns a null result into Optional.empty()
        }
    }

    private static String loadBundledDidDocument() {
        try (InputStream in = DidIssuerKeyResolver.class.getResourceAsStream(BUNDLED_DID_DOCUMENT)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
