/**
 * Copyright (c) 2009-2014, netbout.com
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
package com.netbout.client.retry;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Pageable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached inbox.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.3
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "origin")
public final class ReInbox implements Inbox {

    /**
     * Original object.
     */
    private final transient Inbox origin;

    /**
     * Public ctor.
     * @param orgn Original object
     */
    public ReInbox(final Inbox orgn) {
        this.origin = orgn;
    }

    @Override
    @RetryOnFailure(verbose = false)
    public long start() throws IOException {
        return this.origin.start();
    }

    @Override
    @RetryOnFailure(verbose = false)
    public Bout bout(final long number) throws BoutNotFoundException {
        return new ReBout(this.origin.bout(number));
    }

    @Override
    @RetryOnFailure(verbose = false)
    public Pageable<Bout> jump(final int pos) throws IOException {
        return this.origin.jump(pos);
    }

    @Override
    @RetryOnFailure(verbose = false)
    public Iterable<Bout> iterate() throws IOException {
        return Iterables.transform(
            this.origin.iterate(),
            new Function<Bout, Bout>() {
                @Override
                public Bout apply(final Bout bout) {
                    return new ReBout(bout);
                }
            }
        );
    }
}
