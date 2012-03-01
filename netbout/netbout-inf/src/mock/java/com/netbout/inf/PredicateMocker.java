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
package com.netbout.inf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Predicate}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicateMocker {

    /**
     * The object.
     */
    private final transient Predicate predicate = Mockito.mock(Predicate.class);

    /**
     * The messages.
     */
    private transient Collection<Long> messages;

    /**
     * The iterator.
     */
    private transient Iterator<Long> iterator;

    /**
     * Public ctor.
     */
    public PredicateMocker() {
        this.withMessages(new Long[] {});
    }

    /**
     * Without iteration.
     * @return This object
     */
    public PredicateMocker withoutIteration() {
        this.iterator = null;
        return this;
    }

    /**
     * With this list of messages.
     * @param msgs The list of them
     * @return This object
     */
    public PredicateMocker withMessages(final Long[] msgs) {
        this.messages = Arrays.asList(msgs);
        this.iterator = this.messages.iterator();
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    boolean has;
                    if (PredicateMocker.this.iterator == null) {
                        throw new IllegalArgumentException("#hasNext()");
                    } else {
                        has = PredicateMocker.this.iterator.hasNext();
                    }
                    return has;
                }
            }
        ).when(this.predicate).hasNext();
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    Long next;
                    if (PredicateMocker.this.iterator == null) {
                        throw new IllegalArgumentException("#next()");
                    } else {
                        next = PredicateMocker.this.iterator.next();
                    }
                    return next;
                }
            }
        ).when(this.predicate).next();
        Mockito.doAnswer(
            new Answer() {
                public Object answer(final InvocationOnMock invocation) {
                    final Long msg = (Long) invocation.getArguments()[0];
                    return PredicateMocker.this.messages.contains(msg);
                }
            }
        ).when(this.predicate).contains(Mockito.any(Long.class));
        return this;
    }

    /**
     * Build it.
     * @return The predicate
     */
    public Predicate mock() {
        return this.predicate;
    }

}
