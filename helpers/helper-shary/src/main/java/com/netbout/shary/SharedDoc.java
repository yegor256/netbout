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
package com.netbout.shary;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * Shared document.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "doc")
@XmlAccessorType(XmlAccessType.NONE)
public final class SharedDoc {

    /**
     * The slip of it.
     */
    private final transient Slip slip;

    /**
     * The alias of the author.
     */
    private transient String alias;

    /**
     * Links.
     */
    private final transient Collection<Link> links = new ArrayList<Link>();

    /**
     * Public ctor, for JAXB.
     */
    public SharedDoc() {
        throw new IllegalStateException("invalid call");
    }

    /**
     * Public ctor.
     * @param slp The slip of it
     */
    public SharedDoc(final Slip slp) {
        this.slip = slp;
    }

    /**
     * Get name of the document.
     * @return The name
     */
    @XmlElement(name = "name")
    public String getName() {
        return this.slip.getName();
    }

    /**
     * Get media type of document.
     * @return The type of it
     */
    @XmlElement(name = "type")
    public String getType() {
        return this.slip.getType();
    }

    /**
     * Get author's name.
     * @return The author of it
     */
    @XmlElement(name = "author")
    public String getAuthor() {
        return this.slip.getAuthor();
    }

    /**
     * Get author's alias.
     * @return The alias of it
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Set author's alias.
     * @param txt The alias of it
     */
    @XmlElement(name = "alias")
    public void setAlias(final String txt) {
        this.alias = txt;
    }

    /**
     * Get links.
     * @return The links
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    public Collection<Link> getLinks() {
        return this.links;
    }

    /**
     * Add new link.
     * @param link The link to add
     */
    public void add(final Link link) {
        this.links.add(link);
    }

    /**
     * Get URI of the document.
     * @return The URI of it
     */
    public String getUri() {
        return this.slip.getRawUri();
    }

}
