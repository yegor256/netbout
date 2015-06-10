/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.email;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.search.FlagTerm;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Monitors an email account periodically
 * through a daemon thread.
 * @author Erim Erturk (erimerturk@gmail.com)
 * @version $Id$
 * @since 2.15
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = { "action", "user", "password" })
@EqualsAndHashCode(of = { "action", "user", "password" })
final class EmCatch {
    /**
     * Max sleep time of thread.
     */
    private static final long MAX_SLEEP_TIME = TimeUnit.HOURS.toMillis(24L);
    /**
     * Action.
     */
    private final transient Action action;
    /**
     * Email Login key.
     */
    private final transient String user;
    /**
     * Email password.
     */
    private final transient String password;
    /**
     * Email server host.
     */
    private final transient String host;
    /**
     * Email server port.
     */
    private final transient int port;
    /**
     * Email server check period in milliseconds.
     */
    private final transient long period;
    /**
     * Ctor.
     * @param act Email message handler
     * @param usr Email login key
     * @param pass Email password
     * @param hst Email server host
     * @param prt Email server port
     * @param prd Email server check period in milliseconds
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    EmCatch(final Action act, final String usr, final String pass,
            final String hst, final int prt, final long prd) {
        this.action = act;
        this.user = usr;
        this.password = pass;
        this.host = hst;
        this.port = prt;
        this.period = prd;
        final Thread monitor = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    EmCatch.this.mainLoop();
                }
            }
        );
        monitor.setDaemon(true);
        monitor.start();
    }
    /**
     * Main loop of the daemon thread. Fetches unread mail and persists
     * new bout message periodically
     */
    private void mainLoop() {
        long prd = this.period;
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(this.period);
                EmCatch.this.check();
                prd = this.period;
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            } catch (final MessagingException ex) {
                Logger.error(this, "%[exception]s", ex);
                if (prd * 2 < EmCatch.MAX_SLEEP_TIME) {
                    prd = prd * 2;
                }
            }
        }
    }
    /**
     * Read unread mail and persist new bout message.
     * @throws MessagingException If fails
     */
    private void check() throws MessagingException {
        Store store = null;
        Folder inbox = null;
        try {
            final Properties props = new Properties();
            final Session session = Session.getInstance(props);
            final URLName url = new URLName(
                "pop3",
                this.host,
                this.port,
                null,
                this.user,
                this.password
            );
            store = session.getStore(url);
            store.connect();
            inbox = store.getFolder("inbox");
            if (!inbox.exists()) {
                throw new IllegalStateException("inbox folder not exist!");
            }
            inbox.open(Folder.READ_WRITE);
            final FlagTerm unseen = new FlagTerm(
                new Flags(Flags.Flag.SEEN),
                false
            );
            for (final Message msg : inbox.search(unseen)) {
                this.action.run(msg);
            }
        } catch (final NoSuchProviderException ex) {
            throw new IllegalStateException(ex);
        } finally {
            inbox.close(true);
            store.close();
        }
    }

    /**
     * Email Catch Action.
     * @author Erim Erturk (erimerturk@gmail.com)
     * @version $Id$
     * @since 2.15
     */
    interface Action {
        /**
         * Create bout message from email message.
         * @param msg Bout message as email.
         * @todo #600:30min/DEV Create bout message using using
         *  msg values. Msg recipient value will hold boutId info.
         *  Msg from value will hold bout message creator info.
         *  Msg content will hold bout message content info.
         *  This method should get bout info from email and save it
         *  as bout message. And then change method signature like
         *  void run(Bout bout, String email, String body);
         */
        void run(Message msg);
    }

}
