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
package com.netbout.inf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Queue of tasks.
 *
 * <p>We use custom implementation because of a bug in OpenJDK:
 * http://stackoverflow.com/questions/12349881
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class MuxQueue {

    /**
     * List of tasks, a synchronized list.
     */
    private final transient List<MuxTask> tasks =
        new CopyOnWriteArrayList<MuxTask>();

    /**
     * Total number of elements inside.
     * @return The number
     */
    public int size() {
        return this.tasks.size();
    }

    /**
     * Does it contain this task?
     * @param task The task to check for
     * @return TRUE if contains
     */
    public boolean contains(final MuxTask task) {
        return this.tasks.contains(task);
    }

    /**
     * Add new task to the queue.
     * @param task The task to add
     */
    public void add(final MuxTask task) {
        this.tasks.add(task);
    }

    /**
     * Poll queue head, or NULL in time out.
     * @param timeout How many time units to wait for
     * @param unit Unit of time
     * @return The task or NULL
     * @throws InterruptedException If interrupted
     */
    public MuxTask poll(final long timeout, final TimeUnit unit)
        throws InterruptedException {
        MuxTask task = null;
        final long max = System.currentTimeMillis() + unit.toMillis(timeout);
        while (true) {
            synchronized (this.tasks) {
                if (!this.tasks.isEmpty()) {
                    task = this.tasks.get(0);
                    this.tasks.remove(0);
                    break;
                }
            }
            if (System.currentTimeMillis() >= max) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(1);
        }
        return task;
    }

}
