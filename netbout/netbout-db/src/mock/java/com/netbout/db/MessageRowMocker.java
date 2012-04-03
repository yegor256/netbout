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
package com.netbout.db;

import com.netbout.spi.Urn;
import java.util.Date;

/**
 * Mocker of {@code MESSAGE} row in a database.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageRowMocker {

    /**
     * The bout it is related to.
     */
    private final transient Long bout;

    /**
     * Date of bout.
     */
    private transient Date date = new Date();

    /**
     * Text of message.
     */
    private transient String text = "hi there!";

    /**
     * Author of bout.
     */
    private transient Urn author = new IdentityRowMocker().mock();

    /**
     * Public ctor.
     * @param number The bout
     */
    public MessageRowMocker(final Long number) {
        this.bout = number;
    }

    /**
     * With this text.
     * @param txt The text
     * @return THis object
     */
    public MessageRowMocker withText(final String txt) {
        this.text = txt;
        return this;
    }

    /**
     * With this date.
     * @param when Date of message
     * @return THis object
     */
    public MessageRowMocker withDate(final Date when) {
        this.date = when;
        return this;
    }

    /**
     * With this author.
     * @param name The author
     * @return THis object
     */
    public MessageRowMocker withAuthor(final Urn name) {
        this.author = name;
        return this;
    }

    /**
     * Mock it and return its number.
     * @return The number of just mocked message
     */
    public Long mock() {
        final MessageFarm farm = new MessageFarm();
        Long number;
        number = farm.createBoutMessage(this.bout);
        farm.changedMessageAuthor(number, this.author);
        farm.changedMessageDate(number, this.date);
        farm.changedMessageText(number, this.text);
        return number;
    }

}
