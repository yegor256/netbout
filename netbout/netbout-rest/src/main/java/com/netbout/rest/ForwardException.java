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

import java.net.URI;
import javax.ws.rs.WebApplicationException;
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
     * @param uri Where to forward to
     * @param msg The message
     */
    public ForwardException(final URI uri, final String msg) {
        super(
            Response
                .status(Response.Status.TEMPORARY_REDIRECT)
                .entity(msg)
                .location(
                    UriBuilder.fromUri(uri)
                        .queryParam("m", ForwardException.encode(msg))
                        .build())
                .build()
        );
    }

    /**
     * Constructor.
     * @param uri Where to forward to
     * @param msg The message
     */
    public ForwardException(final String uri, final String msg) {
        this(UriBuilder.fromUri(uri).build(), msg);
    }

    /**
     * Constructor.
     * @param uri Where to forward to
     */
    public ForwardException(final String uri) {
        this(UriBuilder.fromUri(uri).build(), "");
    }

    /**
     * Constructor.
     * @param uri Where to forward to
     * @param cause Cause of trouble
     */
    public ForwardException(final String uri, final Exception cause) {
        this(UriBuilder.fromUri(uri).build(), cause.getMessage());
    }

    /**
     * Constructor.
     * @param uri Where to forward to
     * @param cause Cause of trouble
     */
    public ForwardException(final URI uri, final Exception cause) {
        this(uri, cause.getMessage());
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
