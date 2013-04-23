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
package com.netbout.hub;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.spi.Identity;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link URNResolver}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class URNResolverMocker {

    /**
     * The object.
     */
    private final transient URNResolver resolver =
        Mockito.mock(URNResolver.class);

    /**
     * Namespaces and related URL templates.
     */
    private final transient ConcurrentMap<String, URL> namespaces =
        new ConcurrentHashMap<String, URL>();

    /**
     * Public ctor.
     */
    public URNResolverMocker() {
        final Answer<URL> answer = new Answer<URL>() {
            public URL answer(final InvocationOnMock invocation) {
                final URN urn = (URN) invocation.getArguments()[0];
                final String nid = urn.nid();
                if (!URNResolverMocker.this.namespaces.containsKey(nid)) {
                    throw new IllegalArgumentException(
                        Logger.format(
                            "NID of '%s' is not registered among %[list]s",
                            urn,
                            URNResolverMocker.this.namespaces.keySet()
                        )
                    );
                }
                return URNResolverMocker.this.namespaces.get(nid);
            }
        };
        try {
            Mockito.doAnswer(answer).when(this.resolver)
                .authority(Mockito.any(URN.class));
        } catch (Identity.UnreachableURNException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Resolve this namespace to the given URL.
     * @param name The namespace
     * @param url The URL to return
     * @return This object
     */
    public URNResolverMocker resolveAs(final String name, final URL url) {
        this.namespaces.put(name, url);
        return this;
    }

    /**
     * Build it.
     * @return The resolver
     */
    public URNResolver mock() {
        return this.resolver;
    }

}
