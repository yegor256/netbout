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
package com.netbout.harness;

import com.netbout.spi.Identity;
import com.netbout.utils.Cryptor;
import java.util.Random;
import org.mockito.Mockito;

/**
 * Builds a mocked cookie for test requests.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CookieBuilder {

    /**
     * It's a utility class.
     */
    private CookieBuilder() {
        // empty
    }

    /**
     * Build cookie.
     * @return The cookie
     */
    public static String cookie() {
        return String.format("netbout=%s", CookieBuilder.auth());
    }

    /**
     * Build auth code.
     * @return The auth code
     */
    public static String auth() {
        return CookieBuilder.auth(
            String.valueOf(Math.abs(new Random().nextLong()))
        );
    }

    /**
     * Build auth code, for the identity specified.
     * @param name Identity name
     * @return The auth code
     */
    public static String auth(final String name) {
        final Identity identity = Mockito.mock(Identity.class);
        final Random random = new Random();
        final String number = String.valueOf(Math.abs(random.nextLong()));
        Mockito.doReturn(name).when(identity).name();
        Mockito.doReturn(name).when(identity).user();
        return new Cryptor().encrypt(identity);
    }

}
