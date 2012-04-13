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

import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.netbout.inf.Notice;
import com.netbout.inf.notices.AliasAddedNotice;
import com.netbout.inf.notices.BoutRelatedNotice;
import com.netbout.inf.notices.BoutRenamedNotice;
import com.netbout.inf.notices.IdentityRelatedNotice;
import com.netbout.inf.notices.KickOffNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.notices.MessageRelatedNotice;
import com.netbout.inf.notices.MessageSeenNotice;
import com.netbout.inf.notices.ParticipationConfirmedNotice;
import com.ymock.util.Logger;
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
final class NoticeTask implements Task {

    /**
     * The store with predicates.
     */
    private final transient Store store;

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
    private final transient Notice notice;

    /**
     * Public ctor.
     * @param what The notice to process
     * @param str The store to use
     */
    public NoticeTask(final Notice what, final Store str) {
        this.store = str;
        this.notice = what;
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
        return task instanceof Task && this.hashCode() == task.hashCode();
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        final Set<Urn> deps = new HashSet<Urn>();
        if (this.notice instanceof IdentityRelatedNotice) {
            deps.add(((IdentityRelatedNotice) this.notice).identity().name());
        }
        if (this.notice instanceof BoutRelatedNotice) {
            deps.addAll(
                NoticeTask.dudesOf(((BoutRelatedNotice) this.notice).bout())
            );
        }
        if (this.notice instanceof MessageRelatedNotice) {
            deps.addAll(
                NoticeTask.dudesOf(
                    ((MessageRelatedNotice) this.notice).message().bout()
                )
            );
        }
        return deps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(NoticeTask.nameOf(this.notice));
        if (this.notice instanceof IdentityRelatedNotice) {
            text.append(" w/").append(
                ((IdentityRelatedNotice) this.notice).identity().name()
            );
        }
        if (this.notice instanceof BoutRelatedNotice) {
            text.append(" @").append(
                ((BoutRelatedNotice) this.notice).bout().number()
            );
        }
        if (this.notice instanceof MessageRelatedNotice) {
            text.append(" at").append(
                ((MessageRelatedNotice) this.notice).message().number()
            );
        }
        return text.toString();
    }

    /**
     * Execute it.
     */
    private void execute() {
        this.store.see(this.notice);
        Logger.debug(
            this,
            "#execute(): cached \"%s\" in %[nano]s",
            this,
            this.time()
        );
    }

    /**
     * Get name of notice.
     * @param ntc The notice to analyze
     * @return The name
     */
    private static String nameOf(final Notice ntc) {
        String name;
        if (ntc instanceof MessagePostedNotice) {
            name = "message posted";
        } else if (ntc instanceof MessageSeenNotice) {
            name = "message seen";
        } else if (ntc instanceof AliasAddedNotice) {
            name = "alias added";
        } else if (ntc instanceof BoutRenamedNotice) {
            name = "bout renamed";
        } else if (ntc instanceof KickOffNotice) {
            name = "kicked off";
        } else if (ntc instanceof ParticipationConfirmedNotice) {
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
