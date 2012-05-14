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
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;

/**
 * Message in a hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class HubMessage implements Message {

    /**
     * The hub.
     */
    private final transient PowerHub hub;

    /**
     * The viewer.
     */
    private final transient Identity viewer;

    /**
     * The bout where this message is located.
     */
    private final transient Bout ibout;

    /**
     * The data.
     */
    private final transient MessageDt data;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param vwr Viewer
     * @param bout The bout where this message is located
     * @param dat The data
     * @checkstyle ParameterNumber (3 lines)
     */
    public HubMessage(final PowerHub ihub, final Identity vwr,
        final Bout bout, final MessageDt dat) {
        this.hub = ihub;
        this.viewer = vwr;
        this.ibout = bout;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Message msg) {
        return this.date().compareTo(msg.date());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object msg) {
        return msg == this || (msg instanceof Message
            && this.number().equals(((Message) msg).number()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.number().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("msg#%d", this.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        return this.ibout;
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
    public Identity author() {
        try {
            return this.hub.identity(this.data.getAuthor());
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        this.data.addSeenBy(this.viewer.name());
        return this.data.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return this.data.isSeenBy(this.viewer.name());
    }

}
