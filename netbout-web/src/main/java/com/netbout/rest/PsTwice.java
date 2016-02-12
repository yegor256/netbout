/**
 * Copyright (c) 2009-2016, netbout.com
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

import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.Pass;
import org.takes.misc.Opt;

/**
 * Sequentially tries first and second Pass instances. Identity of a second
 * Pass is returned if first passes. Empty identity is returned otherwise.
 *
 * @author Eugene Kondrashev (eugene.kondreashev@gmail.com)
 * @version $Id$
 * @since 2.16
 */
public final class PsTwice implements Pass {

    /**
     * First Pass to check.
     */
    private final transient Pass fst;

    /**
     * Second Pass to check.
     */
    private final transient Pass snd;

    /**
     * Ctor.
     * @param first Pass to enter
     * @param second Pass to enter if previous succeeded
     */
    public PsTwice(final Pass first, final Pass second) {
        this.fst = first;
        this.snd = second;
    }

    @Override
    public Opt<Identity> enter(final Request req) throws IOException {
        Opt<Identity> user = new Opt.Empty<Identity>();
        if (this.fst.enter(req).has()) {
            user = this.snd.enter(req);
        }
        return user;
    }

    @Override
    public Response exit(final Response response,
        final Identity identity) throws IOException {
        return this.snd.exit(response, identity);
    }
}
