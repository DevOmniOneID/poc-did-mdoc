/*
 * Copyright 2026 OmniOne. Apache-2.0.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import java.math.BigInteger;
import java.util.Arrays;

/** Minimal base58btc decoder (Bitcoin alphabet), dependency-free. */
final class Base58 {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(58);

    private Base58() {}

    static byte[] decode(String input) {
        if (input.isEmpty()) return new byte[0];
        BigInteger num = BigInteger.ZERO;
        for (int i = 0; i < input.length(); i++) {
            int digit = ALPHABET.indexOf(input.charAt(i));
            if (digit < 0) throw new IllegalArgumentException("Invalid base58 char: " + input.charAt(i));
            num = num.multiply(BASE).add(BigInteger.valueOf(digit));
        }
        // When num is zero, the numeric payload is empty (no content bytes).
        // BigInteger.ZERO.toByteArray() returns {0x00}, which would be double-counted
        // with the leading-zero prepend below, producing {0,0} for input "1".
        byte[] bytes;
        if (num.equals(BigInteger.ZERO)) {
            bytes = new byte[0];
        } else {
            bytes = num.toByteArray();
            // strip BigInteger sign byte
            if (bytes.length > 1 && bytes[0] == 0) {
                bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
            }
        }
        // restore leading zeros (encoded as '1')
        int leadingZeros = 0;
        while (leadingZeros < input.length() && input.charAt(leadingZeros) == '1') leadingZeros++;
        byte[] out = new byte[leadingZeros + bytes.length];
        System.arraycopy(bytes, 0, out, leadingZeros, bytes.length);
        return out;
    }
}
