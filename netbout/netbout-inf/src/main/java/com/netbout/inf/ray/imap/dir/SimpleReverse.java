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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import com.netbout.inf.ray.imap.Reverse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple implementation of {@link Reverse}.
 *
 * <p>The class is thread-safe, except {@link #load(InputStream)}
 * and {@link #save(OutputStream)} methods.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class SimpleReverse implements Reverse {

    /**
     * Map of values and message numbers.
     */
    private final transient ConcurrentMap<Long, String> map =
        new ConcurrentSkipListMap<Long, String>(Collections.reverseOrder());

    /**
     * {@inheritDoc}
     */
    @Override
    public long sizeof() {
        return this.map.size();
    }

    /**
     * {@inheritDoc}
     * @checkstyle MagicNumber (30 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public String get(final long msg) throws Reverse.ValueNotFoundException {
        String value = null;
        final long start = System.currentTimeMillis();
        while (true) {
            value = this.map.get(msg);
            if (value != null) {
                break;
            }
            if (System.currentTimeMillis() - start > 10) {
                throw new Reverse.ValueNotFoundException(
                    Logger.format(
                        // @checkstyle LineLength (1 line)
                        "value not found for msg #%d among %d others, even after %[ms]s of waiting",
                        msg,
                        this.map.size(),
                        System.currentTimeMillis() - start
                    )
                );
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final long number, final String value) {
        synchronized (this.map) {
            final String existing = this.map.get(number);
            if (existing != null && !existing.equals(value)) {
                throw new IllegalArgumentException(
                    String.format(
                        "can't replace value for msg #%d from '%s' to '%s'",
                        number,
                        existing,
                        value
                    )
                );
            }
            this.map.put(number, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final long msg) {
        this.map.remove(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final OutputStream stream) throws IOException {
        final DataOutputStream data = new DataOutputStream(stream);
        for (Map.Entry<Long, String> entry : this.map.entrySet()) {
            data.writeLong(entry.getKey());
            data.writeUTF(entry.getValue());
        }
        data.writeLong(0L);
        data.flush();
        Logger.debug(
            this,
            "#save(..): saved %d values",
            this.map.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final InputStream stream) throws IOException {
        this.map.clear();
        final DataInputStream data = new DataInputStream(stream);
        while (true) {
            final long msg = data.readLong();
            if (msg == 0) {
                break;
            }
            if (this.map.containsKey(msg)) {
                throw new IOException("duplicate key in reverse");
            }
            this.map.put(msg, data.readUTF());
        }
        if (!this.map.isEmpty()) {
            Logger.debug(
                this,
                "#load(..): loaded %d values",
                this.map.size()
            );
        }
    }

    /**
     * Audit it against the list of numbers.
     * @param audit The audit
     * @param value The value these numbers are used for
     * @param numbers All numbers we should see for this value
     */
    public void audit(final Audit audit, final String value,
        final Collection<Long> numbers) {
        for (Long number : numbers) {
            if (!this.map.containsKey(number)) {
                audit.problem(
                    String.format(
                        "msg #%d doesn't have a value, while '%s' expected",
                        number,
                        value
                    )
                );
                break;
            }
            if (!this.map.get(number).equals(value)) {
                audit.problem(
                    String.format(
                        "msg #%d has value '%s' while '%s' expected",
                        number,
                        this.map.get(number),
                        value
                    )
                );
                break;
            }
        }
    }

}
