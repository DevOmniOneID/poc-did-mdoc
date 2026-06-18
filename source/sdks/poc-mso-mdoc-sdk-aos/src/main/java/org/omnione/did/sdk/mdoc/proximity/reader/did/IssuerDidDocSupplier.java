/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

/** Supplies a cached DID Document (raw JSON) for an issuer base DID, or null if not trusted/cached. */
public interface IssuerDidDocSupplier {
    /** @param baseDid e.g. "did:omn:issuer" (no versionId/fragment). @return DID Document JSON, or null. */
    String didDocJsonFor(String baseDid);
}
