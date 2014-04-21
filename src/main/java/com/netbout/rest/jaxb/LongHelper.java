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

import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import java.net.URL;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper, convertable to XML.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "identity")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongHelper extends LongIdentity {

    /**
     * The helper.
     */
    private final transient Helper helper;

    /**
     * Public ctor for JAXB.
     */
    public LongHelper() {
        super();
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param identity The identity
     * @param hlp The helper
     * @param bldr Builder of home URI
     */
    public LongHelper(final Identity identity, final Helper hlp,
        final UriBuilder bldr) {
        super(identity, bldr);
        this.helper = hlp;
    }

    /**
     * Is it a helper?
     * @return The flag
     */
    @XmlAttribute
    public Boolean getHelper() {
        return true;
    }

    /**
     * List of supported operations, if it's a helper.
     * @return The list
     */
    @XmlElement(name = "operation")
    @XmlElementWrapper(name = "supports")
    public Set<String> getSupports() {
        return this.helper.supports();
    }

    /**
     * Get location of the helper.
     * @return The name
     */
    @XmlElement
    public URL getLocation() {
        return this.helper.location();
    }

}
