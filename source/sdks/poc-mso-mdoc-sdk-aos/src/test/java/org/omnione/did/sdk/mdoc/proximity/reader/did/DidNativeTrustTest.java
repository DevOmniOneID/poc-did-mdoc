package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.upokecenter.cbor.CBORObject;
import org.junit.Test;

public class DidNativeTrustTest {

    private CBORObject coseSign1(CBORObject unprotected) {
        CBORObject c = CBORObject.NewArray();
        c.Add(CBORObject.FromObject(new byte[0])); // protected
        c.Add(unprotected);                        // unprotected
        c.Add(CBORObject.Null);                    // payload
        c.Add(CBORObject.FromObject(new byte[64])); // signature
        return c;
    }

    /** COSE_Sign1 with kid only (DID-native: no x5chain) */
    private CBORObject kidOnlyCose() {
        CBORObject u = CBORObject.NewMap();
        u.Add(CBORObject.FromObject(4), CBORObject.FromObject("did:omn:issuer?versionId=1#assert"));
        return coseSign1(u);
    }

    /** COSE_Sign1 with x5chain (label 33) present */
    private CBORObject x5chainCose() {
        CBORObject u = CBORObject.NewMap();
        u.Add(CBORObject.FromObject(33), CBORObject.FromObject(new byte[]{0x30, 0x00}));
        return coseSign1(u);
    }

    /** COSE_Sign1 with both kid and x5chain (hybrid — x5chain takes priority) */
    private CBORObject hybridCose() {
        CBORObject u = CBORObject.NewMap();
        u.Add(CBORObject.FromObject(4),  CBORObject.FromObject("did:omn:issuer?versionId=1#assert"));
        u.Add(CBORObject.FromObject(33), CBORObject.FromObject(new byte[]{0x30, 0x00}));
        return coseSign1(u);
    }

    @Test
    public void trustedWhenSignatureValidAndNoX5chain() {
        assertTrue(DidNativeTrust.isVerified(kidOnlyCose(), true));
    }

    @Test
    public void notTrustedWhenSignatureInvalid() {
        assertFalse(DidNativeTrust.isVerified(kidOnlyCose(), false));
    }

    @Test
    public void notTrustedWhenSignatureNull() {
        assertFalse(DidNativeTrust.isVerified(kidOnlyCose(), null));
    }

    @Test
    public void notTrustedWhenX5chainPresent() {
        // x5chain이 있으면 X.509 경로 위임 (DID 경로 아님)
        assertFalse(DidNativeTrust.isVerified(x5chainCose(), true));
    }

    @Test
    public void notTrustedWhenHybridX5chainPresent() {
        // kid + x5chain 동시: x5chain 존재하면 X.509 경로
        assertFalse(DidNativeTrust.isVerified(hybridCose(), true));
    }

    @Test
    public void notTrustedWhenCoseSign1Null() {
        assertFalse(DidNativeTrust.isVerified(null, true));
    }
}
