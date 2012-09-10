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
package com.netbout.inf.functors;

import com.netbout.inf.Atom;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.NumberAtom;
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
 * Test case of {@link Unbundled}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class UnbundledTest {

    /**
     * Unbundled can find un-bundled messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsUnbundledMessages() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long bnum = MsgMocker.number();
        for (int inc = 0; inc < 2; ++inc) {
            final Bout bout = new BoutMocker().withNumber(bnum + inc).mock();
            final long msg = MsgMocker.number();
            ray.msg(msg);
            final MessagePostedNotice notice = new MessagePostedNotice() {
                @Override
                public Message message() {
                    return new MessageMocker()
                        .withNumber(msg)
                        .inBout(bout)
                        .mock();
                }
            };
            new Bundled().see(ray, notice);
            new Equal().see(ray, notice);
        }
        final Term term = new Unbundled().build(
            ray,
            Arrays.asList(new Atom<?>[] {new NumberAtom(bnum)})
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).end(),
            Matchers.is(false)
        );
    }

    /**
     * Unbundled can find nothing when bout number is not correct.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsNothingWithInvalidBoutNumber() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long bnum = MsgMocker.number();
        final Term term = new Unbundled().build(
            ray,
            Arrays.asList(new Atom<?>[] {new NumberAtom(bnum)})
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).end(),
            Matchers.is(true)
        );
    }

}
