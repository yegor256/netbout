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
package com.netbout.rest.account;

import com.netbout.rest.RsPage;
import com.netbout.spi.Alias;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeLink;

/**
 * Save user email.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.14
 */
final class TkSaveEmail implements Take {

    /**
     * Alias.
     */
    private final transient Alias alias;

    /**
     * Request.
     */
    private final transient Request request;

    /**
     * Ctor.
     * @param als Alias
     * @param req Request
     */
    TkSaveEmail(final Alias als, final Request req) {
        this.alias = als;
        this.request = req;
    }

    @Override
    public Response act() throws IOException {

    }

    public void post(@FormParam("email") final String email)
        throws IOException {
        try {
            this.alias().email(email);
        } catch (final Alias.InvalidEmailException ex) {
            throw FlashInset.forward(this.self(), ex);
        }
        throw FlashInset.forward(
            this.self(),
            String.format(
                "email changed to '%s'",
                email
            ),
            Level.INFO
        );
    }

    /**
     * Get self URI.
     * @return URI
     */
    private URI self() {
        return this.uriInfo().getBaseUriBuilder().clone()
            .path(AccountRs.class)
            .build();
    }

}
