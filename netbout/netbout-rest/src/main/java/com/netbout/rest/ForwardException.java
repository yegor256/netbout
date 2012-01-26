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
package com.netbout.rest;

import com.netbout.spi.client.RestSession;
import com.netbout.utils.TextUtils;
import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Thrown when necessary to forward user to another page and show a message
 * over there.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public class ForwardException extends WebApplicationException {

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param msg The message
     */
    public ForwardException(final Resource res, final String msg) {
        this(res, res.base().path("/g"), msg);
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param builder Where to forward to
     * @param msg The message
     */
    public ForwardException(final Resource res, final UriBuilder builder,
        final String msg) {
        super(
            new IllegalArgumentException(msg),
            ForwardException.response(res, builder, msg)
        );
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param cause Cause of trouble
     */
    public ForwardException(final Resource res, final Exception cause) {
        this(res, res.base().path("/"), cause);
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param builder Where to forward to
     * @param cause Cause of trouble
     */
    public ForwardException(final Resource res, final UriBuilder builder,
        final Exception cause) {
        super(
            cause,
            ForwardException.response(res, builder, cause.getMessage())
        );
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param builder Where to forward to
     * @param msg The message
     * @return The JAX-RS response
     */
    private static Response response(final Resource res,
        final UriBuilder builder, final String msg) {
        final NewCookie cookie = new NewCookie(
            RestSession.MESSAGE_COOKIE,
            TextUtils.pack(msg),
            res.base().build().getPath(),
            res.base().build().getHost(),
            Integer.valueOf(Manifests.read("Netbout-Revision")),
            "netbout message",
            // @checkstyle MagicNumber (1 line)
            60 * 60,
            false
        );
        Logger.debug(
            ForwardException.class,
            "#response(%[type]s, %s, %s): forwarding",
            res,
            builder.build(),
            msg
        );
        return Response.status(Response.Status.TEMPORARY_REDIRECT)
            .header(RestSession.ERROR_HEADER, msg)
            .entity(msg)
            .location(builder.build())
            .cookie(cookie)
            .build();
    }

}
