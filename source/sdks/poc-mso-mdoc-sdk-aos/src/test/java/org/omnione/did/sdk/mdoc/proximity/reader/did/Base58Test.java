package org.omnione.did.sdk.mdoc.proximity.reader.did;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class Base58Test {
    @Test
    public void decodesKnownVector() {
        // "Hello World!" base58btc = "2NEpo7TZRRrLZSi2U"
        assertArrayEquals("Hello World!".getBytes(), Base58.decode("2NEpo7TZRRrLZSi2U"));
    }

    @Test
    public void decodesLeadingZeros() {
        // base58 "1" maps to a single 0x00 byte
        assertArrayEquals(new byte[]{0x00}, Base58.decode("1"));
    }
}
