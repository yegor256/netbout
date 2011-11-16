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
package com.netbout.hub;

import com.netbout.hub.data.BoutData;
import com.netbout.hub.data.MessageData;
import com.netbout.hub.data.ParticipantData;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "bout")
@XmlType(name = "bout")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({ HubMessage.class, HubParticipant.class })
public final class HubBout implements Bout {

    /**
     * The viewer.
     */
    private HubIdentity viewer;

    /**
     * The data.
     */
    private BoutData data;

    /**
     * Public ctor for JAXB.
     */
    public HubBout() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param idnt The viewer
     * @param dat The data
     */
    public HubBout(final HubIdentity idnt, final BoutData dat) {
        this.viewer = idnt;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * HREF of the bout. This is a dirty hack at the moment, and should
     * be refactored somehow. We shouldn't generate a link right here. We should
     * use information provided by the client of this module, in order to
     * build a real URL.
     * @return The url
     */
    @XmlAttribute
    public String getHref() {
        return String.format("/%d", this.number());
    }

    /**
     * JAXB related method, to return the number of the bout.
     * @return The number
     */
    @XmlElement
    public Long getNumber() {
        return this.number();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
    }

    /**
     * JAXB related method, to return the title of the bout.
     * @return The title
     */
    @XmlElement
    public String getTitle() {
        return this.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Participant invite(final Identity friend) {
        final ParticipantData dude =
            new ParticipantData(this.number(), friend.name());
        this.data.addParticipant(dude);
        dude.setConfirmed(false);
        Logger.debug(
            this,
            "#invite('%s'): success",
            friend
        );
        ((HubIdentity) friend).invited(this);
        return new HubParticipant(dude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        final Collection<Participant> participants
            = new ArrayList<Participant>();
        for (ParticipantData dude : this.data.getParticipants()) {
            participants.add(new HubParticipant(dude));
        }
        Logger.debug(
            this,
            "#participants(): %d participants found",
            participants.size()
        );
        return participants;
    }

    /**
     * JAXB related method, to return participants of the bout.
     * @return The collection
     */
    @XmlElement(name = "participant")
    @XmlElementWrapper(name = "participants")
    public Collection<HubParticipant> getParticipants() {
        return (Collection) this.participants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> messages(final String query) {
        final List<MessageData> datas =
            new ArrayList<MessageData>(this.data.getMessages());
        Collections.reverse(datas);
        final List<Message> messages = new ArrayList<Message>();
        for (MessageData msg : datas) {
            messages.add(new HubMessage(this.viewer, msg));
        }
        Logger.debug(
            this,
            "#messages('%s'): %d messages found",
            query,
            messages.size()
        );
        return messages;
    }

    /**
     * JAXB related method, to return messages of the bout.
     * @return The collection
     */
    @XmlElement(name = "message")
    @XmlElementWrapper(name = "messages")
    public Collection<HubMessage> getMessages() {
        return (Collection) this.messages("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        final MessageData msg = this.data.addMessage();
        msg.setDate(new Date());
        msg.setAuthor(this.viewer.name());
        msg.setText(text);
        Logger.debug(
            this,
            "#post('%s'): message posted",
            text
        );
        final Message message = new HubMessage(this.viewer, msg);
        message.text();
        return message;
    }

    /**
     * How many messages are there?
     * @return Total number of messages
     */
    @XmlAttribute(name = "messages")
    public Integer getTotalNumberOfMessages() {
        return this.messages("").size();
    }

    /**
     * How many seen messages are there?
     * @return Total number of messages which were already seen
     */
    @XmlAttribute(name = "seen")
    public Integer getTotalNumberOfSeenMessages() {
        Integer count = 0;
        for (Message msg : this.messages("")) {
            if (msg.seen()) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * This identity is a participant here?
     * @param identity The identity
     * @return Is it?
     */
    protected boolean isParticipant(final Identity identity) {
        for (ParticipantData dude : this.data.getParticipants()) {
            if (dude.getIdentity().equals(identity.name())) {
                return true;
            }
        }
        return false;
    }

}
