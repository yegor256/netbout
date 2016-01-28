/**
 * Copyright (c) 2009-2016, netbout.com
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
package com.netbout.dynamo;

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.netbout.spi.Aliases;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.util.Iterator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link DyMessages}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 */
public final class DyMessagesITCase {

    /**
     * DyMessages can list and create messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void makesAndListsMessages() throws Exception {
        final String alias = "robert";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:84218")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout bout = inbox.bout(inbox.start());
        final Messages messages = bout.messages();
        messages.post("hello!");
        messages.post("hello, again!");
        MatcherAssert.assertThat(
            messages.iterate(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    /**
     * DyMessages can jump through the list.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void jumpsThroughTheList() throws Exception {
        final String alias = "rodrigo";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:844838")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout bout = inbox.bout(inbox.start());
        final Messages messages = bout.messages();
        final int total = Tv.FIVE;
        for (int idx = 0; idx < total; ++idx) {
            messages.post(String.format("msg #%d", idx));
        }
        Pageable<Message> pageable = messages;
        int found = 0;
        while (true) {
            final Iterator<Message> iterator = pageable.iterate().iterator();
            if (!iterator.hasNext()) {
                break;
            }
            pageable = pageable.jump(iterator.next().number());
            ++found;
        }
        MatcherAssert.assertThat(found, Matchers.equalTo(total));
    }

    /**
     * DyMessages can search for text in messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesInMessages() throws Exception {
        final String alias = "frol";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:8831415")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout bout = inbox.bout(inbox.start());
        bout.messages().post("hello");
        // @checkstyle MultipleStringLiteralsCheck (1 line)
        bout.messages().post("world");
        bout.messages().post("foo");
        final Iterator<Message> result = bout.messages().search("r").iterator();
        MatcherAssert.assertThat(
            "search result is empty",
            result.hasNext()
        );
        MatcherAssert.assertThat(
            result.next().text(),
            // @checkstyle MultipleStringLiteralsCheck (1 line)
            Matchers.equalTo("world")
        );
        MatcherAssert.assertThat(
            "more results than expected",
            !result.hasNext()
        );
    }

    /**
     * DyMessages can retain leading spaces as code markdown.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void codeMarkdownInMessages() throws Exception {
        final String alias = "rokit";
        final Aliases aliases =
            new DyBase().user(new URN("urn:test:75065")).aliases();
        aliases.add(alias);
        final Inbox inbox = aliases.iterate().iterator().next().inbox();
        final Bout bout = inbox.bout(inbox.start());
        bout.messages().post("    4 leading spaces retained  ");
        final Iterator<Message> result =
            bout.messages().iterate().iterator();
        MatcherAssert.assertThat(
            "expected message not found",
            result.hasNext()
        );
        MatcherAssert.assertThat(
            result.next().text(),
            Matchers.equalTo("    4 leading spaces retained")
        );
    }
}
