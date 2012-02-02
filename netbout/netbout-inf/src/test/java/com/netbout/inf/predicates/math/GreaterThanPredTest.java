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
package com.netbout.inf.predicates.math;

import com.netbout.inf.MsgMocker;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateMocker;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

/**
 * Test case of {@link GreaterThanPred}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class GreaterThanPredTest {

    /**
     * GreaterThanPred can compare two numbers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesTwoIntegerNumbers() throws Exception {
        final Long num = new Random().nextLong();
        final Predicate pred = new GreaterThanPred(
            Arrays.asList(
                new Predicate[] {
                    new PredicateMocker().doReturn(num + 1L).mock(),
                    new PredicateMocker().doReturn(num).mock(),
                }
            )
        );
        MatcherAssert.assertThat(
            "matched",
            (Boolean) pred.evaluate(new MsgMocker().mock(), 0)
        );
    }

    /**
     * GreaterThanPred can compare two dates.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesTwoDates() throws Exception {
        final Date date = new Date();
        final Predicate pred = new GreaterThanPred(
            Arrays.asList(
                new Predicate[] {
                    new PredicateMocker().doReturn(date).mock(),
                    new PredicateMocker().doReturn(
                        ISODateTimeFormat.dateTime().print(
                            new DateTime(date.getTime() - 1)
                        )
                    ).mock(),
                }
            )
        );
        MatcherAssert.assertThat(
            "left date is bigger than the right one",
            (Boolean) pred.evaluate(new MsgMocker().mock(), 0)
        );
    }

}
