/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import com.upokecenter.cbor.CBORObject;
import java.util.Set;

/**
 * DID-native trust decision: trusts iff (1) DID path with a valid signature (no x5chain) —
 * {@link DidNativeTrust} — AND (2) the issuer base DID is in the trust list.
 */
public final class DidTrustEvaluator {

    private DidTrustEvaluator() {}

    public static boolean isTrusted(CBORObject coseSign1, Boolean issuerSignatureValid,
                                    Set<String> trustedIssuerDids) {
        if (!DidNativeTrust.isVerified(coseSign1, issuerSignatureValid)) return false;
        String kid = IssuerKidExtractor.extractKid(coseSign1);
        if (kid == null) return false;
        return trustedIssuerDids != null
            && trustedIssuerDids.contains(DidDocumentParser.baseDid(kid));
    }
}
