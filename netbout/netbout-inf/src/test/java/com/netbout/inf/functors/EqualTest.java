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
package com.netbout.inf.functors;

import com.netbout.inf.Atom;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Ray;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.ray.MemRay;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Equal}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class EqualTest {

    /**
     * Equal can find a msg by keyword.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsMessageByKeyword() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long msg = MsgMocker.number();
        final Bout bout = new BoutMocker().mock();
        final Message message = new MessageMocker()
            .withNumber(msg)
            .inBout(bout)
            .mock();
        final Equal functor = new Equal();
        functor.see(
            ray,
            new MessagePostedNotice() {
                @Override
                public Message message() {
                    return message;
                }
            }
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(
                functor.build(
                    ray,
                    Arrays.<Atom<?>>asList(
                        VariableAtom.BOUT_NUMBER,
                        new TextAtom(bout.number().toString())
                    )
                )
            ).msg().number(),
            Matchers.equalTo(msg)
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(
                functor.build(
                    ray,
                    Arrays.<Atom<?>>asList(
                        VariableAtom.BOUT_NUMBER,
                        new TextAtom(Long.toString(msg + 1))
                    )
                )
            ).end(),
            Matchers.equalTo(true)
        );
    }

}