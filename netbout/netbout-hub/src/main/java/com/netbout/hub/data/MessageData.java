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

import com.netbout.spi.Identity;
import com.ymock.util.Logger;

/**
 * One message in a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageData {

    /**
     * The author.
     */
    private final Identity identity;

    /**
     * The text.
     */
    private final String text;

    /**
     * The date.
     */
    private final Date date = new Date();

    /**
     * Public ctor.
     * @param idnt The identity
     * @param txt The text
     */
    public MessageData(final Identity idnt, final String txt) {
        this.identity = idnt;
        this.text = txt;
    }

    /**
     * Get text.
     * @return The text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get identity.
     * @return The identity
     */
    public Identity getIdentity() {
        return this.identity;
    }

    /**
     * Get date.
     * @return The date
     */
    public Date getDate() {
        return this.date;
    }

}
