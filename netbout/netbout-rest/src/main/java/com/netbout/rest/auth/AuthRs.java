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
package com.netbout.rest.auth;

import com.netbout.rest.AbstractPage;
import com.netbout.rest.AbstractRs;
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.utils.Cryptor;
import com.sun.jersey.api.client.Client;
import com.ymock.util.Logger;
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
     * @param iname Identity name
     * @param secret Secret word
     * @return The JAX-RS response
     */
    @GET
    public Response auth(@QueryParam("identity") final Urn iname,
        @QueryParam("secret") final String secret) {
        if (iname == null || secret == null) {
            throw new LoginRequiredException(
                this,
                "'identity' and 'secret' query params are mandatory"
            );
        }
        this.logoff();
        Identity identity;
        try {
            identity = this.authenticate(iname, secret);
        } catch (IOException ex) {
            throw new LoginRequiredException(this, ex);
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
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity authenticate(final Urn iname,
        final String secret) throws IOException {
        final Identity remote = this.remote(iname, secret);
        Identity identity;
        try {
            identity = this.hub().identity(iname);
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new LoginRequiredException(this, ex);
        }
        for (String alias : remote.aliases()) {
            identity.alias(alias);
        }
        identity.setPhoto(remote.photo());
        return identity;
    }

    /**
     * Validate provided data.
     * @param iname Identity name
     * @param secret Secret word
     * @return Identity name, if it's valid
     */
    private Identity remote(final Urn iname, final String secret)
        throws IOException {
        Identity remote;
        if (iname.isEmpty() && "localhost".equals(secret)) {
            final RemoteIdentity idnt = new RemoteIdentity();
            idnt.setAuthority("http://www.netbout.com/nb");
            idnt.setName(iname.toString());
            idnt.setJaxbPhoto("http://img.netbout.com/unknown.png");
            remote = idnt;
        } else {
            URL entry;
            try {
                entry = this.hub().resolver().authority(iname);
            } catch (com.netbout.spi.UnreachableUrnException ex) {
                throw new LoginRequiredException(this, ex);
            }
            remote = this.load(
                UriBuilder.fromUri(entry.toString())
                    .queryParam("identity", iname)
                    .queryParam("secret", secret)
                    .queryParam("ip", this.httpServletRequest().getRemoteAddr())
                    .build()
            );
            if (!remote.name().equals(iname)) {
                throw new LoginRequiredException(
                    this,
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "Invalid identity name retrieved '%s', while '%s' expected",
                        remote.name(),
                        iname
                    )
                );
            }
        }
        return remote;
    }

    /**
     * Load identity from the URL provided.
     * @param uri The URI to load from
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    private Identity load(final URI uri) throws IOException {
        final long start = System.currentTimeMillis();
        Identity identity;
        try {
            identity = Client.create().resource(uri)
                .accept(MediaType.APPLICATION_XML)
                .get(RemotePage.class)
                .getIdentity();
        } catch (com.sun.jersey.api.client.UniformInterfaceException ex) {
            throw new IOException(ex);
        }
        Logger.debug(
            this,
            "#load(%s): identity '%s' found in %dms",
            uri,
            identity.name(),
            System.currentTimeMillis() - start
        );
        return identity;
    }

}
