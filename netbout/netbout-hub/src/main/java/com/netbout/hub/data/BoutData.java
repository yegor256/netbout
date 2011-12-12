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
package com.netbout.hub.data;

import com.netbout.bus.Bus;
import com.netbout.hub.BoutDt;
import com.netbout.hub.MessageDt;
import com.netbout.hub.ParticipantDt;
import com.netbout.spi.MessageNotFoundException;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class BoutData implements BoutDt {

    /**
     * Bus to work with.
     */
    private final transient Bus bus;

    /**
     * The number.
     */
    private final transient Long number;

    /**
     * The title.
     */
    private transient String title;

    /**
     * Collection of participants.
     */
    private transient Collection<ParticipantDt> participants;

    /**
     * Ordered list of messages.
     */
    private transient List<MessageDt> messages;

    /**
     * Public ctor.
     * @param ibus The bus
     * @param num The number
     */
    public BoutData(final Bus ibus, final Long num) {
        this.bus = ibus;
        assert num != null;
        this.number = num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getNumber() {
        return this.number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm(final String identity) {
        for (ParticipantDt dude : this.getParticipants()) {
            if (dude.getIdentity().equals(identity)) {
                dude.setConfirmed(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickOff(final String identity) {
        ParticipantDt found = null;
        for (ParticipantDt dude : this.getParticipants()) {
            if (dude.getIdentity().equals(identity)) {
                found = dude;
                break;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Identity '%s' is not in bout #%d, can't kick off",
                    identity,
                    this.number
                )
            );
        }
        this.participants.remove(found);
        this.bus.make("removed-bout-participant")
            .asap()
            .arg(this.number)
            .arg(identity)
            .asDefault(true)
            .exec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (this.title == null) {
            this.title = this.bus.make("get-bout-title")
                .synchronously()
                .arg(this.number)
                .exec();
            Logger.debug(
                this,
                "#getTitle(): title '%s' loaded for bout #%d",
                this.title,
                this.number
            );
        }
        return this.title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(final String text) {
        this.title = text;
        this.bus.make("changed-bout-title")
            .asap()
            .arg(this.number)
            .arg(this.title)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setTitle('%s'): set for bout #%d",
            this.title,
            this.number
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipantDt addParticipant(final String name) {
        final ParticipantDt data =
            new ParticipantData(this.bus, this.number, name);
        this.getParticipants().add(data);
        this.bus.make("added-bout-participant")
            .asap()
            .arg(this.number)
            .arg(data.getIdentity())
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#addParticipant('%s'): added for bout #%d (%d total)",
            data.getIdentity(),
            this.number,
            this.getParticipants().size()
        );
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<ParticipantDt> getParticipants() {
        synchronized (this) {
            if (this.participants == null) {
                this.participants = new CopyOnWriteArrayList<ParticipantDt>();
                final List<String> identities = this.bus
                    .make("get-bout-participants")
                    .synchronously()
                    .arg(this.number)
                    .asDefault(new ArrayList<String>())
                    .exec();
                for (String identity : identities) {
                    this.participants.add(
                        new ParticipantData(this.bus, this.number, identity)
                    );
                }
                Logger.debug(
                    this,
                    "#getParticipants(): reloaded %d participants for bout #%d",
                    this.participants.size(),
                    this.number
                );
            }
            return this.participants;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDt addMessage() {
        final Long num = this.bus.make("create-bout-message")
            .synchronously()
            .arg(this.number)
            .asDefault(1L)
            .exec();
        final MessageDt data = new MessageData(this.bus, num);
        this.getMessages().add(data);
        Logger.debug(
            this,
            "#addMessage(): new empty message #%d added to bout #%d",
            data.getNumber(),
            this.number
        );
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<MessageDt> getMessages() {
        synchronized (this) {
            if (this.messages == null) {
                this.messages = new CopyOnWriteArrayList<MessageDt>();
                final List<Long> nums = this.bus
                    .make("get-bout-messages")
                    .synchronously()
                    .arg(this.number)
                    .asDefault(new ArrayList<Long>())
                    .exec();
                for (Long num : nums) {
                    this.messages.add(new MessageData(this.bus, num));
                }
                Logger.debug(
                    this,
                    "#getMessages(): reloaded %d messages for bout #%d",
                    this.messages.size(),
                    this.number
                );
            }
            return this.messages;
        }
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public MessageDt findMessage(final Long num)
        throws MessageNotFoundException {
        for (MessageDt msg : this.getMessages()) {
            if (msg.getNumber().equals(num)) {
                return msg;
            }
        }
        throw new MessageNotFoundException(num);
    }

}
