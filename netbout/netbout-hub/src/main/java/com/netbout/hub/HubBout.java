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

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubBout implements Bout {

    /**
     * The viewer.
     */
    private final transient HubIdentity viewer;

    /**
     * The data.
     */
    private final transient BoutData data;

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
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        if (!this.confirmed()) {
            throw new IllegalStateException("You can't rename until you join");
        }
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Participant invite(final Identity friend) {
        if (!this.confirmed()) {
            throw new IllegalStateException("You can't invite until you join");
        }
        final ParticipantData dude =
            ParticipantData.build(this.number(), friend.name());
        this.data.addParticipant(dude);
        dude.setConfirmed(false);
        Logger.debug(
            this,
            "#invite('%s'): success",
            friend
        );
        ((HubIdentity) friend).invited(this);
        return HubParticipant.build(dude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        final Collection<Participant> participants
            = new ArrayList<Participant>();
        for (ParticipantData dude : this.data.getParticipants()) {
            participants.add(HubParticipant.build(dude));
        }
        Logger.debug(
            this,
            "#participants(): %d participants found",
            participants.size()
        );
        return participants;
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
            messages.add(HubMessage.build(this.viewer, msg));
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
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        if (!this.confirmed()) {
            throw new IllegalStateException("You can't post until you join");
        }
        final MessageData msg = this.data.addMessage();
        msg.setDate(new Date());
        msg.setAuthor(this.viewer.name());
        msg.setText(text);
        Logger.debug(
            this,
            "#post('%s'): message posted",
            text
        );
        final Message message = HubMessage.build(this.viewer, msg);
        message.text();
        return message;
    }

    /**
     * This identity is a participant here?
     * @param identity The identity
     * @return Is it?
     */
    protected boolean isParticipant(final Identity identity) {
        boolean found = false;
        for (ParticipantData dude : this.data.getParticipants()) {
            if (dude.getIdentity().equals(identity.name())) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * This identity has confirmed participation?
     * @return Is it?
     */
    private boolean confirmed() {
        for (Participant dude : this.participants()) {
            if (dude.identity().equals(this.viewer)) {
                return dude.confirmed();
            }
        }
        throw new IllegalStateException("Can't find myself in participants");
    }

}
