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
package com.netbout.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.takes.Response;
import org.takes.rs.RsWithCookie;
import org.takes.rs.RsWrap;

/**
 * Response decorator which sets cookie with return location.
 *
 * @author Ivan Inozemtsev (ivan.inozemtsev@gmail.com)
 * @version $Id$
 * @since 2.14.13
 */
public final class RsReturn extends RsWrap {
    /**
     * Ctor.
     * @param res Response to decorate
     * @param loc Location to be set as return location
     * @throws UnsupportedEncodingException If fails
     */
    public RsReturn(final Response res, final String loc)
        throws UnsupportedEncodingException {
        this(res, loc, RsReturn.class.getSimpleName());
    }
    /**
     * Ctor.
     * @param res Response to decorate
     * @param loc Location to be set as return location
     * @param cookie Cookie name
     * @throws UnsupportedEncodingException If fails
     */
    public RsReturn(final Response res, final String loc, final String cookie)
        throws UnsupportedEncodingException {
        super(
            new RsWithCookie(
                res,
                cookie,
                URLEncoder.encode(loc, Charset.defaultCharset().name()),
                String.format(
                    Locale.ENGLISH,
                    "Expires=%1$ta, %1$td %1$tb %1$tY %1$tT GMT",
                    new Date(
                        System.currentTimeMillis()
                            + TimeUnit.HOURS.toMillis(1L)
                    )
            )
        )
        );
    }
}
