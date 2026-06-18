/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

/**
 * Result of resolving a credential {@code kid} (DID URL) to an issuer signing key.
 * publicKeyBase64 is an EC point (compressed or uncompressed) in base64-std.
 */
public final class DidResolution {
    private final String did;
    private final String fragment;
    private final String publicKeyBase64;
    private final String algorithm;

    public DidResolution(String did, String fragment, String publicKeyBase64, String algorithm) {
        this.did = did;
        this.fragment = fragment;
        this.publicKeyBase64 = publicKeyBase64;
        this.algorithm = algorithm;
    }

    public String getDid() { return did; }
    public String getFragment() { return fragment; }
    public String getPublicKeyBase64() { return publicKeyBase64; }
    public String getAlgorithm() { return algorithm; }
}
