package org.omnione.did.sdk.mdoc.proximity.reader.did;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

/**
 * Pure-Java predicate: returns true when a COSE_Sign1 carries no x5chain (COSE label 33)
 * and the IssuerAuth signature was already verified via a DID-resolved key.
 *
 * Extracted from TrustManager to stay Android-free and JVM-testable.
 */
public final class DidNativeTrust {

    private DidNativeTrust() {}

    /**
     * Returns true when the document should be trusted via the DID-native path:
     * the IssuerAuth signature was verified (via kid + bundled DID document) and
     * no x5chain is present that would require PKIX validation instead.
     *
     * @param coseSign1            the IssuerAuth COSE_Sign1 array (4 elements)
     * @param issuerSignatureValid result of the crypto signature check in DeviceResponseParser
     */
    public static boolean isVerified(CBORObject coseSign1, Boolean issuerSignatureValid) {
        if (!Boolean.TRUE.equals(issuerSignatureValid)) return false;
        if (coseSign1 == null || coseSign1.size() < 2) return false;
        CBORObject unprotected = coseSign1.get(1);
        if (unprotected == null || unprotected.getType() != CBORType.Map) return true;
        // x5chain present (label 33 or string key) → defer to X.509 path
        return unprotected.get(CBORObject.FromObject(33)) == null
            && unprotected.get(CBORObject.FromObject("x5chain")) == null;
    }
}
