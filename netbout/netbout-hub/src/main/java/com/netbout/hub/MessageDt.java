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
package com.netbout.hub;

import java.util.Date;

/**
 * Message data type.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface MessageDt extends Comparable<MessageDt> {

    /**
     * Get message number.
     * @return The number of it
     */
    Long getNumber();

    /**
     * Set date of the message.
     * @param date The date of the message
     */
    void setDate(Date date);

    /**
     * Get date of the message.
     * @return The date
     */
    Date getDate();

    /**
     * Set identity.
     * @param idnt The identity
     */
    void setAuthor(String idnt);

    /**
     * Get identity.
     * @return The identity
     */
    String getAuthor();

    /**
     * Set text.
     * @param txt The text
     */
    void setText(final String txt);

    /**
     * Get text.
     * @return The text
     */
    String getText();

    /**
     * Add indentity, who has seen the message.
     * @param identity The identity
     */
    void addSeenBy(String identity);

    /**
     * Was it seen by this identity?
     * @param identity The identity
     * @return Was it seen?
     */
    Boolean isSeenBy(String identity);

}
