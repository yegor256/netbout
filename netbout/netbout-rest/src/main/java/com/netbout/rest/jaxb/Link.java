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
package com.netbout.rest.jaxb;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HATEOAS link.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.NONE)
public final class Link {

    /**
     * Rel name.
     */
    private final transient String rel;

    /**
     * Optional label.
     */
    private final transient String label;

    /**
     * The URI.
     */
    private final transient URI href;

    /**
     * The type of resource there.
     */
    private final transient String type;

    /**
     * Public ctor for JAXB.
     */
    public Link() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param uri The href
     */
    public Link(final String rname, final URI uri) {
        this(rname, "", uri, MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param builder URI builder
     */
    public Link(final String rname, final UriBuilder builder) {
        this(rname, "", builder.build(), MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param name The label of it
     * @param builder URI builder
     */
    public Link(final String rname, final String name,
        final UriBuilder builder) {
        this(rname, name, builder.build(), MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param name The label of it
     * @param uri URI
     */
    public Link(final String rname, final String name, final URI uri) {
        this(rname, name, uri, MediaType.TEXT_XML);
    }

    /**
     * Public ctor.
     * @param rname The "rel" of it
     * @param name The label of it
     * @param uri The href
     * @param tpe Media type of destination
     * @checkstyle ParameterNumber (3 lines)
     */
    public Link(final String rname, final String name, final URI uri,
        final String tpe) {
        assert rname != null;
        assert name != null;
        assert uri != null;
        assert tpe != null;
        this.rel = rname;
        this.label = name;
        this.href = uri;
        this.type = tpe;
    }

    /**
     * REL of the link.
     * @return The name
     */
    @XmlAttribute
    public String getRel() {
        return this.rel;
    }

    /**
     * Label of the link.
     * @return The label
     */
    @XmlAttribute
    public String getLabel() {
        String text = null;
        if (!this.label.isEmpty()) {
            text = this.label;
        }
        return text;
    }

    /**
     * HREF of the link.
     * @return The url
     */
    @XmlAttribute
    public URI getHref() {
        return this.href;
    }

    /**
     * Type of destination resource.
     * @return The type
     */
    @XmlAttribute
    public String getType() {
        return this.type;
    }

}
