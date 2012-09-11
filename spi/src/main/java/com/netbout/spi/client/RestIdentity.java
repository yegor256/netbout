/**
 * Copyright (c) 2009-2012, Netbout.com
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
import com.netbout.spi.Profile;
import com.netbout.spi.Urn;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Identity identity) {
        return this.name().compareTo(identity.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((obj instanceof Identity)
            && this.name().equals(((Identity) obj).name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("RestIdentity(%s)", this.name());
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
    public Long eta() {
        return Long.valueOf(
            this.client
                .get("reading ETA value of identity")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/identity/eta")
                .xpath("/page/identity/eta/text()")
                .get(0)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        try {
            return new URL(
                this.client
                    .get("reading authority of identity")
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertXPath("/page/identity/authority")
                    .xpath("/page/identity/authority/text()")
                    .get(0)
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return Urn.create(
            this.client
                .get("reading identity name")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/identity")
                .xpath("/page/identity/name/text()")
                .get(0)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        final URI uri = this.client
            .get("reading 'start' rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='start']")
            .rel("start")
            .get("starting new bout")
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .location();
        return new RestBout(this.client.copy(uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<Bout> inbox(final String query) {
        final List<String> hrefs = this.client
            .queryParam(RestSession.QUERY_PARAM, query)
            .queryParam(RestSession.BUNDLE_PARAM, "")
            .get(String.format("reading bouts in the inbox '%s'", query))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bouts")
            .xpath("/page/bouts/bout/link[@rel='page']/@href");
        final List<Bout> bouts = new ArrayList<Bout>();
        for (String href : hrefs) {
            bouts.add(new RestBout(this.client.copy(href)));
        }
        return Collections.unmodifiableList(bouts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long num) {
        final String href = this.client
            .queryParam(RestSession.BUNDLE_PARAM, "")
            .queryParam(
                RestSession.QUERY_PARAM,
                String.format("(equal $bout.number %d)", num)
            )
            .get(String.format("reading href of bout #%d", num))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(String.format("/page/bouts/bout[number='%d']", num))
            .xpath(
                String.format(
                    "/page/bouts/bout[number='%d']/link[@rel='page']/@href",
                    num
                )
            )
            .get(0);
        return new RestBout(this.client.copy(href));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final Urn name) {
        return new Friend(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<Identity> friends(final String mask) {
        final List<String> names = this.client
            .get("reading 'friends' @rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='friends']")
            .rel("friends")
            .queryParam("mask", mask)
            .queryParam("bout", "1")
            .get(String.format("reading suggestions for '%s'", mask))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(String.format("/page/mask[.='%s']", mask))
            .assertXPath("/page/invitees")
            .xpath("/page/invitees/invitee/name/text()");
        final Set<Identity> friends = new HashSet<Identity>();
        for (String name : names) {
            friends.add(new Friend(Urn.create(name)));
        }
        return Collections.unmodifiableSet(friends);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        final String href = this.client
            .get("reading 'profle' @rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='profile']")
            .xpath("/page/links/link[@rel='profile']/@href")
            .get(0);
        return new RestProfile(this.client.copy(href));
    }

}
