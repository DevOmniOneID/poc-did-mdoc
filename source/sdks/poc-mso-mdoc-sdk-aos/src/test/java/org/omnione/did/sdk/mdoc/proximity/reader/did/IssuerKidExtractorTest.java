package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.upokecenter.cbor.CBORObject;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class IssuerKidExtractorTest {
    private CBORObject coseSign1(CBORObject unprotected) {
        CBORObject c = CBORObject.NewArray();
        c.Add(CBORObject.FromObject(new byte[0])); // protected
        c.Add(unprotected);                          // unprotected
        c.Add(CBORObject.FromObject(new byte[0])); // payload
        c.Add(CBORObject.FromObject(new byte[64])); // signature
        return c;
    }

    @Test
    public void readsKidAsTextString() {
        CBORObject u = CBORObject.NewMap();
        u.Add(CBORObject.FromObject(4), CBORObject.FromObject("did:omn:issuer?versionId=1#assert"));
        assertEquals("did:omn:issuer?versionId=1#assert",
            IssuerKidExtractor.extractKid(coseSign1(u)));
    }

    @Test
    public void readsKidAsByteString() {
        CBORObject u = CBORObject.NewMap();
        u.Add(CBORObject.FromObject(4),
            CBORObject.FromObject("did:omn:issuer#assert".getBytes(StandardCharsets.UTF_8)));
        assertEquals("did:omn:issuer#assert", IssuerKidExtractor.extractKid(coseSign1(u)));
    }

    @Test
    public void nullWhenNoKid() {
        assertNull(IssuerKidExtractor.extractKid(coseSign1(CBORObject.NewMap())));
    }
}
