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
package com.netbout.rest;

import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Encrypts and decrypts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Cryptor {

    /**
     * Separator between name and hash.
     */
    private static final String SEPARATOR = ".";

    /**
     * Encoding to be used.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Salt for hash generation.
     */
    private static final String SALT = "U*#p}*YQ2@-+==I<,.?//";

    /**
     * The entry to work with.
     */
    private final Entry entry;

    /**
     * Public ctor.
     * @param ent Entry to work with
     */
    public Cryptor(final Entry ent) {
        this.entry = ent;
    }

    /**
     * Encrypt identity into text.
     * @param identity The identity
     * @return Encrypted string
     */
    public String encrypt(final Identity identity) {
        final StringBuilder builder = new StringBuilder();
        builder
            .append(this.toBase64(identity.user().name()))
            .append(this.SEPARATOR)
            .append(this.toBase64(identity.name()))
            .append(this.SEPARATOR)
            .append(this.hash(identity.name()));
        return builder.toString();
    }

    /**
     * Decryption exception.
     */
    public static final class DecryptionException extends Exception {
        /**
         * Public ctor.
         * @param hash The source of problem
         * @param message Error message
         * @param args Optional arguments
         */
        public DecryptionException(final String hash, final String message,
            final Object... args) {
            super(
                String.format("%s [%s]", String.format(message, args), hash)
            );
        }
    }

    /**
     * Get identity from hash.
     * @param hash The hash to use
     * @return The name found in it
     * @throws Cryptor.DecryptionException If we can't decrypt it
     */
    public Identity decrypt(final String hash) throws
        Cryptor.DecryptionException {
        final String[] parts = StringUtils.split(hash, this.SEPARATOR);
        // @checkstyle MagicNumber (1 line)
        if (parts.length != 3) {
            throw new DecryptionException(hash, "Not enough parts");
        }
        final String uname = this.fromBase64(parts[0]);
        final String iname = this.fromBase64(parts[1]);
        final String signature = parts[2];
        if (!signature.equals(this.hash(iname))) {
            throw new DecryptionException(
                hash,
                "Signature ('%s') mismatch, while '%s' expected for '%s'",
                signature,
                this.hash(iname),
                iname
            );
        }
        Identity identity;
        try {
            identity = this.entry.identity(iname);
        } catch (com.netbout.spi.UnknownIdentityException ex) {
            throw new DecryptionException(
                hash,
                "Identity '%s' not found: %s",
                iname,
                ex.getMessage()
            );
        }
        assert identity != null;
        return identity;
    }

    /**
     * Create hash from a string.
     * @param text The text to work with
     * @return The hash
     */
    private String hash(final String text) {
        return DigestUtils.md5Hex(text + this.SALT);
    }

    /**
     * Encode string into 64-bit string.
     * @param text The text to encode
     * @return Encoded text
     */
    private String toBase64(final String text) {
        try {
            return new Base64().encodeToString(text.getBytes(this.ENCODING));
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Decode string from 64-bit string.
     * @param text The text to decode
     * @return Decoded text
     */
    private String fromBase64(final String text) {
        try {
            return new String(new Base64().decode(text), this.ENCODING);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
