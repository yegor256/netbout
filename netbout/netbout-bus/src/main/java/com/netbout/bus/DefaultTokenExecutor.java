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
package com.netbout.bus;

import com.netbout.spi.Bout;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.netbout.spi.Plain;
import com.netbout.spi.Urn;
import com.netbout.spi.plain.PlainList;
import com.ymock.util.Logger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default executor of a token.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTokenExecutor implements TokenExecutor {

    /**
     * List of registered helpers.
     */
    private final transient ConcurrentMap<Identity, Helper> helpers =
        new ConcurrentHashMap<Identity, Helper>();

    /**
     * Consumption bills.
     */
    private final transient List<Bill> bills = new CopyOnWriteArrayList<Bill>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Identity identity, final Helper helper) {
        if (this.helpers.containsKey(identity)) {
            throw new IllegalArgumentException(
                String.format(
                    "Identity '%s' has already been registered as '%s'",
                    identity.name(),
                    helper.location()
                )
            );
        }
        if (this.helpers.containsValue(helper)) {
            throw new IllegalArgumentException(
                String.format(
                    "Helper '%s' has already been registered",
                    helper.location()
                )
            );
        }
        this.helpers.put(identity, helper);
        Logger.info(
            this,
            "#register(%s): registered (%d total now)",
            helper,
            this.helpers.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(final TxToken token) {
        final Bill bill = this.run(token, this.helpers.entrySet());
        this.save(bill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(final TxToken token, final Bout bout) {
        final Set<Map.Entry<Identity, Helper>> active =
            new HashSet<Map.Entry<Identity, Helper>>();
        for (Participant participant : bout.participants()) {
            final Identity identity = participant.identity();
            if (this.helpers.containsKey(identity)) {
                active.add(
                    new AbstractMap.SimpleEntry<Identity, Helper>(
                        identity,
                        this.helpers.get(identity)
                    )
                );
            }
        }
        final Bill bill = this.run(token, active);
        bill.inBout(bout.number());
        this.save(bill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stats() {
        final StringBuilder text = new StringBuilder();
        for (Bill archive : this.bills) {
            text.append(archive.toString()).append("\n");
        }
        return text.toString();
    }

    /**
     * Execute given token with a given set of helpers.
     * @param token The token to execute
     * @param targets The helpers to use
     * @return How many miliseconds it took
     */
    private Bill run(final TxToken token,
        final Set<Map.Entry<Identity, Helper>> targets) {
        final Bill bill = new Bill();
        for (Map.Entry<Identity, Helper> helper : targets) {
            if (helper.getValue().supports().contains(token.mnemo())) {
                final long start = System.currentTimeMillis();
                helper.getValue().execute(token);
                if (token.isCompleted()) {
                    bill.done(
                        token.mnemo(),
                        helper.getKey().name(),
                        System.currentTimeMillis() - start
                    );
                    break;
                }
            }
        }
        Logger.debug(
            this,
            "#run(%s, %d helpers): returned [%s]",
            token,
            targets.size(),
            token.getResult()
        );
        return bill;
    }

    /**
     * Save bill to archive.
     * @param bill The bill to save
     */
    private void save(final Bill bill) {
        synchronized (this.bills) {
            if (this.bills.size() > 100) {
                final List<String> lines = new ArrayList<String>();
                for (Bill archive : this.bills) {
                    lines.add(archive.toString());
                }
                this.exec(
                    new DefaultTxToken(
                        "save-bus-statistics",
                        Arrays.asList(new Plain<?>[] {new PlainList(lines)})
                    )
                );
                this.bills.clear();
            }
        }
        this.bills.add(bill);
    }

    /**
     * One bill.
     */
    private static final class Bill {
        /**
         * Mnemo.
         */
        private transient String mnemo;
        /**
         * Helper.
         */
        private transient Urn helper;
        /**
         * Milliseconds.
         */
        private transient Long millis;
        /**
         * Bout.
         */
        private transient Long number;
        /**
         * Mark it done.
         * @param mnem The token we just executed
         * @param hlpr Who completed
         * @param msec How long did it take
         */
        public void done(final String mnem, final Urn hlpr,
            final Long msec) {
            this.mnemo = mnem;
            this.helper = hlpr;
            this.millis = msec;
        }
        /**
         * It is related to this bout.
         * @param num The bout
         */
        public void inBout(final Long num) {
            this.number = num;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format(
                "%s %s %d %d",
                this.mnemo,
                this.helper,
                this.millis,
                this.number
            );
        }
    }

}
