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
package com.netbout.db;

import com.jcabi.urn.URN;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

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
    private transient Date date;

    /**
     * Text of message.
     */
    private transient String text;

    /**
     * Author of bout.
     */
    private transient URN author;

    /**
     * Public ctor.
     * @param number The bout
     * @throws SQLException If fails
     */
    public MessageRowMocker(final Long number) throws SQLException {
        this.author = new IdentityRowMocker().mock();
        this.bout = number;
        this.withDate(new Date());
        this.withText("hi there!");
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
        this.date = DateUtils.truncate(when, Calendar.SECOND);
        return this;
    }

    /**
     * With this author.
     * @param name The author
     * @return THis object
     */
    public MessageRowMocker withAuthor(final URN name) {
        this.author = name;
        return this;
    }

    /**
     * Mock it and return its number.
     * @return The number of just mocked message
     * @throws SQLException If fails
     */
    public Long mock() throws SQLException {
        final MessageFarm farm = new MessageFarm();
        Long number;
        number = farm.createBoutMessage(this.bout);
        farm.changedMessageAuthor(number, this.author);
        farm.changedMessageDate(number, this.date);
        farm.changedMessageText(number, this.text);
        return number;
    }

}
