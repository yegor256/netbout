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

import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.utils.Cipher;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Authorizer of "nb:..." identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/nb")
public final class NbRs extends AbstractRs {

    /**
     * Authentication page.
     * @param iname Name of identity
     * @param secret The secret code
     * @return The JAX-RS response
     */
    @GET
    public Response auth(@QueryParam("identity") final String iname,
        @QueryParam("secret") final String secret) {
        this.validate(iname, secret);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(new NbIdentity(iname.substring(3)))
            .build();
    }

    /**
     * Validate them.
     * @param iname Name of identity
     * @param secret The secret code
     */
    private void validate(final String iname, final String secret) {
        if ((iname == null) || (secret == null) || secret.isEmpty()) {
            throw new ForwardException(this, this.base(), "Failure");
        }
        if (!iname.matches("nb:[a-z]+")) {
            throw new ForwardException(this, this.base(), "Invalid name");
        }
        try {
            if (!new Cipher().decrypt(secret).equals(iname)) {
                throw new ForwardException(this, this.base(), "Wrong secret");
            }
        } catch (com.netbout.utils.DecryptionException ex) {
            throw new ForwardException(this, this.base(), ex);
        }
    }

    /**
     * Nb identity representative.
     */
    private static final class NbIdentity implements Identity {
        /**
         * The suffix after "nb:".
         */
        private transient String suffix;
        /**
         * Public ctor.
         * @param sfx The suffix after "nb:"
         */
        public NbIdentity(final String sfx) {
            this.suffix = sfx;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String user() {
            return "http://www.netbout.com/nb";
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String name() {
            return String.format("nb:%s", this.suffix);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public URL photo() {
            try {
                return new URL(
                    String.format(
                        "http://img.netbout.com/nb/%s.png",
                        this.suffix
                    )
                );
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<String> aliases() {
            return new HashSet<String>();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Bout start() {
            throw new UnsupportedOperationException("#start()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Bout bout(final Long number) {
            throw new UnsupportedOperationException("#bout()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public List<Bout> inbox(final String query) {
            throw new UnsupportedOperationException("#inbox()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void setPhoto(final URL pic) {
            throw new UnsupportedOperationException("#setPhoto()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Identity friend(final String name) {
            throw new UnsupportedOperationException("#friend()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Identity> friends(final String keyword) {
            throw new UnsupportedOperationException("#friends()");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void alias(final String alias) {
            throw new UnsupportedOperationException("#alias()");
        }
    }

}
