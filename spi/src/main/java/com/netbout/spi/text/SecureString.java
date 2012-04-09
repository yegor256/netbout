/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.text;

import com.rexsl.core.Manifests;
import org.apache.commons.lang.CharEncoding;

/**
 * String, which encrypts itself.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://en.wikipedia.org/wiki/One-time_pad">One Time Pad</a>
 * @see <a href="http://en.wikipedia.org/wiki/Base64">Base64</a>
 * @checkstyle MagicNumber (500 lines)
 */
public final class SecureString {

    /**
     * Open content (without encryption).
     */
    private final transient String raw;

    /**
     * Public ctor.
     * @param text The text (without encryption)
     */
    public SecureString(final Object text) {
        this.raw = text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.pack(this.xor(this.raw.getBytes()));
    }

    /**
     * Create it from encrypted input.
     * @param hash The hash to use
     * @return The string
     * @throws StringDecryptionException When can't decrypt
     */
    public static SecureString valueOf(final String hash)
        throws StringDecryptionException {
        try {
            return new SecureString(
                new String(
                    SecureString.xor(SecureString.unpack(hash)),
                    CharEncoding.UTF_8
                )
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get its original text, without encryption.
     * @return The text
     */
    public String text() {
        return this.raw;
    }

    /**
     * XOR array of bytes.
     * @param input The input to XOR
     * @return Encrypted output
     */
    private static byte[] xor(final byte[] input) {
        final byte[] output = new byte[input.length];
        final byte[] secret = Manifests.read("Netbout-SecurityKey").getBytes();
        int spos = 0;
        for (int pos = 0; pos < input.length; pos += 1) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            spos += 1;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }

    /**
     * Pack array of bytes into string.
     * @param input The array of bytes
     * @return Packed output
     */
    private static String pack(final byte[] input) {
        final StringBuilder text = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (int pos = 0; pos < input.length; pos += 1) {
            buffer = (buffer << 8) | (0xFF & input[pos]);
            bits += 8;
            while (bits >= 5) {
                text.append(
                    SecureString.toChar(
                        (byte) ((buffer >> (bits - 5)) & 0x1F)
                    )
                );
                bits -= 5;
            }
        }
        if (bits > 0) {
            text.append(
                SecureString.toChar((byte) ((buffer << (5 - bits)) & 0x1F))
            );
        }
        return text.toString();
    }

    /**
     * Unpack sting into array of bytes.
     * @param input The packed string
     * @return Unpacked output
     * @throws StringDecryptionException When can't unpack
     */
    private static byte[] unpack(final String input)
        throws StringDecryptionException {
        final int length = input.length();
        final byte[] output = new byte[(int) length * 5 / 8];
        long buffer = 0;
        int bits = 0;
        int opos = 0;
        for (int pos = 0; pos < length; pos += 1) {
            buffer = (buffer << 5) + SecureString.toBits(input.charAt(pos));
            bits += 5;
            while (bits >= 8) {
                output[opos] = (byte) (buffer >> (bits - 8));
                opos += 1;
                bits -= 8;
            }
        }
        return output;
    }

    /**
     * Convert 5 bits to one char.
     * @param bits The bits
     * @return The char
     */
    private static char toChar(final byte bits) {
        char output;
        if (bits >= 0 && bits <= 6) {
            output = (char) ('0' + bits);
        } else if (bits >= 7 && bits <= 32) {
            output = (char) ('A' + bits - 7);
        } else {
            throw new IllegalArgumentException(
                String.format("Illegal bits value: '%d'", bits)
            );
        }
        return output;
    }

    /**
     * Convert char to 5 bits.
     * @param symbol The character
     * @return The bits
     * @throws StringDecryptionException When can't decrypt
     */
    private static byte toBits(final char symbol)
        throws StringDecryptionException {
        byte output;
        if (symbol >= 'A' && symbol <= 'Z') {
            output = (byte) (symbol - 'A' + 7);
        } else if (symbol >= '0' && symbol <= '6') {
            output = (byte) (symbol - '0');
        } else {
            throw new StringDecryptionException(
                String.format("Illegal character in Base32: '%s'", symbol)
            );
        }
        return output;
    }

}
