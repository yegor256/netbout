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
package com.netbout.log;

import com.ymock.util.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log appender, for cloud loggers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class CloudAppender extends AppenderSkeleton implements Runnable {

    /**
     * End of line.
     */
    public static final String EOL = "\n";

    /**
     * Queue of messages to send to server.
     */
    private final transient BlockingQueue<String> messages =
        new LinkedBlockingQueue<String>();

    /**
     * The feeder.
     */
    private transient Feeder feeder;

    /**
     * Set feeder, option {@code feeder} in config.
     * @param fdr The feeder to use
     */
    public void setFeeder(final Feeder fdr) {
        if (this.feeder != null) {
            throw new IllegalArgumentException("call #setFeeder() only once");
        }
        this.feeder = fdr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() {
        super.activateOptions();
        final Thread thread = new Thread(this);
        thread.setName(Logger.format("CloudAppender to %[type]s", this.feeder));
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final LoggingEvent event) {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.getLayout().format(event));
        final String[] exc = event.getThrowableStrRep();
        if (exc != null) {
            for (String text : exc) {
                buf.append(text).append(this.EOL);
            }
        }
        this.messages.offer(buf.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    public void run() {
        System.out.println("CloudAppender started to work...");
        while (true) {
            String text;
            try {
                text = this.messages.take();
            } catch (InterruptedException ex) {
                break;
            }
            try {
                this.feeder.feed(text);
            } catch (java.io.IOException ex) {
                System.out.println(
                    Logger.format(
                        "%sfailed to report because of %[exception]s",
                        text,
                        ex
                    )
                );
            }
        }
        System.out.println("CloudAppender finished to work.");
    }

}
