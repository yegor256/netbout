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
package com.netbout.email;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Postman;
import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Pageable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Email pageable.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.10.3
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class EmPageable<T> implements Pageable<T> {

    /**
     * Original.
     */
    private final transient Pageable<T> origin;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     * @param slf Self alias
     */
    EmPageable(final Pageable<T> org, final Postman pst, final String slf) {
        this.origin = org;
        this.postman = pst;
        this.self = slf;
    }

    @Override
    public Pageable<T> jump(final long number) throws IOException {
        return new EmPageable<T>(
            this.origin.jump(number),
            this.postman, this.self
        );
    }

    @Override
    public Iterable<T> iterate() throws IOException {
        return Iterables.transform(
            this.origin.iterate(),
            new Function<T, T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T apply(final T input) {
                    final Object result;
                    if (input instanceof Message) {
                        result = input;
                    } else {
                        result = new EmBout(
                            Bout.class.cast(input),
                            EmPageable.this.postman, EmPageable.this.self
                        );
                    }
                    return (T) result;
                }
            }
        );
    }
}
