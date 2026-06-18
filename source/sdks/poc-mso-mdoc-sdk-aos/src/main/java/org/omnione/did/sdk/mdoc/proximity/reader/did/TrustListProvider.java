/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import java.util.Set;

/** Supplies the set of trusted issuer base DIDs (e.g. "did:omn:issuer"; no versionId/fragment). */
public interface TrustListProvider {
    /** Trusted issuer base DIDs. Never null. */
    Set<String> trustedIssuerDids();
}
