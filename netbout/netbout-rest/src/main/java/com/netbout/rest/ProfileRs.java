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

import com.netbout.rest.jaxb.Namespace;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.jaxb.LongProfile;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Identity;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * User profile.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/pf")
public final class ProfileRs extends AbstractRs {

    /**
     * The profile page.
     * @return The JAX-RS response
     */
    @GET
    public Response front() {
        final LongProfile profile = new LongProfile(
            this.self(),
            this.identity()
        );
        return new PageBuilder()
            .stylesheet("/xsl/profile.xsl")
            .build(AbstractPage.class)
            .init(this)
            .link("promote", this.base().path("/pf/promote"))
            // @checkstyle MultipleStringLiterals (2 lines)
            .link("namespaces", this.base().path("/pf/namespaces"))
            .append(JaxbGroup.build(this.namespaces(), "namespaces"))
            .append(profile)
            .render()
            .authenticated(this.identity())
            .build();
    }

    /**
     * Switch to another language.
     * @param locale The locale to switch to
     * @return The JAX-RS response
     */
    @GET
    @Path("/toggle")
    public Response toggle(@QueryParam("l") final String locale) {
        if (locale == null) {
            throw new ForwardException(
                this,
                this.self(),
                "Query param 'l' missed (with locale to set)"
            );
        }
        new LongProfile(this.self(), this.identity()).setLocale(locale);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(this.identity())
            .entity(String.format("switched to '%s'", locale))
            .status(Response.Status.SEE_OTHER)
            .location(this.self().build())
            .build();
    }

    /**
     * Location of myself.
     * @return The location, its builder actually
     */
    private UriBuilder self() {
        return this.base().path("/pf");
    }

    /**
     * Promote this identity to helper.
     * @param addr The URL of the helper
     * @return The JAX-RS response
     */
    @POST
    @Path("/promote")
    public Response promote(@FormParam("url") final String addr) {
        if (addr == null) {
            throw new ForwardException(this, this.self(), "'url' missed");
        }
        URL url;
        try {
            url = new URL(addr);
        } catch (java.net.MalformedURLException ex) {
            throw new ForwardException(this, this.self(), ex);
        }
        final Identity identity = this.identity();
        this.hub().promote(identity, url);
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.self().build())
            .build();
    }

    /**
     * Register these namespaces.
     * @param text List of namespaces and URLs
     * @return The JAX-RS response
     */
    @POST
    @Path("/namespaces")
    public Response register(@FormParam("text") final String text) {
        if (text == null) {
            throw new ForwardException(this, this.self(), "'text' missed");
        }
        final Identity identity = this.identity();
        for (String line : StringUtils.split(text, "\n")) {
            final String[] parts = StringUtils.split(line, "=", 2);
            this.hub().resolver().register(
                identity,
                StringUtils.trim(parts[0]),
                StringUtils.trim(parts[1])
            );
        }
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.self().build())
            .build();
    }

    /**
     * Get list of my namespaces.
     * @return The collection of them
     */
    @SuppressWarnings(
        { "PMD.AvoidInstantiatingObjectsInLoops", "PMD.UseConcurrentHashMap" }
    )
    private Collection<Namespace> namespaces() {
        final Collection<Namespace> namespaces = new ArrayList<Namespace>();
        final Map<String, String> map = this.hub().resolver()
            .registered(this.identity());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            namespaces.add(new Namespace(entry.getKey(), entry.getValue()));
        }
        return namespaces;
    }

}
