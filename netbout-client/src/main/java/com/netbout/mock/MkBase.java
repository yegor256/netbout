/**
 * Copyright (c) 2009-2015, netbout.com
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
package com.netbout.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.User;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mock base.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "sql")
public final class MkBase implements Base {

    /**
     * Randomizer.
     */
    private static final Random RANDOM = new SecureRandom();

    /**
     * SQL data source provider.
     */
    private final transient Sql sql;

    /**
     * Public ctor.
     * @throws IOException If fails
     */
    public MkBase() throws IOException {
        this.sql = new H2Sql();
    }

    @Override
    public User user(final URN urn) {
        return new MkUser(this.sql, urn);
    }

    @Override
    public void close() {
        // nothing to do
    }

    /**
     * Random alias.
     * @return Alias
     * @throws IOException If fails
     */
    public Alias randomAlias() throws IOException {
        return this.alias(
            String.format(
                "alias%d", MkBase.RANDOM.nextInt(Integer.MAX_VALUE)
            )
        );
    }

    /**
     * Random bout.
     * @return Bout
     * @throws IOException If fails
     */
    public Bout randomBout() throws IOException {
        final Inbox inbox = this.randomAlias().inbox();
        final Bout bout = inbox.bout(inbox.start());
        bout.rename(
            String.format(
                "random title %d", MkBase.RANDOM.nextInt(Integer.MAX_VALUE)
            )
        );
        return bout;
    }
    /**
     * Create alias.
     * @param name Name of the alias
     * @return Alias
     * @throws IOException If fails
     */
    public Alias alias(final String name) throws IOException {
        final User user = this.user(
            URN.create(
                String.format(
                    "urn:test:%d",
                    MkBase.RANDOM.nextInt(Integer.MAX_VALUE)
                )
            )
        );
        final Aliases aliases = user.aliases();
        aliases.add(name);
        final Alias alias = aliases.iterate().iterator().next();
        alias.email(String.format("%s@example.com", alias.name()));
        return alias;
    }

}
