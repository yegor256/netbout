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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple implementation of {@link Reverse}.
 *
 * <p>The class is thread-safe, except {@link #load(InputStream)}
 * and {@link #save(OutputStream)} methods.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleReverse implements Reverse {

    /**
     * Map of values and message numbers.
     */
    private final transient ConcurrentMap<Long, String> map =
        new ConcurrentHashMap<Long, String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(final long msg) {
        final String value = this.map.get(msg);
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("value not found for msg #%d", msg)
            );
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final long number, final String value) {
        this.map.put(number, value);
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
            this.map.put(msg, data.readUTF());
        }
    }

}
