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
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.stamp.StSender;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Email Alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.12
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class EmAlias implements Alias {

    /**
     * Original.
     */
    private final transient Alias origin;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     */
    EmAlias(final Alias org, final Postman pst) {
        this.origin = org;
        this.postman = new Postman() {
            @Override
            public void send(final Envelope envelope) throws IOException {
                pst.send(
                    new Envelope.MIME(envelope).with(
                        new StSender(org.name(), "no-reply@netbout.com")
                    )
                );
            }
        };
    }

    @Override
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    public URI photo() throws IOException {
        return this.origin.photo();
    }

    @Override
    public Locale locale() throws IOException {
        return this.origin.locale();
    }

    @Override
    public void photo(final URI uri) throws IOException {
        this.origin.photo(uri);
    }

    @Override
    public String email() throws IOException {
        return this.origin.email();
    }

    @Override
    public void email(final String email) throws IOException {
        this.origin.email(email);
    }

    @Override
    public void email(final String email, final String urn, final Bout bout)
        throws IOException {
        this.origin.email(email);
        new EmAliasMail(this.postman).send(email, urn, bout);
    }

    @Override
    public Inbox inbox() throws IOException {
        return new EmInbox(this.origin.inbox(), this.postman, this.name());
    }
}
