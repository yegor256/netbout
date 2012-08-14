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
package com.netbout.rest.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link LogList}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LogListTest {

    /**
     * LogList can be empty.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void emptyListReturnsEmptyString() throws Exception {
        MatcherAssert.assertThat(
            new LogList().toString(),
            Matchers.equalTo("")
        );
    }

    /**
     * LogList pack texts into one string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void packsEventsIntoOneString() throws Exception {
        final String message = "How are you doing, dude?";
        final LogList first = new LogList();
        final LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Mockito.doReturn(Thread.currentThread().getName())
            .when(event).getThreadName();
        Mockito.doReturn(message).when(event).getMessage();
        Mockito.doReturn(message).when(event).getRenderedMessage();
        Mockito.doReturn(Level.INFO).when(event).getLevel();
        final WebAppender appender = new WebAppender();
        appender.setLayout(new SimpleLayout());
        appender.append(event);
        final String packed = first.toString();
        final LogList second = new LogList();
        second.append(packed);
        MatcherAssert.assertThat(
            second.events(),
            Matchers.allOf(
                Matchers.<String>iterableWithSize(1),
                Matchers.<String>hasItem(String.format("INFO - %s\n", message))
            )
        );
    }

}
