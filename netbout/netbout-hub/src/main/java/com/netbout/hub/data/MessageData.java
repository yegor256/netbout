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

import com.netbout.hub.Hub;
import com.netbout.hub.MessageDt;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * One message in a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class MessageData implements MessageDt {

    /**
     * Hub to work with.
     */
    private final transient Hub hub;

    /**
     * Number of the message.
     */
    private final transient Long number;

    /**
     * The date.
     */
    private transient Date date;

    /**
     * The author.
     */
    private transient Urn author;

    /**
     * The text.
     */
    private transient String text;

    /**
     * Who already have seen this message, and who haven't?
     */
    private final transient ConcurrentMap<Urn, Boolean> seenBy =
        new ConcurrentHashMap<Urn, Boolean>();

    /**
     * Public ctor.
     * @param ihub The hub
     * @param num The number of this message
     */
    public MessageData(final Hub ihub, final Long num) {
        this.hub = ihub;
        assert num != null;
        this.number = num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MessageDt data) {
        return this.getDate().compareTo(data.getDate());
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
    public void setDate(final Date dte) {
        this.date = dte;
        this.hub.bus().make("changed-message-date")
            .asap()
            .arg(this.number)
            .arg(this.date)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setDate('%s'): set",
            this.date
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getDate() {
        if (this.date == null) {
            this.date = this.hub.bus().make("get-message-date")
                .synchronously()
                .arg(this.number)
                .exec();
            Logger.debug(
                this,
                "#getDate(): date '%s' loaded for msg #%d",
                this.date,
                this.number
            );
        }
        return this.date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthor(final Urn idnt) {
        this.author = idnt;
        this.hub.bus().make("changed-message-author")
            .asap()
            .arg(this.number)
            .arg(this.author)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setAuthor('%s'): set for msg #%d",
            this.author,
            this.number
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn getAuthor() {
        if (this.author == null) {
            this.author = this.hub.bus().make("get-message-author")
                .synchronously()
                .arg(this.number)
                .exec();
            Logger.debug(
                this,
                "#getAuthor(): author '%s' loaded for msg #%d",
                this.author,
                this.number
            );
        }
        return this.author;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(final String txt) {
        this.text = txt;
        this.hub.bus().make("changed-message-text")
            .asap()
            .arg(this.number)
            .arg(this.text)
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#setText('%s'): set for msg #%d",
            this.text,
            this.number
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        if (this.text == null) {
            this.text = this.hub.bus().make("get-message-text")
                .synchronously()
                .arg(this.number)
                .exec();
            Logger.debug(
                this,
                "#getText(): text '%s' loaded for msg #%d",
                this.text,
                this.number
            );
        }
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSeenBy(final Urn identity) {
        if (!this.seenBy.containsKey(identity) || !this.seenBy.get(identity)) {
            this.hub.bus().make("message-was-seen")
                .asap()
                .arg(this.number)
                .arg(identity)
                .asDefault(true)
                .exec();
            Logger.debug(
                this,
                "#addSeenBy('%s'): set for msg #%d",
                identity,
                this.number
            );
        }
        this.seenBy.put(identity, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isSeenBy(final Urn identity) {
        if (!this.seenBy.containsKey(identity)) {
            final Boolean status = this.hub.bus().make("was-message-seen")
                .synchronously()
                .arg(this.number)
                .arg(identity)
                .asDefault(false)
                .exec();
            this.seenBy.put(identity, status);
            Logger.debug(
                this,
                "#isSeenBy('%s'): %b loaded for msg #%d",
                identity,
                status,
                this.number
            );
        }
        return this.seenBy.get(identity);
    }

}
