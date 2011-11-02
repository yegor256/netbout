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

import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Internal storage of bout data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class BoutData {

    /**
     * The title.
     */
    private String title;

    /**
     * Collection of participants.
     */
    private final Collection<ParticipantData> participants =
        new ArrayList<ParticipantData>();

    /**
     * Ordered list of messages.
     */
    private final List<MessageData> messages = new ArrayList<MessageData>();

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
        Logger.info(
            this,
            "#setTitle('%s'): changed",
            text
        );
    }

    /**
     * Add new participant.
     * @param data The participant
     */
    public void addParticipant(final ParticipantData data) {
        this.participants.add(data);
        Logger.info(
            this,
            "#addParticipant('%s'): added (%d in total now)",
            data.getIdentity().name(),
            this.participants.size()
        );
    }

    /**
     * Get list of participants.
     * @return The list
     */
    public Collection<ParticipantData> getParticipants() {
        return this.participants;
    }

    /**
     * Get full list of messages.
     * @param query The search query
     * @return Messages
     */
    public List<MessageData> getMessages(final String query) {
        return this.messages;
    }

    /**
     * Post new message.
     * @param data The data
     */
    public void addMessage(final MessageData data) {
        this.messages.add(data);
        Logger.info(
            this,
            "#addMessage('%s'): added (%d in total now)",
            data.getText(),
            this.messages.size()
        );
    }

}
