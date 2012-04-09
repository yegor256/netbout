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
package com.netbout.db;

import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Mocker of {@code IDENTITY} row in a database.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class IdentityRowMocker {

    /**
     * Aliases to add.
     */
    private final transient Set<String> aliases = new HashSet<String>();

    /**
     * Name of identity.
     */
    private transient Urn identity;

    /**
     * Public ctor.
     */
    public IdentityRowMocker() {
        this.identity = new UrnMocker().mock();
    }

    /**
     * With this name.
     * @param name The name
     * @return THis object
     */
    public IdentityRowMocker namedAs(final String name) {
        return this.namedAs(Urn.create(name));
    }

    /**
     * With this name.
     * @param name The name
     * @return THis object
     */
    public IdentityRowMocker namedAs(final Urn name) {
        this.identity = name;
        return this;
    }

    /**
     * With this alias on board.
     * @param name The alias
     * @return THis object
     */
    public IdentityRowMocker withAlias(final String name) {
        this.aliases.add(name);
        return this;
    }

    /**
     * Mock it and return its URN.
     * @return Name of just mocked identity
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Urn mock() {
        final IdentityFarm farm = new IdentityFarm();
        try {
            farm.identityMentioned(this.identity);
            farm.changedIdentityPhoto(
                this.identity,
                new URL(
                    String.format(
                        "http://localhost/%d",
                        new Random().nextLong()
                    )
                )
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        for (String alias : this.aliases) {
            new AliasRowMocker(this.identity).namedAs(alias).mock();
        }
        return this.identity;
    }

}
