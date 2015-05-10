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
package com.netbout.email;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Postman;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Email Aliases.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.12
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class EmAliases implements Aliases {

    /**
     * Original.
     */
    private final transient Aliases origin;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     */
    EmAliases(final Aliases org, final Postman pst) {
        this.origin = org;
        this.postman = pst;
    }

    @Override
    public String check(final String name) throws IOException {
        return this.origin.check(name);
    }

    @Override
    public void add(final String name) throws IOException {
        this.origin.add(name);
    }

    @Override
    public Iterable<Alias> iterate() throws IOException {
        return Iterables.transform(
            this.origin.iterate(),
            new Function<Alias, Alias>() {
                @Override
                public Alias apply(final Alias input) {
                    return new EmAlias(input, EmAliases.this.postman);
                }
            }
        );
    }

}
