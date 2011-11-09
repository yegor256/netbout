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

import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutData {

    /**
     * The title.
     */
    private String title = "";

    /**
     * The number.
     */
    private Long number;

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
     * Get its number.
     * @return The number
     */
    public Long getNumber() {
        if (this.number == null) {
            throw new IllegalStateException("#setNumber() was never called");
        }
        return this.number;
    }

    /**
     * Set its number.
     * @param num The number
     */
    public void setNumber(final Long num) {
        this.number = num;
        Logger.info(
            this,
            "#setNumber('%d'): changed",
            num
        );
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
