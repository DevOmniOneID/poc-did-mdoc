/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

/** Abstraction over HTTP GET so resolvers are unit-testable without a network. */
public interface HttpFetcher {
    /** Returns the response body for a GET on the given url, or throws on failure. */
    String fetch(String url) throws Exception;
}
