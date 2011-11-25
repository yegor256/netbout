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

import com.rexsl.core.Manifests;
import java.net.URI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;

/**
 * Thrown when necessary to forward user to another page and show a message
 * over there.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ForwardException extends WebApplicationException {

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param uri Where to forward to
     * @param msg The message
     */
    public ForwardException(final Resource res, final URI uri,
        final String msg) {
        super(
            Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .entity(msg)
                .location(uri)
                .cookie(
                    new NewCookie(
                        AbstractPage.MESSAGE_COOKIE,
                        ForwardException.encode(msg),
                        res.uriInfo().getBaseUri().getPath(),
                        res.uriInfo().getBaseUri().getHost(),
                        Integer.valueOf(Manifests.read("Netbout-Revision")),
                        "netbout message",
                        // @checkstyle MagicNumber (1 line)
                        60 * 60,
                        false
                    ))
                .build()
        );
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param uri Where to forward to
     * @param msg The message
     */
    public ForwardException(final Resource res, final String uri,
        final String msg) {
        this(res, UriBuilder.fromUri(uri).build(), msg);
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param uri Where to forward to
     */
    public ForwardException(final Resource res, final String uri) {
        this(res, UriBuilder.fromUri(uri).build(), "");
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param uri Where to forward to
     * @param cause Cause of trouble
     */
    public ForwardException(final Resource res, final String uri,
        final Exception cause) {
        this(res, UriBuilder.fromUri(uri).build(), cause.getMessage());
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param uri Where to forward to
     * @param cause Cause of trouble
     */
    public ForwardException(final Resource res, final URI uri,
        final Exception cause) {
        this(res, uri, cause.getMessage());
    }

    /**
     * Encode message.
     * @param text The text to encode
     * @return Decoded text (from Base64)
     */
    private static String encode(final String text) {
        try {
            return new Base64().encodeToString(text.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
