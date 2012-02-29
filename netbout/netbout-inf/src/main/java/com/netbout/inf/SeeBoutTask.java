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

import com.netbout.bus.Bus;
import com.netbout.spi.Bout;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The task to review one bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeBoutTask extends AbstractTask {

    /**
     * The infinity.
     */
    private final transient Infinity infinity;

    /**
     * The bus.
     */
    private final transient Bus bus;

    /**
     * The bout.
     */
    private final transient Bout bout;

    /**
     * Public ctor.
     * @param inf The infinity
     * @param where The BUS to work with
     * @param what The bout to update
     */
    public SeeBoutTask(final Infinity inf, final Bus where, final Bout what) {
        super();
        this.infinity = inf;
        this.bus = where;
        this.bout = what;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("see-bout-#%d", this.bout.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        final Set<Urn> names = new HashSet<Urn>();
        for (Participant dude : this.bout.participants()) {
            names.add(dude.identity().name());
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() {
        final Collection<Long> numbers = new TreeSet<Long>(
            (List<Long>) this.bus.make("get-bout-messages")
                .synchronously()
                .arg(this.bout.number())
                .asDefault(new ArrayList<Long>())
                .exec()
        );
        for (Long number : numbers) {
            try {
                this.infinity.see(this.bout.message(number));
            } catch (com.netbout.spi.MessageNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.debug(
            this,
            "#execute(): cached %d message(s) of bout #%d in %dms",
            numbers.size(),
            this.bout.number(),
            this.time()
        );
    }

}
