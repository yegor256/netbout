/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.inf.triples;

import com.netbout.spi.Message;
import java.io.Closeable;
import java.io.File;
import java.util.Iterator;

/**
 * Triples with Berkley DB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BerkleyTriples implements Triples {

    /**
     * Public ctor.
     * @param dir Where to keep data
     */
    public BerkleyTriples(final File dir) {
        // todo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        // ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final Long number, final String name, final T value) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean has(final Long number, final String name,
        final T value) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> all(final Long number, final String name) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final T value) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name,
        final Iterator<T> values) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final Long number, final String name) {
    }

}
