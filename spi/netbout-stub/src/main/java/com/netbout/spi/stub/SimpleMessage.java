/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.stub;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;

/**
 * Simple implementation of a {@link Message}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleMessage implements Message {

    /**
     * The bout.
     */
    private Bout bout;

    /**
     * The author.
     */
    private Identity identity;

    /**
     * The text.
     */
    private String text;

    /**
     * The date.
     */
    private Date date = new Date();

    /**
     * Public ctor.
     * @param holder Owner of this message
     * @param idnt The author
     * @param txt The text
     * @param when Date of it
     * @checkstyle ParameterNumber (3 lines)
     */
    public SimpleMessage(final Bout holder, final Identity idnt,
        final String txt, final Date when) {
        this.bout = holder;
        this.identity = idnt;
        this.text = txt;
        this.date = when;
    }

    /**
     * Get link to bout.
     * @return The bout
     */
    public Bout bout() {
        return this.bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        return this.identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        try {
            return ChainedHelperProvider.INSTANCE
                .local(this.bout(), this.text)
                .execute("pre-render-message", this.text);
        } catch (com.netbout.spi.HelperException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.date;
    }

}
