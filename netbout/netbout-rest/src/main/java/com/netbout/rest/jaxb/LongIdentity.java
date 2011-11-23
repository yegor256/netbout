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
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Identity convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "identity")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongIdentity {

    /**
     * The identity.
     */
    private transient Identity identity;

    /**
     * Public ctor for JAXB.
     */
    public LongIdentity() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param idnt The identity
     */
    public LongIdentity(final Identity idnt) {
        this.identity = idnt;
    }

    /**
     * Get name.
     * @return The name
     */
    @XmlElement
    public String getName() {
        return this.identity.name();
    }

    /**
     * Get his alias.
     * @return The alias
     */
    @XmlElement
    public String getAlias() {
        return new AliasBuilder(this.identity).build();
    }

    /**
     * Get photo.
     * @return The photo
     */
    @XmlElement
    public String getPhoto() {
        return this.identity.photo().toString();
    }

    /**
     * List of aliases.
     * @return The list
     */
    @XmlElement(name = "alias")
    @XmlElementWrapper(name = "aliases")
    public Collection<String> getAliases() {
        return this.identity.aliases();
    }

}
