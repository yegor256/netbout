/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.text;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;

/**
 * String, which encrypts itself.
 *
 * <p>Objects of this class are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://en.wikipedia.org/wiki/One-time_pad">One Time Pad</a>
 */
public final class SecureString {

    /**
     * Password to use in encryption.
     */
    private static final String KEY = "j&^%hgfRR43$#&==_ )(00(0}{-~";

    /**
     * Open content (without encryption).
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt The text (without encryption)
     */
    public SecureString(final String txt) {
        this.text = txt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Base64.encodeBase64String(this.xor(this.text.getBytes()))
            .replace("=", "");
    }

    /**
     * Create it from encrypted input.
     * @param hash The hash to use
     * @return The string
     */
    public static SecureString valueOf(final String hash) {
        try {
            this.text = new String(
                SecureString.xor(Base64.decodeBase64(hash.getBytes())),
                CharEncoding.UTF_8
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get its original text, without encryption.
     * @return The text
     */
    public String decrypted() {
        return this.text;
    }

    /**
     * XOR array of bytes.
     * @param input The input to XOR
     * @return Encrypted output
     */
    private static byte[] xor(final byte[] input) {
        final byte[] output = new byte[input.length];
        final byte[] secret = SecureString.KEY.getBytes();
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

}
