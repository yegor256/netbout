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

import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link CloudAppender}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CloudAppenderTest {

    /**
     * CloudAppender can send messages in background.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsMessagesInBackground() throws Exception {
        final CloudAppender appender = new CloudAppender();
        appender.setLayout(new SimpleLayout());
        final Feeder feeder = Mockito.mock(Feeder.class);
        appender.setFeeder(feeder);
        final LoggingEvent event = new LoggingEvent(
            this.getClass().getName(),
            new RootLogger(Level.DEBUG),
            Level.DEBUG,
            "some text to log",
            new IllegalArgumentException()
        );
        appender.append(event);
        final long start = System.currentTimeMillis();
        while (!appender.isEmpty()) {
            TimeUnit.SECONDS.sleep(1L);
            if (System.currentTimeMillis() - start
                > TimeUnit.MINUTES.toMillis(1L)) {
                throw new IllegalStateException("waiting for too long");
            }
        }
        Mockito.verify(feeder).feed("DEBUG - some text to log\n");
    }

}
