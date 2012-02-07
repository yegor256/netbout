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

import com.ymock.util.Logger;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * One value inside {@link Msg} (thread-safe).
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Value {

    /**
     * Maximum wait time, in sec.
     */
    private static final int MAX_WAIT = 10;

    /**
     * Values.
     */
    private final transient Collection<Object> values =
        new CopyOnWriteArraySet<Object>();

    /**
     * Show some stats.
     * @return Text
     */
    public String statistics() {
        return Logger.format("%[list]s", this.values);
    }

    /**
     * Clear all values.
     */
    public void clear() {
        this.values.clear();
    }

    /**
     * Get value (wait for it if necessary).
     * @return The value
     * @param <T> Type of value
     * @throws InterruptedException If can't find the value for long time
     */
    public <T> T get() throws InterruptedException {
        int max = this.MAX_WAIT;
        while (this.values.isEmpty() && max > 0) {
            max -= 1;
            TimeUnit.SECONDS.sleep(1L);
        }
        if (this.values.isEmpty()) {
            throw new InterruptedException();
        }
        if (this.values.size() > 1) {
            throw new IllegalStateException(
                "There is more than one value, use has() instead"
            );
        }
        return (T) this.values.iterator().next();
    }

    /**
     * Has this value.
     * @param val The value to put
     * @param <T> Type of value
     * @return Yes or no
     */
    public <T> boolean has(final T val) {
        return this.values.contains(val);
    }

    /**
     * Put value.
     * @param val The value to put
     * @param <T> Type of value
     */
    public <T> void put(final T val) {
        synchronized (this) {
            this.clear();
            this.values.add(val);
        }
    }

    /**
     * Put value.
     * @param val The value to put
     * @param <T> Type of value
     */
    public <T> void add(final T val) {
        this.values.add(val);
    }

}
