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
package com.netbout.rest;

import com.netbout.rest.period.Period;
import com.netbout.rest.period.PeriodsBuilder;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.XhtmlMatchers;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link InboxRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class InboxRsTest {

    /**
     * InboxRs can render front page of the inbox.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void rendersInboxFrontPage() throws Exception {
        final Urn name = new UrnMocker().mock();
        final IdentityMocker imocker = new IdentityMocker().namedAs(name);
        final Calendar cal = new GregorianCalendar();
        final long total = Period.MAX * 2 + 1;
        for (long num = total; num > 0; num -= 1) {
            cal.add(Calendar.MILLISECOND, -Math.abs(new Random().nextInt()));
            final Date date = cal.getTime();
            imocker.withBout(
                num,
                new BoutMocker()
                    .withNumber(num)
                    .withDate(date)
                    .withParticipant(name)
                    .withMessage(new MessageMocker().withDate(date).mock())
                    .mock()
            );
        }
        final InboxRs rest = new NbResourceMocker()
            .withIdentity(imocker.mock())
            .mock(InboxRs.class);
        final Response response = rest.inbox(null);
        MatcherAssert.assertThat(
            NbResourceMocker.the((NbPage) response.getEntity(), rest),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/page/bouts[count(bout)>1]"),
                XhtmlMatchers.hasXPath(
                    String.format("/page/bouts/bout[number=%d]", total)
                ),
                XhtmlMatchers.hasXPath(
                    String.format(
                        "/page/periods[count(link)=%d]",
                        PeriodsBuilder.MAX_LINKS
                    )
                ),
                XhtmlMatchers.hasXPath("/page/periods/link[@rel='more']"),
                XhtmlMatchers.hasXPath("/page/periods/link[@rel='earliest']"),
                XhtmlMatchers.hasXPath("//link[@rel='more']")
            )
        );
    }

    /**
     * InboxRs can start new bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void startsNewBout() throws Exception {
        final InboxRs rest = new NbResourceMocker().mock(InboxRs.class);
        final Response response = rest.start();
        MatcherAssert.assertThat(
            response.getStatus(),
            Matchers.equalTo(Response.Status.SEE_OTHER.getStatusCode())
        );
    }

}
