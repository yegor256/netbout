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
 * Email Catch.
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
     * Ctor.
     * @param act Email message handler
     * @param usr Email login key
     * @param pass Email password
     * @param hst Email server host
     * @param prt Email server port
     * @param period Email server check period
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    EmCatch(final Action act, final String usr, final String pass,
            final String hst, final int prt, final long period) {
        this.action = act;
        this.user = usr;
        this.password = pass;
        this.host = hst;
        this.port = prt;
        final Thread monitor = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        EmCatch.this.check();
                        try {
                            TimeUnit.SECONDS.sleep(period);
                        } catch (final InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        );
        monitor.setDaemon(true);
        monitor.start();
    }
    /**
     * Read unread mail and persist new bout message.
     */
    private void check() {
        try {
            final Properties props = new Properties();
            final Session session = Session.getInstance(props);
            final URLName urlName = new URLName(
                "pop3",
                this.host,
                this.port,
                null,
                this.user,
                this.password
            );
            final Store store = session.getStore(urlName);
            store.connect();
            final Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            final FlagTerm unseen = new FlagTerm(
                new Flags(Flags.Flag.SEEN),
                false
            );
            final Message[] messages = inbox.search(unseen);
            for (final Message each : messages) {
                this.action.run(each);
            }
            inbox.close(true);
            store.close();
        } catch (final NoSuchProviderException ex) {
            throw new IllegalStateException(ex);
        } catch (final MessagingException ex) {
            throw new IllegalStateException(ex);
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
