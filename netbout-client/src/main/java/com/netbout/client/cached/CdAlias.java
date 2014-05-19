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
package com.netbout.client.cached;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.netbout.spi.Alias;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached alias.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.3
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "origin")
public final class CdAlias implements Alias {

    /**
     * Original object.
     */
    private final transient Alias origin;

    /**
     * Public ctor.
     * @param orgn Original object
     */
    public CdAlias(final Alias orgn) {
        this.origin = orgn;
    }

    @Override
    @Cacheable
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    @Cacheable
    public URI photo() throws IOException {
        return this.origin.photo();
    }

    @Override
    @Cacheable
    public Locale locale() throws IOException {
        return this.origin.locale();
    }

    @Override
    @Cacheable.FlushAfter
    public void photo(final URI uri) throws IOException {
        this.origin.photo(uri);
    }

    @Override
    @Cacheable
    public Inbox inbox() throws IOException {
        return new CdInbox(this.origin.inbox());
    }
}
