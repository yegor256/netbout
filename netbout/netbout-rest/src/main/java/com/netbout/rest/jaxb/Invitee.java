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
 * this code accidentally and without intent to use it, please report this
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

import com.netbout.spi.Friend;
import com.netbout.spi.Profile;
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
    private final transient Friend friend;

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
     * @param frnd Parent identity to refer to
     * @param bldr Uri builder
     */
    public Invitee(final Friend frnd, final UriBuilder bldr) {
        this.friend = frnd;
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
            .replaceQueryParam("name", "{name}")
            .build(this.friend.name())
            .toString();
    }

    /**
     * Get his alias.
     * @return The alias
     */
    @XmlElement
    public String getAlias() {
        return new Profile.Conventional(this.friend)
            .aliases()
            .iterator()
            .next();
    }

    /**
     * JAXB related method, to return the name.
     * @return The name
     */
    @XmlElement
    public String getName() {
        return this.friend.name().toString();
    }

    /**
     * JAXB related method, to return the photo of the identity.
     * @return The photo
     */
    @XmlElement
    public URL getPhoto() {
        return this.friend.profile().photo();
    }

}
