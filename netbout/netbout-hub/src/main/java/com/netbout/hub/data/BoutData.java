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

import com.netbout.hub.queue.HelpQueue;
import com.ymock.util.Logger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bout with data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutData {

    /**
     * The number.
     */
    private final Long number;

    /**
     * The title.
     */
    private String title;

    /**
     * Collection of participants.
     */
    private Collection<ParticipantData> participants;

    /**
     * Ordered list of messages.
     */
    private List<MessageData> messages;

    /**
     * Public ctor.
     * @param num The number
     */
    public BoutData(final Long num) {
        this.number = num;
    }

    /**
     * Get its number.
     * @return The number
     */
    public Long getNumber() {
        return this.number;
    }

    /**
     * Get title.
     * @return The title
     */
    public String getTitle() {
        if (this.title == null) {
            this.title = HelpQueue.make("get-bout-title")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.number)
                .exec(String.class);
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
     * Set title.
     * @param text The title
     */
    public void setTitle(final String text) {
        this.title = text;
        HelpQueue.make("changed-bout-title")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.number)
            .arg(this.title)
            .exec(Boolean.class);
        Logger.debug(
            this,
            "#setTitle('%s'): set for bout #%d",
            this.title,
            this.number
        );
    }

    /**
     * Add new participant.
     * @param data The participant
     */
    public void addParticipant(final ParticipantData data) {
        this.getParticipants().add(data);
        HelpQueue.make("added-bout-participant")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.number)
            .arg(data.getIdentity())
            .exec(Boolean.class);
        Logger.debug(
            this,
            "#addParticipant('%s'): added for bout #%d (%d total)",
            data.getIdentity(),
            this.number,
            this.getParticipants().size()
        );
    }

    /**
     * Get list of participants.
     * @return The list
     */
    public Collection<ParticipantData> getParticipants() {
        synchronized (this) {
            if (this.participants == null) {
                this.participants = new CopyOnWriteArrayList<ParticipantData>();
                final String[] identities = HelpQueue
                    .make("get-bout-participant-identities")
                    .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                    .arg(this.number)
                    .asDefault(new String[]{})
                    .exec(String[].class);
                for (String identity : identities) {
                    this.participants.add(
                        new ParticipantData(this.number, identity)
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
     * Post new message.
     * @return The data
     */
    public MessageData addMessage() {
        final Long num = HelpQueue.make("create-bout-message")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .arg(this.number)
            .exec(Long.class);
        final MessageData data = new MessageData(num);
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
     * Get full list of messages.
     * @return Messages
     */
    public List<MessageData> getMessages() {
        synchronized (this) {
            if (this.messages == null) {
                this.messages = new CopyOnWriteArrayList<MessageData>();
                final Long[] nums = HelpQueue.make("get-bout-messages")
                    .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                    .arg(this.number)
                    .asDefault(new Long[]{})
                    .exec(Long[].class);
                for (Long num : nums) {
                    this.messages.add(new MessageData(num));
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

}
