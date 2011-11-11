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
     * The title.
     */
    private String title;

    /**
     * The number.
     */
    private Long number;

    /**
     * Collection of participants.
     */
    private Collection<ParticipantData> participants;

    /**
     * Ordered list of messages.
     */
    private List<MessageData> messages;

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
        if (this.number != null) {
            throw new IllegalStateException(
                String.format(
                    "setNumber() can't called for bout #%d",
                    this.number
                )
            );
        }
        Logger.debug(
            this,
            "#setNumber(%d): set",
            num
        );
        this.number = num;
    }

    /**
     * Get title.
     * @return The title
     */
    public String getTitle() {
        if (this.title == null) {
            this.title = HelpQueue.exec(
                "get-bout-title",
                String.class,
                HelpQueue.Priority.SYNCHRONOUSLY,
                this.number
            );
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
        HelpQueue.exec(
            "changed-bout-title",
            Boolean.class,
            HelpQueue.Priority.ASAP,
            this.number,
            this.title
        );
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
        HelpQueue.exec(
            "added-bout-participant",
            Boolean.class,
            HelpQueue.Priority.ASAP,
            this.number,
            data.getIdentity()
        );
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
                final String[] identities = HelpQueue.exec(
                    "get-bout-participant-identities",
                    String[].class,
                    HelpQueue.Priority.SYNCHRONOUSLY,
                    this.number
                );
                for (String identity : identities) {
                    final ParticipantData data = new ParticipantData();
                    data.setBout(this.number);
                    data.setIdentity(identity);
                    this.participants.add(data);
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
     * @param data The data
     */
    public void addMessage(final MessageData data) {
        this.getMessages().add(data);
        HelpQueue.exec(
            "added-bout-message",
            Boolean.class,
            HelpQueue.Priority.ASAP,
            this.number,
            data.getDate().getTime(),
            data.getAuthor(),
            data.getText()
        );
        Logger.debug(
            this,
            "#addMessage(..): message by '%s' added to bout #%d",
            data.getAuthor(),
            this.number
        );
    }

    /**
     * Get full list of messages.
     * @return Messages
     */
    public List<MessageData> getMessages() {
        synchronized (this) {
            if (this.messages == null) {
                this.messages = new CopyOnWriteArrayList<MessageData>();
                final Long[] dates = HelpQueue.exec(
                    "get-bout-message-dates",
                    Long[].class,
                    HelpQueue.Priority.SYNCHRONOUSLY,
                    this.number
                );
                for (Long msec : dates) {
                    final MessageData data = new MessageData();
                    data.setBout(this.number);
                    data.setDate(new Date(msec));
                    this.messages.add(data);
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
