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
     * @throws Exception If some problem inside
     */
    public IdentityMocker() throws Exception {
        Mockito.doReturn(new UrnMocker().mock()).when(this.identity).name();
        Mockito.doReturn(new URL("http://localhost"))
            .when(this.identity).authority();
        Mockito.doReturn(new BoutMocker().mock()).when(this.identity).start();
        Mockito.doReturn(new BoutMocker().mock()).when(this.identity)
            .bout(Mockito.any(Long.class));
        Mockito.doReturn(new URL("http://localhost/unknown.png"))
            .when(this.identity).photo();
    }

    /**
     * This is the name of identity.
     * @param The name of it
     * @return This object
     * @throws Exception If some problem inside
     */
    public IdentityMocker namedAs(final String name) throws Exception {
        return this.namedAs(new Urn(name));
    }

    /**
     * This is the name of identity.
     * @param The name of it
     * @return This object
     * @throws Exception If some problem inside
     */
    public IdentityMocker namedAs(final Urn name) throws Exception {
        Mockito.doReturn(name).when(this.identity).name();
        return this;
    }

    /**
     * This is the user of identity, which it belongs to.
     * @param The name of user
     * @return This object
     * @throws Exception If some problem inside
     */
    public IdentityMocker belongsTo(final String name) throws Exception {
        return this.belongsTo(new URL(name));
    }

    /**
     * This is the user of identity, which it belongs to.
     * @param The name of user
     * @return This object
     * @throws Exception If some problem inside
     */
    public IdentityMocker belongsTo(final URL name) throws Exception {
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
     * @throws java.net.MalformedURLException When format problem
     */
    public IdentityMocker withPhoto(final String photo)
        throws java.net.MalformedURLException {
        Mockito.doReturn(new URL(photo)).when(this.identity).photo();
        return this;
    }

    /**
     * With this bout on board.
     * @param num Number of it
     * @param bout The bout
     * @return This object
     * @throws BoutNotFoundException If some problem
     */
    public IdentityMocker withBout(final Long num, final Bout bout)
        throws BoutNotFoundException {
        Mockito.doReturn(bout).when(this.identity).bout(num);
        return this;
    }

    /**
     * Mock it.
     * @return Mocked identity
     * @throws Exception If some problem inside
     */
    public Identity mock() throws Exception {
        Mockito.doReturn(this.aliases).when(this.identity).aliases();
        return this.identity;
    }

}
