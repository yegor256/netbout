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
package com.netbout.inf.ray.imap;

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import com.netbout.inf.Lattice;
import com.netbout.inf.ray.imap.dir.SimpleNumbers;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implemenation of {@link Index}.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
class BaseIndex implements FlushableIndex {

    /**
     * Attribute to use.
     */
    private final transient Attribute attribute;

    /**
     * Directory to work with.
     */
    private final transient Directory directory;

    /**
     * Main map.
     */
    private final transient ConcurrentMap<String, BaseIndex.TempNumbers> map =
        new ConcurrentHashMap<String, BaseIndex.TempNumbers>();

    /**
     * Numbers that has expiration date.
     */
    private static final class TempNumbers extends SimpleNumbers {
        /**
         * When was it accessed last time.
         */
        private final transient AtomicLong time = new AtomicLong();
        /**
         * Mark access time.
         */
        public void ping() {
            this.time.set(System.currentTimeMillis());
        }
        /**
         * Is it already expired and may be removed from memory?
         * @return TRUE if yes
         */
        public boolean expired() {
            return System.currentTimeMillis() - this.time.get()
                // @checkstyle MagicNumber (1 line)
                > 10 * 60 * 1000;
        }
    }

    /**
     * Public ctor.
     * @param attr Attribute to work with
     * @param dir Directory with files
     */
    public BaseIndex(final Attribute attr, final Directory dir) {
        this.attribute = attr;
        this.directory = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long sizeof() {
        long sizeof = 0L;
        for (Numbers numbers : this.map.values()) {
            sizeof += numbers.sizeof();
        }
        return sizeof;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%,d values in %,d bytes",
            this.map.size(),
            this.sizeof()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.clean(BaseIndex.validate(msg));
        this.add(msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.numbers(value).add(BaseIndex.validate(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.numbers(value).remove(BaseIndex.validate(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        BaseIndex.validate(msg);
        for (String value : this.map.keySet()) {
            this.delete(msg, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long next(final String value, final long msg) {
        return this.numbers(value).next(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lattice lattice(final String value) {
        return this.numbers(value).lattice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String attr(final long msg) {
        throw new UnsupportedOperationException(
            String.format("use ReversiveIndex for '%s'", this.attribute)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        final long start = System.currentTimeMillis();
        for (Map.Entry<String, BaseIndex.TempNumbers> entry
            : this.map.entrySet()) {
            this.directory.save(
                this.attribute,
                entry.getKey(),
                entry.getValue()
            );
            if (entry.getValue().expired()) {
                this.map.remove(entry.getKey());
                Logger.debug(
                    this,
                    "#flush(): '%[text]s' expired and removed",
                    entry.getKey()
                );
            }
        }
        Logger.debug(
            this,
            "#flush(): saved %d values to writer in %[ms]s",
            this.map.size(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * Validate this message number and throw runtime exception if it's not
     * valid (is ZERO or MAX_VALUE).
     * @param msg The number of msg
     * @return The same message
     */
    private static long validate(final long msg) {
        if (msg == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (msg == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
        return msg;
    }

    /**
     * Numbers for the given value.
     * @param text The text value
     * @return Numbers (link to existing structure in the MAP)
     */
    private Numbers numbers(final String text) {
        synchronized (this.map) {
            if (!this.map.containsKey(text)) {
                final BaseIndex.TempNumbers numbers =
                    new BaseIndex.TempNumbers();
                try {
                    this.directory.load(this.attribute, text, numbers);
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
                this.map.put(text, numbers);
            }
        }
        final BaseIndex.TempNumbers nums = this.map.get(text);
        nums.ping();
        return nums;
    }

}
