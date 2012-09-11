/**
 * Copyright (c) 2009-2012, Netbout.com
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
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import com.netbout.spi.text.SecureString;
import java.net.URL;
import java.util.Set;

/**
 * Identity with encrypted {@link #toString()}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CryptedIdentity implements Identity {

    /**
     * The wrapped identity.
     */
    private final transient Identity idnt;

    /**
     * Public ctor.
     * @param identity The identity
     */
    public CryptedIdentity(final Identity identity) {
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
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Identity identity) {
        return this.idnt.compareTo(identity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return this.idnt.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.idnt.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        return this.idnt.eta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        return this.idnt.authority();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.idnt.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return this.idnt.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return this.idnt.bout(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final String query) {
        return this.idnt.inbox(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        return this.idnt.profile();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity friend(final Urn name) throws UnreachableUrnException {
        return this.idnt.friend(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        return this.idnt.friends(keyword);
    }

    /**
     * Instantiate it from hash and Hub.
     * @param hub Hub where to get identities
     * @param hash The hash to use
     * @return The name found in it
     * @throws CryptedIdentity.DecryptionException If we can't decrypt it
     */
    public static CryptedIdentity parse(final Hub hub, final String hash)
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
            identity = hub.identity(new Urn(iname));
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new CryptedIdentity.DecryptionException(ex);
        } catch (java.net.URISyntaxException ex) {
            throw new CryptedIdentity.DecryptionException(ex);
        }
        Logger.debug(
            CryptedIdentity.class,
            "#decrypt(%[type]s, %s): identity '%s' found",
            hub,
            hash,
            identity.name()
        );
        return new CryptedIdentity(identity);
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
