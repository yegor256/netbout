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
package com.netbout.rest;

import com.netbout.rest.auth.FacebookRs;
import com.netbout.rest.jaxb.Link;
import com.netbout.rest.jaxb.LongHelper;
import com.netbout.rest.jaxb.LongIdentity;
import com.netbout.rest.jaxb.Nano;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.client.RestSession;
import com.rexsl.core.Manifests;
import com.rexsl.core.XslResolver;
import com.rexsl.page.JaxbBundle;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * BasePage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @todo #254 Somehow we should specify PORT in the cookie. Without this param
 *  the site doesn't work in localhost:9099 in Chrome. Works fine in Safari,
 *  but not in Chrome. see http://stackoverflow.com/questions/1612177
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("PMD.TooManyMethods")
public class BasePage {

    /**
     * Home resource of this page.
     */
    private transient Resource home;

    /**
     * Is this page searcheable?
     */
    private transient boolean searcheable;

    /**
     * The response builder to return.
     */
    private final transient Response.ResponseBuilder builder = Response.ok();

    /**
     * Collection of elements.
     */
    private final transient Collection elements = new LinkedList();

    /**
     * Collection of links.
     */
    private final transient Collection<Link> links = new LinkedList<Link>();

    /**
     * Collection of log events.
     */
    private transient Collection<String> log;

    /**
     * Initializer.
     * @param res Home of this page
     * @param srch Is this page searcheable?
     * @return This object
     */
    public final BasePage init(final Resource res, final boolean srch) {
        this.home = res;
        this.searcheable = srch;
        this.link(
            "self",
            UriBuilder.fromUri(this.home.uriInfo().getRequestUri())
        );
        this.link("home", this.home.base());
        return this;
    }

    /**
     * Add new link by name and HREF.
     * @param name The name
     * @param href The link
     * @return This object
     */
    public final BasePage link(final String name, final String href) {
        this.links.add(
            new Link(
                name,
                this.home.base().path(href).build()
            )
        );
        return this;
    }

    /**
     * Add new link by name and HREF.
     * @param name The name
     * @param uri The link
     * @return This object
     */
    public final BasePage link(final String name, final UriBuilder uri) {
        this.links.add(new Link(name, uri));
        return this;
    }

    /**
     * Append new element.
     * @param element The element to append
     * @return This object
     */
    public final BasePage append(final Object element) {
        this.elements.add(element);
        if (!(element instanceof org.w3c.dom.Element)) {
            final XslResolver resolver = (XslResolver) this.home.providers()
                .getContextResolver(
                    Marshaller.class,
                    MediaType.APPLICATION_XML_TYPE
                );
            resolver.add(element.getClass());
        }
        return this;
    }

    /**
     * Add new element.
     * @param bundle The element
     * @return This object
     */
    public final BasePage append(final JaxbBundle bundle) {
        this.append(bundle.element());
        return this;
    }

    /**
     * Render it.
     * @return This object
     */
    public final BasePage render() {
        this.builder.entity(this);
        this.log = this.home.log().events();
        this.home.log().clear();
        return this;
    }

    /**
     * Add authentication information.
     * @param identity The user
     * @return This object
     */
    public final Response.ResponseBuilder authenticated(
        final Identity identity) {
        if (identity instanceof Helper) {
            this.append(new LongHelper(identity, (Helper) identity));
        } else {
            this.append(new LongIdentity(identity));
        }
        this.append(new JaxbBundle("auth", new Cryptor().encrypt(identity)));
        this.link("logout", "/g/out");
        this.link("profile", "/pf");
        if (this.trusted(identity)) {
            this.link("start", "/s");
        } else {
            this.link("re-login", "/g/re");
        }
        this.extend();
        final URI base = this.home.base().build();
        return this.builder
            .header(
                HttpHeaders.SET_COOKIE,
                this.nocookie(RestSession.MESSAGE_COOKIE)
        )
            .cookie(
                new CookieBuilder(base)
                    .named(RestSession.LOG_COOKIE)
                    .valued(this.home.log().toString())
                    .temporary()
                    .build()
            )
            .cookie(
                new CookieBuilder(base)
                    .named(RestSession.AUTH_COOKIE)
                    .valued(new Cryptor().encrypt(identity))
                    .temporary()
                    .build()
            )
            .type(MediaType.TEXT_XML);
    }

    /**
     * It's anonymous.
     * @return This object
     */
    public final Response.ResponseBuilder anonymous() {
        this.link("login", "/g");
        this.extend();
        return this.builder
            .header(
                HttpHeaders.SET_COOKIE,
                this.nocookie(RestSession.MESSAGE_COOKIE)
        )
            .header(
                HttpHeaders.SET_COOKIE,
                this.nocookie(RestSession.LOG_COOKIE)
            )
            .header(
                HttpHeaders.SET_COOKIE,
                this.nocookie(RestSession.AUTH_COOKIE)
            )
            .type(MediaType.TEXT_XML);
    }

    /**
     * Preserve authentication status.
     * @return This object
     */
    public final Response.ResponseBuilder preserved() {
        Response.ResponseBuilder bldr;
        try {
            bldr = this.authenticated(this.home.identity());
        } catch (LoginRequiredException ex) {
            bldr = this.anonymous();
        }
        return bldr;
    }

    /**
     * Get all elements.
     * @return Full list of injected elements
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public final Collection<Object> getElements() {
        return this.elements;
    }

    /**
     * Get all links.
     * @return Full list of links
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    public final Collection<Link> getLinks() {
        return this.links;
    }

    /**
     * Get all log events.
     * @return Full list of events
     */
    @XmlElement(name = "event")
    @XmlElementWrapper(name = "log")
    public final Collection<String> getLog() {
        return this.log;
    }

    /**
     * Get time of page generation, in nanoseconds.
     * @return Time in nanoseconds
     */
    @XmlElement
    public final Nano getNano() {
        return new Nano(this.home.nano());
    }

    /**
     * Get time of page generation.
     * @return Time in ISO 8601
     */
    @XmlAttribute
    public final Date getTime() {
        return new Date();
    }

    /**
     * Get IP address of the server.
     * @return The IP address
     */
    @XmlAttribute
    public final String getIp() {
        return String.format(
            "%s:%d",
            this.home.httpServletRequest().getLocalAddr(),
            this.home.httpServletRequest().getLocalPort()
        );
    }

    /**
     * Is this page searcheable?
     * @return Yes or no
     */
    @XmlAttribute
    public final boolean isSearcheable() {
        return this.searcheable;
    }

    /**
     * Can we fully trust this guy or he should re-login?
     * @param identity The person
     * @return Trusted?
     * @todo #249 We should find a better place for this method, and its
     *  implementation is just a skeleton for now. We should implement it
     *  somehow properly.
     */
    public static boolean trusted(final Identity identity) {
        final String nid = identity.name().nid();
        return nid.equals(FacebookRs.NAMESPACE)
            || "test".equals(nid)
            || "netbout".equals(nid);
    }

    /**
     * Extend page with mandatory elements.
     */
    private void extend() {
        this.append(
            new JaxbBundle("version")
                .add("name", Manifests.read("Netbout-Version"))
                .up()
                // @checkstyle MultipleStringLiterals (1 line)
                .add("revision", Manifests.read("Netbout-Revision"))
                .up()
                .add("date", Manifests.read("Netbout-Date"))
                .up()
        );
        this.append(new JaxbBundle("message", this.home.message()));
    }

    /**
     * Create header that cleans cookie with the given name.
     * @param name Name of the cookie
     * @return Value of the HTTP header
     */
    private String nocookie(final String name) {
        return new CookieBuilder(this.home.base().build())
            .named(name)
            .pathed(
                String.format(
                    "/%s",
                    this.home.httpServletRequest().getContextPath()
                )
            )
            .build()
            .toString();
    }

}
