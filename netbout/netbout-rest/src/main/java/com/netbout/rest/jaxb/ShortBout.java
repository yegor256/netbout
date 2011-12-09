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

import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Short version of a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "bout")
@XmlAccessorType(XmlAccessType.NONE)
public final class ShortBout {

    /**
     * The original bout.
     */
    private transient Bout bout;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * Public ctor for JAXB.
     */
    public ShortBout() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param parent Parent bout to refer to
     * @param bldr URI builder
     */
    public ShortBout(final Bout parent, final UriBuilder bldr) {
        this.bout = parent;
        this.builder = bldr;
    }

    /**
     * HREF of the bout.
     * @return The url
     */
    @XmlAttribute
    public String getHref() {
        return this.builder
            .path("/{num}")
            .build(this.bout.number())
            .toString();
    }

    /**
     * JAXB related method, to return the number of the bout.
     * @return The number
     */
    @XmlElement
    public Long getNumber() {
        return this.bout.number();
    }

    /**
     * JAXB related method, to return the title of the bout.
     * @return The title
     */
    @XmlElement
    public String getTitle() {
        return this.bout.title();
    }

    /**
     * JAXB related method, to return participants of the bout.
     * @return The collection
     */
    @XmlElement(name = "participant")
    @XmlElementWrapper(name = "participants")
    public Collection<LongParticipant> getParticipants() {
        final Collection<LongParticipant> dudes =
            new ArrayList<LongParticipant>();
        for (Participant dude : this.bout.participants()) {
            dudes.add(new LongParticipant(dude, this.builder));
        }
        return dudes;
    }

    /**
     * How many messages are there?
     * @return Total number of messages
     */
    @XmlAttribute(name = "messages")
    public Integer getTotalNumberOfMessages() {
        return this.bout.messages("").size();
    }

    /**
     * How many seen messages are there?
     * @return Total number of messages which were already seen
     */
    @XmlAttribute(name = "seen")
    public Integer getTotalNumberOfSeenMessages() {
        Integer count = 0;
        for (Message msg : this.bout.messages("")) {
            if (msg.seen()) {
                count += 1;
            }
        }
        return count;
    }

}
