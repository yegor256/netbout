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

import com.netbout.rest.page.JaxbBundle;
import com.netbout.spi.Identity;
import com.rexsl.core.Manifests;
import com.rexsl.core.XslResolver;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractPage implements Page {

    /**
     * Home resource of this page.
     */
    private Resource home;

    /**
     * When this page was started to build.
     */
    private final long start;

    /**
     * Collection of elements.
     */
    private final Collection elements = new ArrayList();

    /**
     * Public ctor.
     */
    public AbstractPage() {
        this.start = System.nanoTime();
    }

    /**
     * Initializer.
     * @param res Home of this page
     * @return This object
     */
    public final Page init(final Resource res) {
        this.home = res;
        this.append(
            new JaxbBundle("links")
                .add(Page.HATEOAS_LINK)
                    .attr(Page.HATEOAS_NAME, "self")
                    .attr(
                        Page.HATEOAS_HREF,
                        this.home.uriInfo().getAbsolutePath()
                )
                .up()
                .add(Page.HATEOAS_LINK)
                    .attr(Page.HATEOAS_NAME, "home")
                    .attr(
                        Page.HATEOAS_HREF,
                        this.home.uriInfo()
                            .getAbsolutePathBuilder()
                            .replacePath("/")
                            .build()
                )
                .up()
                .add(Page.HATEOAS_LINK)
                    .attr(Page.HATEOAS_NAME, "start")
                    .attr(
                        Page.HATEOAS_HREF,
                        this.home.uriInfo()
                            .getAbsolutePathBuilder()
                            .replacePath("/s")
                            .build()
                )
                .up()
                .add(Page.HATEOAS_LINK)
                    .attr(Page.HATEOAS_NAME, "login")
                    .attr(
                        Page.HATEOAS_HREF,
                        this.home.uriInfo()
                            .getAbsolutePathBuilder()
                            .replacePath("/g")
                            .build()
                )
                .up()
                .add(Page.HATEOAS_LINK)
                    .attr(Page.HATEOAS_NAME, "logout")
                    .attr(
                        Page.HATEOAS_HREF,
                        this.home.uriInfo()
                            .getAbsolutePathBuilder()
                            .replacePath("/g/out")
                            .build()
                )
                .up()
        );
        this.append(
            new JaxbBundle("version")
                .add("name", Manifests.read("Netbout-Version"))
                .up()
                .add("revision", Manifests.read("Netbout-Revision"))
                .up()
                .add("date", Manifests.read("Netbout-Date"))
                .up()
        );
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
    public final Response.ResponseBuilder authenticated(
        final Identity identity) {
        this.append(identity);
        return Response.ok()
            .entity(this)
            .cookie(
                new NewCookie(
                    "netbout",
                    new Cryptor(this.home.entry()).encrypt(identity)
                )
            )
            .type(MediaType.APPLICATION_XML);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Response.ResponseBuilder anonymous() {
        return Response.ok()
            .entity(this)
            .type(MediaType.APPLICATION_XML);
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
     * Get time of page generation.
     * @return Time in microseconds
     */
    @XmlAttribute
    public final Long getMcs() {
        // @checkstyle MagicNumber (1 line)
        return (System.nanoTime() - this.start) / 1000L;
    }

}
