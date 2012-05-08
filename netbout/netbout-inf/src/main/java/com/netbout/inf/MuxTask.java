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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.netbout.inf.notices.AliasAddedNotice;
import com.netbout.inf.notices.BoutNotice;
import com.netbout.inf.notices.BoutRenamedNotice;
import com.netbout.inf.notices.IdentityNotice;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.inf.notices.KickOffNotice;
import com.netbout.inf.notices.MessageNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.notices.MessageSeenNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The task to review one notice.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class MuxTask implements Runnable {

    /**
     * The store with predicates.
     */
    private final transient Store store;

    /**
     * The ray.
     */
    private final transient Ray ray;

    /**
     * When started.
     */
    private transient long started;

    /**
     * When finished (or ZERO if still running).
     */
    private transient long finished;

    /**
     * The notice.
     */
    private final transient Notice ntc;

    /**
     * Dependants.
     */
    private final transient Set<Urn> deps;

    /**
     * Public ctor.
     * @param what The notice to process
     * @param iray The ray to use
     * @param str The store to use
     */
    public MuxTask(final Notice what, final Ray iray, final Store str) {
        this.store = str;
        this.ray = iray;
        this.ntc = what;
        final Set<Urn> urns = new HashSet<Urn>();
        if (what instanceof IdentityNotice) {
            urns.add(((IdentityNotice) what).identity().name());
        }
        if (what instanceof BoutNotice) {
            urns.addAll(
                MuxTask.dudesOf(((BoutNotice) what).bout())
            );
        }
        if (what instanceof MessageNotice) {
            urns.addAll(
                MuxTask.dudesOf(
                    ((MessageNotice) what).message().bout()
                )
            );
        }
        if (urns.isEmpty()) {
            throw new IllegalArgumentException("empty list of deps");
        }
        this.deps = Collections.unmodifiableSet(urns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object task) {
        return task instanceof MuxTask && this.hashCode() == task.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.started = System.nanoTime();
        this.execute();
        this.finished = System.nanoTime();
    }

    /**
     * Get notice incapsulated.
     * @return The notice
     */
    public Notice notice() {
        return this.ntc;
    }

    /**
     * Get its execution time.
     * @return Time in nanoseconds
     */
    public long time() {
        long time;
        if (this.finished == 0L) {
            time = System.nanoTime() - this.started;
        } else {
            time = this.finished - this.started;
        }
        return time;
    }

    /**
     * Get names of all people waiting for the completion of this task.
     * @return Names
     */
    public Set<Urn> dependants() {
        return this.deps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(MuxTask.nameOf(this.ntc));
        if (this.ntc instanceof IdentityNotice) {
            text.append(" w/").append(
                ((IdentityNotice) this.ntc).identity().name()
            );
        }
        if (this.ntc instanceof BoutNotice) {
            text.append(" @").append(
                ((BoutNotice) this.ntc).bout().number()
            );
        }
        if (this.ntc instanceof MessageNotice) {
            text.append(" at").append(
                ((MessageNotice) this.ntc).message().number()
            );
        }
        return text.toString();
    }

    /**
     * Execute it.
     */
    private void execute() {
        this.store.see(this.ray, this.ntc);
        Logger.debug(
            this,
            "#execute(): done \"%s\" in %[nano]s",
            this,
            this.time()
        );
    }

    /**
     * Get name of notice.
     * @param notice The notice to analyze
     * @return The name
     */
    private static String nameOf(final Notice notice) {
        String name;
        if (notice instanceof MessagePostedNotice) {
            name = "message posted";
        } else if (notice instanceof MessageSeenNotice) {
            name = "message seen";
        } else if (notice instanceof AliasAddedNotice) {
            name = "alias added";
        } else if (notice instanceof BoutRenamedNotice) {
            name = "bout renamed";
        } else if (notice instanceof KickOffNotice) {
            name = "kicked off";
        } else if (notice instanceof JoinNotice) {
            name = "participation confirmed";
        } else {
            throw new IllegalStateException("unknown type of notice");
        }
        return name;
    }

    /**
     * Get list of dudes (names of participants) from the bout.
     * @param bout The bout to analyze
     * @return The names
     */
    private static Set<Urn> dudesOf(final Bout bout) {
        final Set<Urn> deps = new HashSet<Urn>();
        for (Participant dude : bout.participants()) {
            deps.add(dude.identity().name());
        }
        return deps;
    }

}
