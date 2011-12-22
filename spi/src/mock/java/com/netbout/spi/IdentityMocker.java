/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.mockito.Mockito;

/**
 * Mocker of {@link Identity}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class IdentityMocker {

    /**
     * Mocked identity.
     */
    private final Identity identity = Mockito.mock(Identity.class);

    /**
     * Aliases.
     */
    private final Set<String> aliases = new HashSet<String>();

    /**
     * Public ctor.
     */
    public IdentityMocker() {
        this.namedAs(new UrnMocker().mock());
        this.withAlias("test identity alias");
        this.belongsTo("http://localhost/some-authority");
        this.withPhoto("http://localhost/unknown.png");
        Mockito.doReturn(this.aliases).when(this.identity).aliases();
        Mockito.doReturn(new BoutMocker().mock()).when(this.identity).start();
        try {
            Mockito.doReturn(new BoutMocker().mock()).when(this.identity)
                .bout(Mockito.any(Long.class));
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * This is the name of identity.
     * @param The name of it
     * @return This object
     */
    public IdentityMocker namedAs(final String name) {
        return this.namedAs(Urn.create(name));
    }

    /**
     * This is the name of identity.
     * @param The name of it
     * @return This object
     */
    public IdentityMocker namedAs(final Urn name) {
        Mockito.doReturn(name).when(this.identity).name();
        return this;
    }

    /**
     * This is the user of identity, which it belongs to.
     * @param The name of user
     * @return This object
     */
    public IdentityMocker belongsTo(final String name) {
        try {
            return this.belongsTo(new URL(name));
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * This is the user of identity, which it belongs to.
     * @param The name of user
     * @return This object
     */
    public IdentityMocker belongsTo(final URL name) {
        Mockito.doReturn(name).when(this.identity).authority();
        return this;
    }

    /**
     * With this alias.
     * @param alias The alias
     * @return This object
     */
    public IdentityMocker withAlias(final String alias) {
        this.aliases.add(alias);
        return this;
    }

    /**
     * With this photo.
     * @param photo The photo
     * @return This object
     */
    public IdentityMocker withPhoto(final String photo) {
        try {
            Mockito.doReturn(new URL(photo)).when(this.identity).photo();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * With this bout on board.
     * @param num Number of it
     * @param bout The bout
     * @return This object
     * @throws BoutNotFoundException If some problem
     */
    public IdentityMocker withBout(final Long num, final Bout bout) {
        try {
            Mockito.doReturn(bout).when(this.identity).bout(num);
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Mock it.
     * @return Mocked identity
     */
    public Identity mock() {
        return this.identity;
    }

}
