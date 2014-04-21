/**
 * Copyright (c) 2009-2014, Netbout.com
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Participant convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
public final class Photo {

    /**
     * Use original photo.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Original {
    }

    /**
     * The friend.
     */
    private transient Friend friend;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * Public ctor for JAXB.
     */
    public Photo() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param dude The friend
     * @param bldr The builder
     */
    public Photo(final Friend dude, final UriBuilder bldr) {
        this.friend = dude;
        this.builder = bldr;
    }

    /**
     * Get its photo.
     * @return The photo
     * @todo #494 This is a workaround, the builder is not used.
     */
    @XmlValue
    public URI getPhoto() {
        assert this.builder != null;
        URI photo;
        if ("test".equals(this.friend.name().nid())
            || this.friend.getClass().isAnnotationPresent(
                Photo.Original.class
            )) {
            try {
                photo = this.friend.profile().photo().toURI();
            } catch (java.net.URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            photo = UriBuilder.fromPath("/f/photo")
                .queryParam("urn", "{urn}")
                .build(this.friend.name());
        }
        return photo;
    }

}
