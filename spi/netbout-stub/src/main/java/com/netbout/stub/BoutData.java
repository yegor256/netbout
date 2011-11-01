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
package com.netbout.stub;

import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of a {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class BoutData {

    /**
     * The entry.
     */
    private final InMemoryEntry entry;

    /**
     * The title.
     */
    private String title;

    /**
     * Collection of participants.
     */
    private final Collection<SimpleParticipant> participants =
        new ArrayList<SimpleParticipant>();

    /**
     * Ordered list of messages.
     */
    private final List<SimpleMessage> messages = new ArrayList<SimpleMessage>();

    /**
     * Public ctor.
     * @param ent The entry to work with
     */
    public BoutData(final InMemoryEntry ent) {
        this.entry = ent;
    }

    /**
     * Get title.
     * @return The title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set title.
     * @param text The title
     */
    public void setTitle(final String text) {
        this.title = text;
    }

    /**
     * Invite new person.
     * @param identity The person
     * @return Invited
     */
    public Participant invite(final String identity) {
        final SimpleParticipant dude = new SimpleParticipant(identity);
        this.participants.add(dude);
        return dude;
    }

    /**
     * Get list of participants.
     * @return The list
     */
    public Collection<SimpleParticipant> participants() {
        return this.participants;
    }

    /**
     * Get full list of messages.
     * @param query The search query
     * @return Messages
     */
    public List<SimpleMessage> messages(final String query) {
        return this.messages;
    }

    /**
     * Post new message.
     * @param identity The author
     * @param text The message
     * @return The message just posted
     */
    public SimpleMessage post(final String identity, final String text) {
        final SimpleMessage msg = new SimpleMessage(identity, text);
        this.messages.add(msg);
        return msg;
    }

}
