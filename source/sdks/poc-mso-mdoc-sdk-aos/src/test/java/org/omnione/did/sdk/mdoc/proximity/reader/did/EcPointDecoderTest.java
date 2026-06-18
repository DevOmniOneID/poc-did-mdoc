package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.ECPublicKey;
import org.junit.Test;

public class EcPointDecoderTest {
    @Test
    public void decodesP256PointFromParsedJwk() throws Exception {
        String didDoc = new String(Files.readAllBytes(
            Path.of("src/test/resources/fixtures/did_omn_issuer.json")), StandardCharsets.UTF_8);
        String b64 = new DidDocumentParser()
            .parse(didDoc, "did:omn:issuer?versionId=1#assert")
            .orElseThrow().getPublicKeyBase64();

        ECPublicKey key = EcPointDecoder.decode(b64);
        assertNotNull(key);
        assertEquals("EC", key.getAlgorithm());
        assertEquals(256, key.getParams().getCurve().getField().getFieldSize());
    }
}
