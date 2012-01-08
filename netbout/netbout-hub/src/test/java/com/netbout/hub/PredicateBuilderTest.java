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
package com.netbout.hub;

import com.netbout.spi.MessageMocker;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case of {@link PredicateBuilder}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicateBuilderTest {

    /**
     * PredicateBuilder can parse different formats.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void parsesDifferentQueriesWithoutProblems() throws Exception {
        final String[] queries = new String[] {
            "",
            "it's my story: \"\n\t\r \u0435\"",
            "(and 1)",
            "(and (equal 1 1) (or (matches $text $date)))",
            "just simple text: \u0435",
        };
        for (String query : queries) {
            new PredicateBuilder().parse(query);
        }
    }

    /**
     * PredicateBuilder can build a predicate from a string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsPredicateFromQuery() throws Exception {
        final PredicateBuilder builder = new PredicateBuilder();
        final String text = "\u043F\u0440\u0438\u0432\u0435";
        final Predicate pred = builder.parse(
            String.format("(and (matches \"%s\" $text) (equal $pos 0))", text)
        );
        MatcherAssert.assertThat(
            "message found",
            (Boolean) pred.evaluate(
                new MessageMocker().withText(text).mock(),
                0
            )
        );
        MatcherAssert.assertThat(
            "message not found",
            !(Boolean) pred.evaluate(
                new MessageMocker().withText("bar").mock(),
                1
            )
        );
    }

    /**
     * PredicateBuilder can build a predicate from a plain text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsPredicateFromText() throws Exception {
        final PredicateBuilder builder = new PredicateBuilder();
        final String text = "\u043F\u0440\u0438\u0432\u0435\u0442";
        final Predicate pred = builder.parse(text);
        MatcherAssert.assertThat(
            "message with text is found",
            (Boolean) pred.evaluate(
                new MessageMocker().withText(text).mock(),
                0
            )
        );
        MatcherAssert.assertThat(
            "message without text is not found",
            !(Boolean) pred.evaluate(
                new MessageMocker().withText("some text").mock(),
                0
            )
        );
    }

}
