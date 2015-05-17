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
package com.netbout.cached;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached Inbox.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class CdInbox implements Inbox {

    /**
     * Original.
     */
    private final transient Inbox origin;

    /**
     * Public ctor.
     * @param org Origin
     */
    CdInbox(final Inbox org) {
        this.origin = org;
    }

    @Override
    @Cacheable.FlushBefore
    public long start() throws IOException {
        return this.origin.start();
    }

    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.HOURS)
    public long unread() throws IOException {
        return this.origin.unread();
    }

    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.HOURS)
    @Loggable(
        value = Loggable.DEBUG,
        ignore = Inbox.BoutNotFoundException.class
    )
    public Bout bout(final long number) throws Inbox.BoutNotFoundException {
        return new CdBout(this.origin.bout(number));
    }

    @Override
    public Pageable<Bout> jump(final long number) throws IOException {
        return new CdPageable<Bout>(this.origin.jump(number));
    }

    @Override
    public Iterable<Bout> iterate() throws IOException {
        return Iterables.transform(
            this.origin.iterate(),
            new Function<Bout, Bout>() {
                @Override
                public Bout apply(final Bout input) {
                    return new CdBout(input);
                }
            }
        );
    }
}
