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
package com.netbout.rest.bout;

import com.netbout.rest.RqAlias;
import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHeaders;
import org.takes.rq.RqWrap;

/**
 * Retrieves bout from request.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
@EqualsAndHashCode(callSuper = true)
public final class RqBout extends RqWrap {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse The base
     * @param req Request
     */
    public RqBout(final Base bse, final Request req) {
        super(req);
        this.base = bse;
    }

    /**
     * Get bout.
     * @return Bout
     * @throws IOException If fails
     */
    public Bout bout() throws IOException {
        final Alias alias = new RqAlias(this.base, this).alias();
        final long number = Long.parseLong(
            new RqHeaders(this).header("X-Netbout-Bout").iterator().next()
        );
        final Bout bout;
        try {
            bout = alias.inbox().bout(number);
        } catch (final Inbox.BoutNotFoundException ex) {
            throw new HttpException(HttpURLConnection.HTTP_NOT_FOUND, ex);
        }
        if (!new Friends.Search(bout.friends()).exists(alias.name())) {
            throw new RsForward(
                new RsFlash(
                    String.format("you're not in bout #%d", bout.number()),
                    Level.WARNING
                )
            );
        }
        return bout;
    }

}
