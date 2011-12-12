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

import com.netbout.hub.User;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.utils.Cryptor;
import com.sun.jersey.api.client.Client;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * REST authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/auth")
public final class AuthRs extends AbstractRs {

    /**
     * Authentication page.
     * @param uname User name
     * @param iname Identity name
     * @param secret Secret word
     * @return The JAX-RS response
     */
    @GET
    public Response auth(@QueryParam("user") final String uname,
        @QueryParam("identity") final String iname,
        @QueryParam("secret") final String secret) {
        this.logoff();
        Identity identity;
        try {
            identity = this.authenticate(uname, iname, secret);
        } catch (IOException ex) {
            throw new ForwardException(this, this.base().path("/g"), ex);
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .header("Netbout-auth", new Cryptor().encrypt(identity))
            .build();
    }

    /**
     * Authenticate the user through facebook.
     * @param uname User name
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity authenticate(final String uname, final String iname,
        final String secret) throws IOException {
        final Identity remote = this.load(
            UriBuilder.fromUri(uname)
                .queryParam("identity", iname)
                .queryParam("secret", secret)
                .build()
        );
        if (!remote.name().equals(iname)) {
            throw new ForwardException(
                this,
                this.base().path("/g"),
                String.format(
                    "Invalid identity name retrieved '%s', while '%s' expected",
                    remote.name(),
                    iname
                )
            );
        }
        final User user = this.hub().user(uname);
        Identity identity;
        try {
            identity = user.identity(iname);
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(
                String.format(
                    "Identity '%s' is not reachable: %s",
                    iname,
                    ex
                )
            );
        }
        for (String alias : remote.aliases()) {
            identity.alias(alias);
        }
        identity.setPhoto(remote.photo());
        return identity;
    }

    /**
     * Load identity from the URL provided.
     * @param uri The URI to load from
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity load(final URI uri) throws IOException {
        return Client.create().resource(uri)
            .accept(MediaType.APPLICATION_XML)
            .get(RemotePage.class)
            .getIdentity();
    }

    /**
     * Remote page wrapper.
     */
    @XmlRootElement(name = "page")
    @XmlAccessorType(XmlAccessType.NONE)
    private static final class RemotePage {
        /**
         * The identity.
         */
        private RemoteIdentity identity;
        /**
         * Set identity.
         * @param idnt The identity
         */
        @XmlElement
        public void setIdentity(final RemoteIdentity idnt) {
            this.identity = idnt;
        }
        /**
         * Get identity.
         * @return The identity
         */
        public RemoteIdentity getIdentity() {
            if (this.identity == null) {
                throw new IllegalStateException("/page/identity missed");
            }
            return this.identity;
        }
    }

    /**
     * Remote identity representative.
     */
    @XmlType(name = "identity")
    private static final class RemoteIdentity implements Identity {
        /**
         * User name.
         */
        private transient String uname;
        /**
         * Identity name.
         */
        private transient String iname;
        /**
         * Photo of identity.
         */
        private transient URL iphoto;
        /**
         * Aliases.
         */
        private transient List<String> ialiases = new ArrayList<String>();
        /**
         * Set user name.
         * @param name The name of it
         */
        @XmlElement
        public void setUser(final String name) {
            this.uname = name;
        }
        /**
         * Set identity name.
         * @param name The name of it
         */
        @XmlElement
        public void setName(final String name) {
            this.iname = name;
        }
        /**
         * Set photo.
         * @param url The URL
         */
        @XmlElement(name = "photo")
        public void setJaxbPhoto(final String url) {
            try {
                this.iphoto = new URL(url);
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }
        /**
         * Set aliases.
         * @param names List of them
         */
        @XmlElement
        public void setAliases(final List<String> names) {
            this.ialiases.addAll(names);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String user() {
            if (this.uname == null) {
                throw new IllegalStateException("/page/identity/user missed");
            }
            return this.uname;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String name() {
            if (this.iname == null) {
                throw new IllegalStateException("/page/identity/name missed");
            }
            return this.iname;
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
        public URL photo() {
            if (this.iphoto == null) {
                throw new IllegalStateException("/page/identity/photo missed");
            }
            return this.iphoto;
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
        public Set<String> aliases() {
            return new HashSet(this.ialiases);
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
