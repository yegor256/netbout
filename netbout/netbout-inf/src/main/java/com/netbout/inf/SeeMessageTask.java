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

import com.netbout.spi.Message;
import com.ymock.util.Logger;

/**
 * The task to review one message.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeMessageTask implements Task {

    /**
     * The heap.
     */
    private final transient Heap heap;

    /**
     * The bout.
     */
    private final transient Message message;

    /**
     * Public ctor.
     * @param where The HEAP to work with
     * @param what The message to update
     */
    public SeeMessageTask(final Heap where, final Message what) {
        this.heap = where;
        this.message = what;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("see-message-#%d", this.message.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec() {
        final long start = System.currentTimeMillis();
        final Long number = this.message.number();
        final DefaultMsg msg =
            new DefaultMsg(number, this.message.bout().number());
        this.heap.put(number, msg);
        PredicateBuilder.extract(this.message, msg);
        msg.close();
        Logger.debug(
            this,
            "#exec(): cached message #%d in %dms",
            this.message.number(),
            System.currentTimeMillis() - start
        );
    }

}
