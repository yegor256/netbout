/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.email;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Postman;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Email Bout.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.12
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class EmBout implements Bout {

    /**
     * Original.
     */
    private final transient Bout origin;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     * @param slf Self alias
     */
    EmBout(final Bout org, final Postman pst, final String slf) {
        this.origin = org;
        this.self = slf;
        this.postman = pst;
    }

    @Override
    public long number() throws IOException {
        return this.origin.number();
    }

    @Override
    public Date date() throws IOException {
        return this.origin.date();
    }

    @Override
    public Date updated() throws IOException {
        return this.origin.updated();
    }

    @Override
    public String title() throws IOException {
        return this.origin.title();
    }

    @Override
    public void rename(final String text) throws IOException {
        this.origin.rename(text);
    }

    @Override
    public boolean subscription() throws IOException {
        return this.origin.subscription();
    }

    @Override
    public void subscribe(final boolean subs) throws IOException {
        this.origin.subscribe(subs);
    }

    @Override
    public Messages messages() throws IOException {
        return new EmMessages(
            this.origin.messages(),
            this.postman, this, this.self
        );
    }

    @Override
    public Friends friends() throws IOException {
        return new EmFriends(this.origin.friends());
    }

    @Override
    public Attachments attachments() throws IOException {
        return new EmAttachments(this.origin.attachments());
    }
}
