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

import com.netbout.spi.xml.SchemaLocation;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Beginning of document sharing.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "Slip", namespace = Slip.NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
@SchemaLocation("http://www.netbout.com/ns/shary/Slip.xsd")
public final class Slip {

    /**
     * Namespace.
     */
    public static final String NAMESPACE = "urn:netbout:ns:shary/Slip";

    /**
     * Is it an allowing slip or declining?
     */
    private transient boolean allow;

    /**
     * Title of the document.
     */
    private transient String name;

    /**
     * URI of the document to share.
     */
    private transient String uri;

    /**
     * Author of the document.
     */
    private transient String author;

    /**
     * Public ctor, for JAXB.
     */
    public Slip() {
        // empty
    }

    /**
     * Public ctor.
     * @param flag Allow of disallow?
     * @param addr The address of the document
     * @param who The author
     * @param title The title of the document
     * @checkstyle ParameterNumber (3 lines)
     */
    public Slip(final boolean flag, final String addr, final String who,
        final String title) {
        this.allow = flag;
        this.uri = addr;
        this.author = who;
        this.name = title;
    }

    /**
     * Render it for a reader.
     * @return The text
     */
    public String render() {
        String text;
        if (this.allow) {
            text = String.format(
                "%s shared **\"%s\"** document with us.",
                this.name,
                this.author
            );
        } else {
            text = String.format(
                "%s decided not to share \"%s\" with us any more.",
                this.name,
                this.author
            );
        }
        return text;
    }

    /**
     * Get allowing flag.
     * @return Author allowed to see the document?
     */
    public boolean isAllow() {
        return this.allow;
    }

    /**
     * Set allowing flag.
     * @param flag The flag
     */
    @XmlElement(name = "allow", namespace = Slip.NAMESPACE)
    public void setAllow(final boolean flag) {
        this.allow = flag;
    }

    /**
     * Get URI.
     * @return The URI of the document
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Set URI.
     * @param addr The URI of the document
     */
    @XmlElement(name = "uri", namespace = Slip.NAMESPACE)
    public void setUri(final String addr) {
        this.uri = addr;
    }

    /**
     * Get author's name.
     * @return The URN of the author
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Get author's name.
     * @param urn The URN of the author
     */
    @XmlElement(name = "author", namespace = Slip.NAMESPACE)
    public void setAuthor(final String urn) {
        this.author = urn;
    }

    /**
     * Get title of the document to show to everybody.
     * @return The name of it (unique in the bout)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get title of the document to show to everybody.
     * @param txt The name of it (unique in the bout)
     */
    @XmlElement(name = "name", namespace = Slip.NAMESPACE)
    public void setName(final String txt) {
        this.name = txt;
    }

    /**
     * Calculate and return type of document to share.
     * @return The media type of it
     */
    public String getType() {
        return "text/plain";
    }

}
