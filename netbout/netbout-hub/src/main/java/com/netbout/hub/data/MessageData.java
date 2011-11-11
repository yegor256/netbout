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

import com.netbout.hub.HelpQueue;
import com.ymock.util.Logger;
import java.util.Date;

/**
 * One message in a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageData {

    /**
     * Number of bout.
     */
    private Long bout;

    /**
     * The date.
     */
    private Date date;

    /**
     * The author.
     */
    private String author;

    /**
     * The text.
     */
    private String text;

    /**
     * Set bout number.
     * @param num The number
     */
    public void setBout(final Long num) {
        if (this.bout != null) {
            throw new IllegalStateException(
                "setBout() can only set number one time, not change"
            );
        }
        this.bout = num;
        Logger.debug(
            this,
            "#setBout('%d'): set",
            this.bout
        );
    }

    /**
     * Get bout number.
     * @return The identity
     */
    public Long getBout() {
        if (this.bout == null) {
            throw new IllegalStateException("#setBout() was never called");
        }
        return this.bout;
    }

    /**
     * Set date.
     * @param dte The date
     */
    public void setDate(final Date dte) {
        if (this.date != null) {
            throw new IllegalStateException(
                "setDate() can only set date one time, not change"
            );
        }
        this.date = dte;
        Logger.debug(
            this,
            "#setDate('%s'): set",
            this.date
        );
    }

    /**
     * Get date.
     * @return The date
     */
    public Date getDate() {
        if (this.date == null) {
            throw new IllegalStateException("#setDate() was never called");
        }
        return this.date;
    }

    /**
     * Set identity.
     * @param idnt The identity
     */
    public void setAuthor(final String idnt) {
        if (this.author != null) {
            throw new IllegalStateException(
                "setAuthor() can only set identity one time, not change"
            );
        }
        this.author = idnt;
        HelpQueue.exec(
            "changed-message-author",
            Boolean.class,
            HelpQueue.SYNCHRONOUSLY,
            this.bout,
            this.date,
            this.author
        );
        Logger.debug(
            this,
            "#setAuthor('%s'): set",
            this.author
        );
    }

    /**
     * Get identity.
     * @return The identity
     */
    public String getAuthor() {
        if (this.author == null) {
            this.author = HelpQueue.exec(
                "get-message-author",
                String.class,
                HelpQueue.SYNCHRONOUSLY,
                this.bout,
                this.date
            );
            Logger.debug(
                this,
                "#getAuthor(): author '%s' loaded for msg in bout #%d",
                this.author,
                this.bout
            );
        }
        return this.author;
    }

    /**
     * Set text.
     * @param txt The text
     */
    public void setText(final String txt) {
        if (this.text != null) {
            throw new IllegalStateException(
                "setText() can only set text one time, not change"
            );
        }
        this.text = txt;
        HelpQueue.exec(
            "changed-message-text",
            Boolean.class,
            HelpQueue.SYNCHRONOUSLY,
            this.bout,
            this.date,
            this.text
        );
        Logger.debug(
            this,
            "#setText('%s'): set",
            this.text
        );
    }

    /**
     * Get text.
     * @return The text
     */
    public String getText() {
        if (this.text == null) {
            this.text = HelpQueue.exec(
                "get-message-text",
                String.class,
                HelpQueue.SYNCHRONOUSLY,
                this.bout,
                this.date
            );
            Logger.debug(
                this,
                "#getText(): text '%s' loaded for msg in bout #%d",
                this.text,
                this.bout
            );
        }
        return this.text;
    }

}
