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
import java.util.Random;
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
     * Public ctor.
     * @throws Exception If some problem inside
     */
    public IdentityMocker() throws Exception {
        final String name = String.valueOf(Math.abs(new Random().nextInt()));
        Mockito.doReturn(name).when(this.identity).name();
        Mockito.doReturn(name).when(this.identity).user();
        Mockito.doReturn(new BoutMocker().mock()).when(this.identity).start();
        Mockito.doReturn(new BoutMocker().mock()).when(this.identity)
            .bout(Mockito.any(Long.class));
    }

    /**
     * This is the name of identity.
     * @param The name of it
     * @return This object
     */
    public IdentityMocker namedAs(final String name) {
        Mockito.doReturn(name).when(this.identity).name();
        return this;
    }

    /**
     * This is the user of identity, which it belongs to.
     * @param The name of user
     * @return This object
     */
    public IdentityMocker belongsTo(final String name) {
        Mockito.doReturn(name).when(this.identity).user();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked identity
     * @throws Exception If some problem inside
     */
    public Identity mock() throws Exception {
        Mockito.doReturn(new URL("http://localhost/unknown.png"))
            .when(this.identity).photo();
        return this.identity;
    }

}
