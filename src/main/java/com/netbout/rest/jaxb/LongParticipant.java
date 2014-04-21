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

import com.netbout.spi.Helper;
import com.netbout.spi.Participant;
import com.netbout.spi.Profile;
import com.rexsl.page.Link;
import java.util.Collection;
import java.util.LinkedList;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Participant convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "participant")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongParticipant {

    /**
     * The bout.
     */
    private transient Participant participant;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Participant viewer;

    /**
     * Public ctor for JAXB.
     */
    public LongParticipant() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param dude The participant
     * @param bldr The builder
     * @param vwr The viewer
     */
    public LongParticipant(final Participant dude, final UriBuilder bldr,
        final Participant vwr) {
        this.participant = dude;
        this.builder = bldr;
        this.viewer = vwr;
    }

    /**
     * Get kick-off link.
     * @return The link
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    public Collection<Link> getLinks() {
        final Collection<Link> links = new LinkedList<Link>();
        if (this.viewer.leader() && !this.viewer.equals(this.participant)) {
            links.add(
                new Link(
                    "kickoff",
                    this.builder.clone()
                        .path("/kickoff")
                        .replaceQueryParam("name", "{name}")
                        .build(this.participant.name())
                )
            );
        }
        return links;
    }

    /**
     * Get its identity.
     * @return The name
     */
    @XmlElement
    public String getIdentity() {
        return this.participant.name().toString();
    }

    /**
     * Get his alias.
     * @return The alias
     */
    @XmlElement
    public String getAlias() {
        return new Profile.Conventional(this.participant)
            .aliases()
            .iterator()
            .next();
    }

    /**
     * Get its photo.
     * @return The photo
     */
    @XmlElement
    public Photo getPhoto() {
        return new Photo(this.participant, this.builder);
    }

    /**
     * Is he confirmed?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isConfirmed() {
        return this.participant.confirmed();
    }

    /**
     * Is he a leader?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isLeader() {
        return this.participant.leader();
    }

    /**
     * Is it a helper?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isHelper() {
        return this.participant instanceof Helper;
    }

    /**
     * Is it me?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isMe() {
        return this.participant.equals(this.viewer);
    }

}
