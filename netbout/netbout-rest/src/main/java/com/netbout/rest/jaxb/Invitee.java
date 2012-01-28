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

import com.netbout.spi.Identity;
import com.netbout.spi.NetboutUtils;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Invitee for a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "invitee")
@XmlAccessorType(XmlAccessType.NONE)
public final class Invitee {

    /**
     * The original identity.
     */
    private final transient Identity identity;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * Public ctor for JAXB.
     */
    public Invitee() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param idnt Parent identity to refer to
     * @param bldr Uri builder
     */
    public Invitee(final Identity idnt, final UriBuilder bldr) {
        this.identity = idnt;
        this.builder = bldr;
    }

    /**
     * HREF of the invitee.
     * @return The url
     */
    @XmlAttribute
    public String getHref() {
        return this.builder
            .path("/i")
            .queryParam("name", "{name}")
            .build(this.identity.name())
            .toString();
    }

    /**
     * Get his alias.
     * @return The alias
     */
    @XmlElement
    public String getAlias() {
        return NetboutUtils.aliasOf(this.identity);
    }

    /**
     * JAXB related method, to return the name.
     * @return The name
     */
    @XmlElement
    public String getName() {
        return this.identity.name().toString();
    }

    /**
     * JAXB related method, to return the photo of the identity.
     * @return The photo
     */
    @XmlElement
    public URL getPhoto() {
        return this.identity.photo();
    }

}
