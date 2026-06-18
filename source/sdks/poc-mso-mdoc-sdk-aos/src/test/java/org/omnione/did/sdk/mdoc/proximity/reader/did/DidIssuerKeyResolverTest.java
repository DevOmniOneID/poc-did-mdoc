package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.upokecenter.cbor.CBORObject;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;
import org.junit.Test;

public class DidIssuerKeyResolverTest {

    private static final String ISSUER_KID = "did:omn:issuer?versionId=1#assert";

    /** Builds a minimal IssuerAuth COSE_Sign1 [protected, unprotected, payload, signature]. */
    private CBORObject coseSign1WithKid(String kid) {
        CBORObject unprotected = CBORObject.NewMap();
        if (kid != null) {
            unprotected.Add(CBORObject.FromObject(4), CBORObject.FromObject(kid));
        }
        CBORObject coseSign1 = CBORObject.NewArray();
        coseSign1.Add(CBORObject.FromObject(new byte[0])); // protected
        coseSign1.Add(unprotected);                        // unprotected (kid lives here, label 4)
        coseSign1.Add(CBORObject.FromObject(new byte[0])); // payload
        coseSign1.Add(CBORObject.FromObject(new byte[64])); // signature
        return coseSign1;
    }

    @Test
    public void resolvesIssuerKeyFromBundledDidDocument() {
        Optional<ECPublicKey> key = DidIssuerKeyResolver.resolve(coseSign1WithKid(ISSUER_KID));
        assertTrue("bundled DID document should resolve the issuer kid", key.isPresent());
        assertEquals("EC", key.get().getAlgorithm());
        // P-256 field size = 256 bits
        assertEquals(256, key.get().getParams().getCurve().getField().getFieldSize());
    }

    @Test
    public void emptyWhenNoKid() {
        assertFalse(DidIssuerKeyResolver.resolve(coseSign1WithKid(null)).isPresent());
    }

    @Test
    public void emptyWhenKidNotInBundledDocument() {
        Optional<ECPublicKey> key =
            DidIssuerKeyResolver.resolve(coseSign1WithKid("did:omn:other?versionId=1#key-99"));
        assertFalse(key.isPresent());
    }
}
