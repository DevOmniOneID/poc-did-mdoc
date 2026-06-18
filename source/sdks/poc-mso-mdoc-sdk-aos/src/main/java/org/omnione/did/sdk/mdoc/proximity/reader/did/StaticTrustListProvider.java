/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Fixed in-memory trust list. */
public final class StaticTrustListProvider implements TrustListProvider {
    private final Set<String> dids;

    public StaticTrustListProvider(Set<String> dids) {
        this.dids = dids == null ? Collections.emptySet()
            : Collections.unmodifiableSet(new HashSet<>(dids));
    }

    @Override
    public Set<String> trustedIssuerDids() {
        return dids;
    }
}
