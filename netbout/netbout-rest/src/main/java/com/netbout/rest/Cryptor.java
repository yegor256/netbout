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

import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.PageBuilder;
import com.rexsl.core.Manifests;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
     * Encrypt user name.
     * @param name The name to encrypt
     * @return Encrypted string
     */
    public String encrypt(final String name) {
        return String.format(
            "%s%s%s",
            this.encode64(name),
            this.SEPARATOR,
            this.encode64(this.hash(name))
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
     * Get name from hash.
     * @param hash The hash to use
     * @return The name found in it
     * @throws DecryptionException If we can't decrypt it
     */
    public String decrypt(final String hash) throws DecryptionException {
        final String name = this.decode64(
            hash.substring(0, hash.indexOf(this.SEPARATOR))
        );
        final String sign = this.decode64(
            hash.substring(
                hash.indexOf(this.SEPARATOR) + this.SEPARATOR.length()
            )
        );
        if (sign.equals(this.hash(name))) {
            throw new DecryptionException(hash);
        }
        return name;
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
