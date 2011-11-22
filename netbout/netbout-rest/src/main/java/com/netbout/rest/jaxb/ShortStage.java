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

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Short version of a stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "stage")
@XmlAccessorType(XmlAccessType.NONE)
public final class ShortStage {

    /**
     * The name of the identity.
     */
    private transient String identity;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * Public ctor for JAXB.
     */
    public ShortStage() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param name The identity
     * @param bldr URI builder
     */
    private ShortStage(final String name, final UriBuilder bldr) {
        this.identity = name;
        this.builder = bldr;
    }

    /**
     * Builder.
     * @param name The identity
     * @param bldr URI builder
     * @return The instance just created
     */
    public static ShortStage build(final String name, final UriBuilder bldr) {
        return new ShortStage(name, bldr);
    }

    /**
     * HREF of the stage.
     * @return The url
     */
    @XmlAttribute
    public String getHref() {
        return this.builder
            .queryParam("stage", this.identity)
            .build()
            .toString();
    }

    /**
     * Name of identity.
     * @return The number
     */
    @XmlAttribute
    public String getName() {
        return this.identity;
    }

}
