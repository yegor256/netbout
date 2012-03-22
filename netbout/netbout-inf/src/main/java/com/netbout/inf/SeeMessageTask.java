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
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.Set;

/**
 * The task to review one message.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeMessageTask extends AbstractTask {

    /**
     * The bout.
     */
    private final transient Message message;

    /**
     * Public ctor.
     * @param what The message to update
     * @param index The index to use
     */
    public SeeMessageTask(final Message what, final Index index) {
        super(index);
        this.message = what;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        final Set<Urn> names = new HashSet<Urn>();
        for (Participant dude : this.message.bout().participants()) {
            names.add(dude.identity().name());
        }
        return names;
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
     *
     * <p>There is no synchronization, intentionally. Msg class is thread-safe
     * and we don't worry about concurrent changes to it.
     */
    @Override
    protected void execute() {
        PredicateBuilder.extract(this.message, this.index());
        Logger.debug(
            this,
            "#execute(): cached message #%d in %[nano]s",
            this.message.number(),
            this.time()
        );
    }

}
