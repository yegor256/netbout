/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The identity of the person in a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class RestIdentity implements Identity {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Public ctor.
     * @param clnt Rest client
     */
    public RestIdentity(final RestClient clnt) {
        this.client = clnt;
    }

    /**
     * Get its URI.
     * @return The URI
     */
    public URI uri() {
        return this.client.uri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String user() {
        throw new UnsupportedOperationException(
            "Identity#user() is not supported by Netbout REST API"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity")
            .xpath("/page/identity/name")
            .get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        final URI uri = this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .location();
        return new RestBout(this.client.clone(uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        final List<String> hrefs = this.client
            .queryParam("q", query)
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bouts")
            .xpath("/page/bouts/bout/@href");
        final List<Bout> bouts = new ArrayList<Bout>();
        for (String href : hrefs) {
            bouts.add(new RestBout(this.client.clone(href)));
        }
        return bouts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long num) {
        final String href = this.client
            .queryParam("q", String.format("bout:%s", num))
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(String.format("/page/bouts/bout[number='%d']", num))
            .xpath(String.format("/page/bouts/bout[number='%d']/@href", num))
            .get(0);
        return new RestBout(this.client.clone(href));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        final String href = this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity/photo[.!='']")
            .xpath("/page/identity/photo")
            .get(0);
        try {
            return new URL(href);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(URL photo) {
        throw new UnsupportedOperationException(
            "Identity#setPhoto() is not implemented yet"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        final List<String> names = this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity/aliases")
            .xpath("/page/identity/aliases/alias");
        return new HashSet(names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        throw new UnsupportedOperationException(
            "Identity#alias() is not implemented yet"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invited(final Bout bout) {
        throw new IllegalArgumentException(
            "Identity#invited() shouldn't be called on REST API"
        );
    }

}
