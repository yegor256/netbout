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

import com.netbout.spi.xml.DomParser;
import com.netbout.spi.xml.DomPrinter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Rendered content.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "render")
@XmlAccessorType(XmlAccessType.NONE)
final class RawXml {

    /**
     * The XML to show.
     */
    private final transient DomParser xml;

    /**
     * Public ctor for JAXB.
     */
    public RawXml() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param dom The source
     */
    public RawXml(final DomParser dom) {
        this.xml = dom;
    }

    /**
     * Get text.
     * @return The text
     */
    @XmlValue
    public String getText() {
        return StringEscapeUtils.escapeXml(
            new DomPrinter(this.xml.parse()).print()
        );
    }

    /**
     * Get XML namespace.
     * @return The namespace
     */
    @XmlAttribute
    public String getNamespace() {
        String namespace;
        try {
            namespace = this.xml.namespace().toString();
        } catch (com.netbout.spi.xml.DomValidationException ex) {
            namespace = ex.getMessage();
        }
        return namespace;
    }

    /**
     * Get root element name.
     * @return The name
     */
    @XmlAttribute
    public String getName() {
        return this.xml.parse().getDocumentElement().getTagName();
    }

}
