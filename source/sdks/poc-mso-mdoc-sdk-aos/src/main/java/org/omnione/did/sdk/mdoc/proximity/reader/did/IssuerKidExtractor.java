/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import java.nio.charset.StandardCharsets;

/** Extracts the DID-native issuer kid (COSE header label 4) from an IssuerAuth COSE_Sign1. */
public final class IssuerKidExtractor {
    private static final int COSE_HEADER_KID = 4;

    private IssuerKidExtractor() {}

    /** Returns the kid string (e.g. did:omn:issuer?versionId=1#assert), or null if absent. */
    public static String extractKid(CBORObject coseSign1) {
        if (coseSign1 == null || coseSign1.getType() != CBORType.Array || coseSign1.size() < 2) {
            return null;
        }
        // 1) unprotected header (index 1)
        String kid = readKid(coseSign1.get(1));
        if (kid != null) return kid;
        // 2) protected header (index 0, bstr-wrapped CBOR map) — robustness
        try {
            CBORObject protectedBstr = coseSign1.get(0);
            if (protectedBstr != null && protectedBstr.getType() == CBORType.ByteString
                    && protectedBstr.GetByteString().length > 0) {
                return readKid(CBORObject.DecodeFromBytes(protectedBstr.GetByteString()));
            }
        } catch (Exception ignored) { /* fall through */ }
        return null;
    }

    private static String readKid(CBORObject header) {
        if (header == null || header.getType() != CBORType.Map) return null;
        CBORObject kid = header.get(CBORObject.FromObject(COSE_HEADER_KID));
        if (kid == null) return null;
        String value;
        if (kid.getType() == CBORType.TextString) {
            value = kid.AsString();
        } else if (kid.getType() == CBORType.ByteString) {
            value = new String(kid.GetByteString(), StandardCharsets.UTF_8);
        } else {
            return null;
        }
        // String.isBlank() is API 33+; trim().isEmpty() is minSdk-26 safe and equivalent here.
        return value.trim().isEmpty() ? null : value;
    }
}
