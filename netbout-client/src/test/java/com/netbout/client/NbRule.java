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
package com.netbout.client;

import com.netbout.client.cached.CdUser;
import com.netbout.spi.User;
import java.net.URI;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * User rule.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.1
 * @todo #1012:30min No public static methods are allowed. This class should
 *  be refactored to not contain any of them. The method get() should either be
 *  an instance method with refrence to this (to avoid checkstyle
 *  NonStaticMethodCheck) or a static but not public.
 */
public final class NbRule implements TestRule {

    /**
     * Get user.
     * @return User
     */
    public static User get() {
        final String token = System.getProperty("netbout.token");
        final URI url = URI.create(
            System.getProperty("netbout.url", "http://www.netbout.com")
        );
        Assume.assumeNotNull(token);
        Assume.assumeTrue(!token.isEmpty());
        return new CdUser(new RtUser(url, token));
    }

    @Override
    public Statement apply(final Statement stmt, final Description desc) {
        return stmt;
    }
}
