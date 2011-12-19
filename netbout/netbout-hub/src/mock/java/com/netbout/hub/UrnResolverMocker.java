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

import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mocker of {@link UrnResolver}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UrnResolverMocker {

    /**
     * The object.
     */
    private final transient UrnResolver resolver =
        new UrnResolverMocker.Resolver();

    /**
     * Resolve this namespace to the given URL.
     * @param name The namespace
     * @param url The URL to return
     * @return This object
     */
    public UrnResolverMocker resolveAs(final String name, final URL url) {
        this.resolver.register(null, name, url.toString());
        return this;
    }

    /**
     * Build it.
     * @return The resolver
     */
    public UrnResolver mock() {
        return this.resolver;
    }

    /**
     * Resolver.
     */
    public static final class Resolver implements UrnResolver {
        /**
         * Namespaces and related URL templates.
         */
        private final transient ConcurrentMap<String, URL> namespaces =
            new ConcurrentHashMap<String, URL>();
        /**
         * {@inheritDoc}
         */
        @Override
        public void register(final Identity owner, final String namespace,
            final String url) {
            try {
                this.namespaces.put(namespace, new URL(url));
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentMap<String, String> registered(final Identity owner) {
            return null;
        }
        /**
         * {@inheritDoc}
         * @checkstyle RedundantThrows (3 lines)
         */
        @Override
        public URL authority(final Urn urn) throws UnreachableUrnException {
            final String nid = urn.nid();
            if (!this.namespaces.containsKey(nid)) {
                throw new UnreachableUrnException(
                    urn,
                    Logger.format(
                        "Namespace '%s' is not registered among %[list]s",
                        nid,
                        this.namespaces.keySet()
                    )
                );
            }
            return this.namespaces.get(nid);
        }
    }

}
