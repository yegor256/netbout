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

import com.netbout.hub.HubEntry;
import com.netbout.hub.HubIdentity;
import com.netbout.utils.TextUtils;
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
     * Salt for hash generation.
     */
    private static final String SALT = "U*#p}*YQ2@-+==I<,.?//";

    /**
     * Encrypt user+identity into text.
     * @param identity The identity
     * @return Encrypted string
     */
    public String encrypt(final HubIdentity identity) {
        final StringBuilder builder = new StringBuilder();
        builder
            .append(TextUtils.toBase(identity.user()))
            .append(this.SEPARATOR)
            .append(TextUtils.toBase(identity.name()))
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
    public HubIdentity decrypt(final String hash) throws
        Cryptor.DecryptionException {
        if (hash == null) {
            throw new DecryptionException(hash, "Hash is NULL");
        }
        final String[] parts = StringUtils.split(hash, this.SEPARATOR);
        // @checkstyle MagicNumber (1 line)
        if (parts.length != 3) {
            throw new DecryptionException(hash, "Not enough parts");
        }
        final String uname = TextUtils.fromBase(parts[0]);
        final String iname = TextUtils.fromBase(parts[1]);
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
        final HubIdentity identity = HubEntry.user(uname).identity(iname);
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

}
