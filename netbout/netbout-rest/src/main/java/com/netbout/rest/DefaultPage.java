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

import com.rexsl.core.XslResolver;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
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
public final class DefaultPage implements Page {

    /**
     * Link element.
     */
    public static final String HATEOAS_LINK = "link";

    /**
     * Name attribute.
     */
    public static final String HATEOAS_NAME = "name";

    /**
     * HREF attribute.
     */
    public static final String HATEOAS_HREF = "href";

    /**
     * Home resource of this page.
     */
    private final Resource home;

    /**
     * Collection of elements.
     */
    private final Collection elements = new ArrayList();

    /**
     * Public ctor for JAXB, should never be called.
     */
    public DefaultPage() {
        throw new IllegalStateException("#DefaultPage(): illegal call");
    }

    /**
     * Public ctor.
     * @param res Home of this page
     * @see PageBuilder#build(Resource,String)
     */
    public DefaultPage(final Resource res) {
        this.home = res;
        this.append(
            new JaxbBundle("links")
                .add(this.HATEOAS_LINK)
                    .attr(this.HATEOAS_NAME, "self")
                    .attr(
                        this.HATEOAS_HREF,
                        this.home.uriInfo().getAbsolutePath()
                )
                .up()
                .add(this.HATEOAS_LINK)
                    .attr(this.HATEOAS_NAME, "home")
                    .attr(
                        this.HATEOAS_HREF,
                        this.home.uriInfo()
                            .getAbsolutePathBuilder()
                            .replacePath("/")
                            .build()
                )
                .up()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page append(final Object element) {
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
    public Page append(final JaxbBundle bundle) {
        this.append(bundle.element());
        return this;
    }

    /**
     * Get all elements.
     * @return Full list of injected elements
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public Collection<Object> getElements() {
        return this.elements;
    }

}
