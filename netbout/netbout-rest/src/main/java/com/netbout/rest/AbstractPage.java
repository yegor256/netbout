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

import com.netbout.rest.auth.FacebookRs;
import com.netbout.rest.jaxb.Link;
import com.netbout.rest.jaxb.LongHelper;
import com.netbout.rest.jaxb.LongIdentity;
import com.netbout.rest.jaxb.Nano;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.client.RestSession;
import com.rexsl.core.Manifests;
import com.rexsl.core.XslResolver;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
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
 * Page.
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
public abstract class AbstractPage implements Page {

    /**
     * Home resource of this page.
     */
    private transient Resource home;

    /**
     * The response builder to return.
     */
    private final transient Response.ResponseBuilder builder = Response.ok();

    /**
     * Collection of elements.
     */
    private final transient Collection elements = new ArrayList();

    /**
     * Collection of links.
     */
    private final transient Collection<Link> links = new ArrayList<Link>();

    /**
     * Collection of log events.
     */
    private transient Collection<String> log;

    /**
     * Initializer.
     * @param res Home of this page
     * @return This object
     */
    public final Page init(final Resource res) {
        this.home = res;
        this.link("self", this.home.uriInfo().getAbsolutePathBuilder());
        this.link("home", this.home.base());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Page link(final String name, final String href) {
        this.links.add(
            new Link(
                name,
                this.home.base().path(href).build()
            )
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Page link(final String name, final UriBuilder uri) {
        this.links.add(new Link(name, uri));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Page append(final Object element) {
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
     * {@inheritDoc}
     */
    @Override
    public final Page append(final JaxbBundle bundle) {
        this.append(bundle.element());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Page render() {
        this.builder.entity(this);
        this.log = this.home.log().events();
        this.home.log().clear();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                new NewCookie(
                    RestSession.LOG_COOKIE,
                    this.home.log().toString(),
                    base.getPath(),
                    base.getHost(),
                    // @checkstyle MultipleStringLiterals (1 line)
                    Integer.valueOf(Manifests.read("Netbout-Revision")),
                    "Netbout.com log",
                    // @checkstyle MagicNumber (1 line)
                    60 * 60 * 24 * 90,
                    false
                )
            )
            .cookie(
                new NewCookie(
                    RestSession.AUTH_COOKIE,
                    new Cryptor().encrypt(identity),
                    base.getPath(),
                    base.getHost(),
                    // @checkstyle MultipleStringLiterals (1 line)
                    Integer.valueOf(Manifests.read("Netbout-Revision")),
                    "Netbout.com logged-in user",
                    // @checkstyle MagicNumber (1 line)
                    60 * 60 * 24 * 90,
                    false
                )
            )
            .type(MediaType.TEXT_XML);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
        final URI base = this.home.base().build();
        return String.format(
            // @checkstyle LineLength (1 line)
            "%s=deleted;Domain=%s;Path=/%s;Expires=Thu, 01-Jan-1970 00:00:01 GMT",
            name,
            base.getHost(),
            this.home.httpServletRequest().getContextPath()
        );
    }

}
