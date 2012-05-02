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

import com.netbout.hub.UrnResolver;
import com.netbout.spi.Urn;
import com.sun.jersey.api.client.Client;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Mediator between us and remote authenticator.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class AuthMediator {

    /**
     * URN resolver.
     */
    private final transient UrnResolver resolver;

    /**
     * Public ctor.
     * @param rslv Resolver of URNs
     */
    public AuthMediator(final UrnResolver rslv) {
        this.resolver = rslv;
    }

    /**
     * Validate provided data.
     * @param iname Identity name
     * @param secret Secret word
     * @return Identity name, if it's valid
     * @throws IOException If some problem with FB
     */
    public RemoteIdentity authenticate(final Urn iname, final String secret)
        throws IOException {
        RemoteIdentity remote;
        if (iname.isEmpty() && "localhost".equals(secret)) {
            remote = this.stub(iname);
        } else {
            remote = this.remote(iname, secret);
        }
        return remote;
    }

    /**
     * Authenticate as a local stub.
     * @param iname Identity name
     * @return Identity
     */
    private RemoteIdentity stub(final Urn iname) {
        final RemoteIdentity identity = new RemoteIdentity();
        identity.setAuthority("http://www.netbout.com/nb");
        identity.setName(iname.toString());
        identity.setPhoto("http://cdn.netbout.com/unknown.png");
        identity.setLocale(Locale.ENGLISH.toString());
        return identity;
    }

    /**
     * Authenticate as remote identity.
     * @param iname Identity name
     * @param secret Secret word
     * @return Identity
     * @throws IOException If some problem
     */
    private RemoteIdentity remote(final Urn iname, final String secret)
        throws IOException {
        RemoteIdentity remote;
        URL entry;
        try {
            entry = this.resolver.authority(iname);
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new IOException(ex);
        }
        final URI uri = UriBuilder.fromUri(entry.toString())
            .queryParam("identity", "{iname}")
            .queryParam("secret", "{secret}")
            .build(iname, secret);
        try {
            remote = this.load(uri);
        } catch (IOException ex) {
            throw new IOException(
                String.format(
                    "Failed to load identity '%s' from '%s'",
                    iname,
                    uri
                ),
                ex
            );
        }
        if (!remote.name().equals(iname) && !iname.nss().isEmpty()) {
            throw new IOException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Invalid identity name '%s' retrieved from '%s', while '%s' expected",
                    remote.name(),
                    uri,
                    iname
                )
            );
        }
        return remote;
    }

    /**
     * Load identity from the URL provided.
     * @param uri The URI to load from
     * @return The identity found
     * @throws IOException If some problem with FB
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private RemoteIdentity load(final URI uri) throws IOException {
        final long start = System.nanoTime();
        RemoteIdentity identity;
        try {
            identity = Client.create().resource(uri)
                .accept(MediaType.APPLICATION_XML)
                .get(RemotePage.class)
                .getIdentity();
            // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            throw new IOException(
                String.format("Failed to load identity from '%s'", uri),
                ex
            );
        }
        Logger.debug(
            this,
            "#load(%s): identity '%s' found in %[nano]s",
            uri,
            identity.name(),
            System.nanoTime() - start
        );
        return identity;
    }

}
