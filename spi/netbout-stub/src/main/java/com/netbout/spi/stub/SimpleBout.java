/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.stub;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.UnknownIdentityException;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of a {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleBout implements Bout {

    /**
     * The viewer.
     */
    private Identity identity;

    /**
     * The data.
     */
    private BoutData data;

    /**
     * Public ctor.
     * @param idnt The viewer
     * @param dat The data
     */
    public SimpleBout(final Identity idnt, final BoutData dat) {
        this.identity = idnt;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity() {
        return this.identity;
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
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Participant invite(final String friend)
        throws UnknownIdentityException {
        final ParticipantData dude = new ParticipantData(
            ((InMemoryEntry) this.identity().user().entry()).friend(friend),
            false
        );
        this.data.addParticipant(dude);
        Logger.info(
            this,
            "#invite('%s'): success",
            friend
        );
        return new SimpleParticipant(
            this,
            dude.getIdentity(),
            dude.isConfirmed()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        final Collection<Participant> participants
            = new ArrayList<Participant>();
        for (ParticipantData dude : this.data.getParticipants()) {
            participants.add(
                new SimpleParticipant(
                    this,
                    dude.getIdentity(),
                    dude.isConfirmed()
                )
            );
        }
        Logger.info(
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
        final List<Message> messages = new ArrayList<Message>();
        for (MessageData msg : this.data.getMessages(query)) {
            messages.add(
                new SimpleMessage(
                    this,
                    msg.getIdentity(),
                    msg.getText(),
                    msg.getDate()
                )
            );
        }
        Logger.info(
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
        final MessageData msg = new MessageData(
            this.identity(),
            text
        );
        this.data.addMessage(msg);
        Logger.info(
            this,
            "#post('%s'): message posted",
            text
        );
        return new SimpleMessage(
            this,
            msg.getIdentity(),
            msg.getText(),
            msg.getDate()
        );
    }

}
