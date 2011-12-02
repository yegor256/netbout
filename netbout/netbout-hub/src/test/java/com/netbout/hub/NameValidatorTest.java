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

import com.netbout.bus.Bus;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link NameValidator}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NameValidatorTest {

    /**
     * Validator should ask Bus about possibility to validate the identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void asksBusAboutPossibilityToNotifyIdentity() throws Exception {
        final Bus bus = new BusMocker()
            // @checkstyle MultipleStringLiterals (1 line)
            .doReturn(true, "can-notify-identity")
            .mock();
        final NameValidator validator = new NameValidator(bus);
        final String name = "test@example.com";
        MatcherAssert.assertThat(
            validator.ifValid(name),
            Matchers.equalTo(name)
        );
        // @checkstyle MultipleStringLiterals (1 line)
        Mockito.verify(bus).make("can-notify-identity");
    }

    /**
     * Validator should ask about possibility and throw an exception if such
     * a possibility is absent.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = com.netbout.spi.UnreachableIdentityException.class)
    public void asksBusAboutPossibilityAndThrowsException() throws Exception {
        final Bus bus = new BusMocker()
            // @checkstyle MultipleStringLiterals (1 line)
            .doReturn(false, "can-notify-identity")
            .mock();
        final NameValidator validator = new NameValidator(bus);
        final String name = "some-strange-identity-name";
        validator.ifValid(name);
    }

    /**
     * Facebook identity should no lead to any complains.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntComplainAboutFacebookIdentity() throws Exception {
        final Bus bus = Mockito.mock(Bus.class);
        final NameValidator validator = new NameValidator(bus);
        final String name = String.valueOf(Math.abs(new Random().nextLong()));
        MatcherAssert.assertThat(
            validator.ifValid(name),
            Matchers.equalTo(name)
        );
    }

    /**
     * Names reserved for netbout should not lead to problems.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntComplainAboutNetboutReservedNames() throws Exception {
        final Bus bus = Mockito.mock(Bus.class);
        final NameValidator validator = new NameValidator(bus);
        final String name = "nb:some-name-no-matter-what";
        MatcherAssert.assertThat(
            validator.ifValid(name),
            Matchers.equalTo(name)
        );
    }

}
