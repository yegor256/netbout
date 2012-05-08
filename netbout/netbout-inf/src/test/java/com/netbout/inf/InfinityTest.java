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
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.SimpleXml;
import com.rexsl.test.XmlDocument;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Infinity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class InfinityTest {

    /**
     * Infinity can consume messages and then find them.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void consumesMessagesAndFindsThem() throws Exception {
        final XmlDocument xml = new SimpleXml(
            this.getClass().getResourceAsStream("scenario.xml")
        );
        for (XmlDocument scene : xml.nodes("/scenario/scene")) {
            final Infinity inf = this.prepare(scene);
            for (XmlDocument query : scene.nodes("query")) {
                MatcherAssert.assertThat(
                    inf.messages(query.xpath("predicate/text()").get(0)),
                    Matchers.contains(
                        InfinityTest.numbers(
                            query.xpath("messages/text()").get(0)
                        )
                    )
                );
            }
            inf.close();
        }
    }

    /**
     * Prepare infinity.
     * @param xml XML config
     * @return Infinity
     * @throws Exception If there is some problem inside
     */
    private Infinity prepare(final XmlDocument xml) throws Exception {
        final Infinity inf = new DefaultInfinity(new FolderMocker().mock());
        for (XmlDocument see : xml.nodes("see")) {
            final Urn[] deps = inf.see(this.notice(see)).toArray(new Urn[0]);
            while (inf.eta(deps) > 0) {
                TimeUnit.MILLISECONDS.sleep(2);
                Logger.debug(this, "eta=%dms", inf.eta(deps));
            }
        }
        return inf;
    }

    /**
     * Convert XML to notice.
     * @param xml XML config
     * @return The notice
     * @throws Exception If there is some problem inside
     */
    private Notice notice(final XmlDocument xml) throws Exception {
        final Class type = Class.forName(xml.xpath("@notice").get(0));
        final Properties props = this.properties(xml);
        Notice notice;
        if (type.equals(MessagePostedNotice.class)) {
            notice = new MessagePostedNotice() {
                @Override
                public Message message() {
                    return InfinityTest.message(props);
                }
            };
        } else {
            throw new IllegalArgumentException(type.getName());
        }
        return notice;
    }

    /**
     * Convert XML to properties.
     * @param xml XML config
     * @return The properties
     */
    private Properties properties(final XmlDocument xml) {
        final Properties props = new Properties();
        for (XmlDocument param : xml.nodes("child::*")) {
            props.setProperty(
                param.node().getLocalName(),
                param.node().getTextContent().trim()
            );
        }
        return props;
    }

    /**
     * Convert properties to message.
     * @param props The props
     * @return The message
     */
    private static Message message(final Properties props) {
        return new MessageMocker()
            .withText(props.getProperty("message.text", "msg text"))
            .withAuthor(props.getProperty("message.author", "urn:test:John"))
            .withNumber(Long.valueOf(props.getProperty("message.number", "1")))
            .inBout(InfinityTest.bout(props))
            .mock();
    }

    /**
     * Convert properties to bout.
     * @param props The props
     * @return The bout
     */
    private static Bout bout(final Properties props) {
        return new BoutMocker()
            .titledAs(props.getProperty("bout.title", "some title"))
            .withNumber(Long.valueOf(props.getProperty("bout.number", "55")))
            .mock();
    }

    /**
     * Convert text to array of msg numbers.
     * @param text The text
     * @return The array
     */
    private static Long[] numbers(final String text) {
        final String[] parts = text.split("\\s+,\\s+");
        final Long[] nums = new Long[parts.length];
        for (int pos = 0; pos < parts.length; ++pos) {
            nums[pos] = Long.valueOf(parts[pos]);
        }
        return nums;
    }

}
