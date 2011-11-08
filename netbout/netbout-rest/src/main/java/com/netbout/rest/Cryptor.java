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
    private static final String SEPARATOR = "===";

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
        return String.format(
            "%s%s%s",
            this.encode64(identity.user().name()),
            this.SEPARATOR,
            this.encode64(identity.name()),
            this.SEPARATOR,
            this.encode64(this.hash(identity.name()))
        );
    }

    /**
     * Decryption exception.
     */
    public static final class DecryptionException extends Exception {
        /**
         * Public ctor.
         * @param hash The source of problem
         */
        public DecryptionException(final String hash) {
            super("Can't decrypt: " + hash);
        }
    }

    /**
     * Get identity from hash.
     * @param hash The hash to use
     * @return The name found in it
     * @throws DecryptionException If we can't decrypt it
     */
    public Identity decrypt(final String hash) throws DecryptionException {
        final String[] parts = StringUtils.split(hash, this.SEPARATOR);
        if (parts.length != 3) {
            throw new DecryptionException(hash);
        }
        final String uname = this.decode64(parts[0]);
        final String iname = this.decode64(parts[1]);
        final String signature = this.decode64(parts[2]);
        if (!signature.equals(this.hash(iname))) {
            throw new DecryptionException(hash);
        }
        Identity identity;
        try {
            identity = this.entry.identity(iname);
        } catch (com.netbout.spi.UnknownIdentityException ex) {
            throw new DecryptionException(hash);
        }
        return identity;
    }

    /**
     * Create hash from a string.
     * @param text The text to work with
     * @return The hash
     */
    private String hash(final String text) {
        return String.format("%d", text.hashCode());
    }

    /**
     * Encode string into 64-bit string.
     * @param text The text to encode
     * @return Encoded text
     */
    private String encode64(final String text) {
        return text;
    }

    /**
     * Decode string from 64-bit string.
     * @param text The text to decode
     * @return Decoded text
     */
    private String decode64(final String text) {
        return text;
    }

}
