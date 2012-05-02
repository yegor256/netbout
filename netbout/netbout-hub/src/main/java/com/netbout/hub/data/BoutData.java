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

import com.netbout.hub.BoutDt;
import com.netbout.hub.Hub;
import com.netbout.hub.MessageDt;
import com.netbout.hub.ParticipantDt;
import com.netbout.spi.MessageNotFoundException;
import com.netbout.spi.Urn;
import com.jcabi.log.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class BoutData implements BoutDt {

    /**
     * Hub to work with.
     */
    private final transient Hub hub;

    /**
     * The number.
     */
    private final transient Long number;

    /**
     * Listener of msg creation operations.
     */
    private final transient MsgListener listener;

    /**
     * The title.
     */
    private transient String title;

    /**
     * The date.
     */
    private transient Date date;

    /**
     * Collection of participants.
     */
    private transient Collection<ParticipantDt> participants;

    /**
     * List of already retrieved messages (cached).
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final transient Map<Long, MessageDt> messages =
        new ConcurrentHashMap<Long, MessageDt>();

    /**
     * Public ctor.
     * @param ihub The hub
     * @param num The number
     * @param lstr Listener of message operations
     */
    public BoutData(final Hub ihub, final Long num, final MsgListener lstr) {
        this.hub = ihub;
        assert num != null;
        this.number = num;
        this.listener = lstr;
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
    public void confirm(final Urn identity) {
        this.find(identity).setConfirmed(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickOff(final Urn identity) {
        final ParticipantDt dude = this.find(identity);
        synchronized (this.number) {
            this.participants.remove(dude);
            this.hub.make("removed-bout-participant")
                .synchronously()
                .arg(this.number)
                .arg(identity)
                .asDefault(true)
                .exec();
        }
        this.hub.infinity().see(
            new KickOffNotice() {
                @Override
                public Bout bout() {
                    return new InfBout(BoutData.this);
                }
                @Override
                public Identity identity() {
                    return new InfIdentity(identity.name());
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeader(final Urn name) {
        synchronized (this.number) {
            final Collection<ParticipantDt> dudes = this.getParticipants();
            ParticipantDt leader = null;
            for (ParticipantDt dude : dudes) {
                if (dude.isLeader()) {
                    leader = dude;
                    break;
                }
            }
            ParticipantDt candidate = null;
            for (ParticipantDt dude : dudes) {
                if (dude.getIdentity().equals(name)) {
                    candidate = dude;
                    break;
                }
            }
            if (candidate == null) {
                throw new IllegalArgumentException(
                    String.format(
                        "Identity '%s' not in bout #%d, can't set as leader",
                        name,
                        this.number
                    )
                );
            }
            candidate.setLeader(true);
            if (leader != null) {
                leader.setLeader(false);
            }
        }
        Logger.debug(
            this,
            "#setLeader('%s'): changed in bout #%d",
            name,
            this.number
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getDate() {
        synchronized (this.number) {
            if (this.date == null) {
                this.date = this.hub.make("get-bout-date")
                    .synchronously()
                    .arg(this.number)
                    .exec();
            }
        }
        return this.date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        synchronized (this.number) {
            if (this.title == null) {
                this.title = this.hub.make("get-bout-title")
                    .synchronously()
                    .arg(this.number)
                    .exec();
            }
        }
        return this.title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(final String text) {
        synchronized (this.number) {
            this.title = text;
            this.hub.make("changed-bout-title")
                .asap()
                .arg(this.number)
                .arg(this.title)
                .asDefault(true)
                .exec();
            this.hub.infinity().see(
                new BoutRenamedNotice() {
                    @Override
                    public Bout bout() {
                        return new InfBout(BoutData.this);
                    }
                }
            );
        }
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
    public ParticipantDt addParticipant(final Urn name) {
        final ParticipantDt data =
            new ParticipantData(this.hub, this.number, name);
        synchronized (this.number) {
            this.getParticipants().add(data);
            this.hub.make("added-bout-participant")
                .synchronously()
                .arg(this.number)
                .arg(data.getIdentity())
                .asDefault(true)
                .exec();
            this.hub.infinity().see(
                new ParticipantInvitedNotice() {
                    @Override
                    public Bout bout() {
                        return new InfBout(BoutData.this);
                    }
                    @Override
                    public Identity invitee() {
                        return new InfIdentity(urn);
                    }
                }
            );
        }
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
        synchronized (this.number) {
            if (this.participants == null) {
                this.participants = new CopyOnWriteArrayList<ParticipantDt>();
                final List<Urn> identities = this.hub
                    .make("get-bout-participants")
                    .synchronously()
                    .arg(this.number)
                    .asDefault(new ArrayList<Urn>())
                    .exec();
                for (Urn identity : identities) {
                    this.participants.add(
                        new ParticipantData(this.hub, this.number, identity)
                    );
                }
            }
        }
        return this.participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageDt addMessage() {
        synchronized (this.number) {
            final Long num = this.hub.make("create-bout-message")
                .synchronously()
                .arg(this.number)
                .asDefault(1L)
                .exec();
            final MessageDt data = new MessageData(this.hub, num);
            this.messages.put(num, data);
            this.listener.messageCreated(num, this.number);
            this.hub.infinity().see(
                new MessagePostedNotice() {
                    @Override
                    public Message message() {
                        return new InfMessage(data);
                    }
                }
            );
            Logger.debug(
                this,
                "#addMessage(): new empty message #%d added to bout #%d",
                data.getNumber(),
                this.number
            );
            return data;
        }
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public MessageDt findMessage(final Long num)
        throws MessageNotFoundException {
        synchronized (this.number) {
            if (!this.messages.containsKey(num)) {
                final Boolean exists = this.hub
                    .make("check-message-existence")
                    .synchronously()
                    .arg(this.number)
                    .arg(num)
                    .asDefault(false)
                    .exec();
                if (!exists) {
                    throw new MessageNotFoundException(num);
                }
                this.messages.put(num, new MessageData(this.hub, num));
            }
        }
        return this.messages.get(num);
    }

    /**
     * Find this participant in the bout.
     * @param name Name of it
     * @return The participant
     */
    private ParticipantDt find(final Urn name) {
        ParticipantDt found = null;
        for (ParticipantDt dude : this.getParticipants()) {
            if (dude.getIdentity().equals(name)) {
                found = dude;
                break;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Identity '%s' is not in bout #%d, can't confirm/leave",
                    name,
                    this.number
                )
            );
        }
        return found;
    }

}
