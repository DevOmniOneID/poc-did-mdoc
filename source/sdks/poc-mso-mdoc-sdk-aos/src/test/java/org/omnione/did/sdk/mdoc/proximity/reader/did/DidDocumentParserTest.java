package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;

public class DidDocumentParserTest {
    private static final String KID = "did:omn:issuer?versionId=1#assert";

    private String fixture(String name) throws Exception {
        return new String(Files.readAllBytes(
            Path.of("src/test/resources/fixtures/", name)), StandardCharsets.UTF_8);
    }

    @Test
    public void parsesPublicKeyJwk() throws Exception {
        Optional<DidResolution> r = new DidDocumentParser().parse(fixture("did_omn_issuer.json"), KID);
        assertTrue(r.isPresent());
        assertEquals("did:omn:issuer", r.get().getDid());
        assertEquals("assert", r.get().getFragment());
        assertFalse(r.get().getPublicKeyBase64().isEmpty());
    }

    @Test
    public void parsesUniversalResolverMultibase() throws Exception {
        Optional<DidResolution> r = new DidDocumentParser()
            .parse(fixture("resolution_did_omn_issuer.json"), KID);
        assertTrue(r.isPresent());
        assertFalse(r.get().getPublicKeyBase64().isEmpty());
    }

    @Test
    public void emptyWhenKidNotFound() throws Exception {
        Optional<DidResolution> r = new DidDocumentParser()
            .parse(fixture("did_omn_issuer.json"), "did:omn:other#key-99");
        assertFalse(r.isPresent());
    }
}
