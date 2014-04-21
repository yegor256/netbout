/**
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.text.SecureString;
import javax.validation.constraints.NotNull;

/**
 * Identity with encrypted {@link #toString()}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CryptedIdentity {

    /**
     * The wrapped identity.
     */
    private final transient Identity idnt;

    /**
     * Public ctor.
     * @param identity The identity
     */
    public CryptedIdentity(@NotNull final Identity identity) {
        this.idnt = identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new SecureString(this.idnt.name()).toString();
    }

    /**
     * Instantiate it from hash and Hub.
     * @param hub Hub where to get identities
     * @param hash The hash to use
     * @return The name found in it
     * @throws CryptedIdentity.DecryptionException If we can't decrypt it
     */
    public static Identity parse(@NotNull final Hub hub, final String hash)
        throws CryptedIdentity.DecryptionException {
        if (hash == null) {
            throw new CryptedIdentity.DecryptionException();
        }
        String iname;
        try {
            iname = SecureString.valueOf(hash).text();
        } catch (com.netbout.spi.text.StringDecryptionException ex) {
            throw new CryptedIdentity.DecryptionException(ex);
        }
        Identity identity;
        try {
            identity = hub.identity(new URN(iname));
        } catch (Identity.UnreachableURNException ex) {
            throw new CryptedIdentity.DecryptionException(ex);
        } catch (java.net.URISyntaxException ex) {
            throw new CryptedIdentity.DecryptionException(ex);
        }
        Logger.debug(
            CryptedIdentity.class,
            "#parse(%[type]s, '%s'): identity '%s' found",
            hub,
            hash,
            identity.name()
        );
        return identity;
    }

    /**
     * When decryption can't build an identity.
     */
    public static final class DecryptionException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA789EC21879L;
        /**
         * Public ctor.
         */
        public DecryptionException() {
            super("");
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public DecryptionException(final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public DecryptionException(final Throwable cause) {
            super(cause);
            Logger.warn(
                this,
                "#DecryptionException('%s'): thrown",
                cause.getMessage()
            );
        }
        /**
         * Public ctor.
         * @param hash The source of problem
         * @param message Error message
         * @param args Optional arguments
         */
        public DecryptionException(final String hash, final String message,
            final Object... args) {
            super(
                Logger.format("%s [%s]", String.format(message, args), hash)
            );
            Logger.warn(
                this,
                "#DecryptionException('%s', '%s'): thrown",
                hash,
                message
            );
        }

    }
}
