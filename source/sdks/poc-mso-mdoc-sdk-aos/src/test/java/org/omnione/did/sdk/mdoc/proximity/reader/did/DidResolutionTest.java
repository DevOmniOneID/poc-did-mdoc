package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DidResolutionTest {
    @Test
    public void exposesFields() {
        DidResolution r = new DidResolution("did:omn:issuer", "assert", "BASE64KEY", "Secp256r1");
        assertEquals("did:omn:issuer", r.getDid());
        assertEquals("assert", r.getFragment());
        assertEquals("BASE64KEY", r.getPublicKeyBase64());
        assertEquals("Secp256r1", r.getAlgorithm());
    }
}
