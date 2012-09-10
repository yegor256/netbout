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
 * this code accidentally and without intent to use it, please report this
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

import com.netbout.spi.Urn;
import java.util.Date;
import java.util.Random;
import org.mockito.Mockito;

/**
 * Mocker of {@link MessageDt}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageDtMocker {

    /**
     * The object.
     */
    private final transient MessageDt message = Mockito.mock(MessageDt.class);

    /**
     * Public ctor.
     */
    public MessageDtMocker() {
        this.withNumber(Math.abs(new Random().nextLong()));
        this.withDate(new Date());
        this.withText("some random text");
    }

    /**
     * With this number.
     * @param num The number
     * @return This object
     */
    public MessageDtMocker withNumber(final Long num) {
        Mockito.doReturn(num).when(this.message).getNumber();
        return this;
    }

    /**
     * With this date.
     * @param date The date of message
     * @return This object
     */
    public MessageDtMocker withDate(final Date date) {
        Mockito.doReturn(date).when(this.message).getDate();
        return this;
    }

    /**
     * With this author.
     * @param author The author
     * @return This object
     */
    public MessageDtMocker withAuthor(final Urn author) {
        Mockito.doReturn(author).when(this.message).getAuthor();
        return this;
    }

    /**
     * With this text.
     * @param text The text of the message
     * @return This object
     */
    public MessageDtMocker withText(final String text) {
        Mockito.doReturn(text).when(this.message).getText();
        return this;
    }

    /**
     * Build it.
     * @return The bout
     */
    public MessageDt mock() {
        return this.message;
    }

}
