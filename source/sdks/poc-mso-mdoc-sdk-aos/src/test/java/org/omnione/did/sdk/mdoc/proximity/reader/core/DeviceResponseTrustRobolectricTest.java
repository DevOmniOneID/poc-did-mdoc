package org.omnione.did.sdk.mdoc.proximity.reader.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Base64;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * End-to-end-ish verification of the DID-native trust path WITHOUT BLE and WITHOUT a real device.
 *
 * <p>BLE is only a transport — it hands the reader a {@code DeviceResponse} byte array. This test
 * feeds that array straight into {@link DeviceResponseParser#parse(byte[])} and then asks
 * {@link TrustManager#isDocumentTrusted}, exercising the exact code path a real BLE session uses
 * after receiving bytes. Robolectric provides {@code android.util.Base64}/{@code Log} on the JVM.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 33)
public class DeviceResponseTrustRobolectricTest {

    private byte[] fixtureDeviceResponse() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/mdoc_kid_devresp.b64")) {
            assertNotNull("fixture mdoc_kid_devresp.b64 must be on the test classpath", in);
            String b64 = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            // fixture is base64url-encoded
            return Base64.decode(b64, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        }
    }

    @Test
    public void didNativeMdocSignatureVerifiesViaBundledDidDocument() throws Exception {
        DeviceResponseParser.ParsedResponse response =
            DeviceResponseParser.parse(fixtureDeviceResponse());

        assertFalse("response must contain at least one document",
            response.getDocuments().isEmpty());
        DeviceResponseParser.ParsedDocument doc = response.getDocuments().get(0);

        assertNotNull(doc.getIssuerSignedInfo());
        assertEquals("IssuerAuth signature must verify against the bundled DID document",
            Boolean.TRUE, doc.getIssuerSignedInfo().isIssuerSignatureValid());
    }

    @Test
    public void didNativeMdocIsTrustedWithoutSkipIssuerTrust() throws Exception {
        DeviceResponseParser.ParsedResponse response =
            DeviceResponseParser.parse(fixtureDeviceResponse());
        DeviceResponseParser.ParsedDocument doc = response.getDocuments().get(0);

        // No X.509 trusted certs configured — the DID-native path must stand on its own.
        TrustManager trustManager = new TrustManager(Collections.emptyList());

        assertTrue("DID-native mDoc must be trusted without 'Skip Issuer Trust'",
            trustManager.isDocumentTrusted(doc));
    }
}
