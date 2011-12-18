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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default URN resolver.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultUrnResolver implements UrnResolver {

    /**
     * Marker for URL template.
     */
    private static final String MARKER = "{nss}";

    /**
     * The hub.
     */
    private final transient Hub hub;

    /**
     * Namespaces and related URL templates.
     */
    private final transient ConcurrentMap<String, String> inamespaces =
        new ConcurrentHashMap<String, String>();

    /**
     * Public ctor.
     * @param ihub The hub
     */
    public DefaultUrnResolver(final Hub ihub) {
        this.hub = ihub;
        this.inamespaces.put("void", "http://www.netbout.com/");
        this.inamespaces.put("netbout", "http://www.netbout.com/nb");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Identity owner, final String namespace,
        final String template) {
        if (!namespace.matches("^[a-z]{1,31}$")) {
            throw new IllegalArgumentException(
                String.format(
                    "Namespace is not valid '%s'",
                    namespace
                )
            );
        }
        try {
            new URL(template.replace(this.MARKER, "-"));
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Template format is not valid '%s'",
                    template
                ),
                ex
            );
        }
        this.namespaces().put(namespace, template);
        this.hub.bus()
            .make("namespace-was-registered")
            .asap()
            .arg(owner.name())
            .arg(namespace)
            .arg(template)
            .asDefault(false)
            .exec();
        Logger.info(
            this,
            "#register('%s', '%s', '%s'): namespace registered (%d in total)",
            owner.name(),
            namespace,
            template,
            this.namespaces().size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<String, String> registered(final Identity owner) {
        return this.inamespaces;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public URL authority(final Urn urn) throws UnreachableUrnException {
        String url;
        final String nid = urn.nid();
        if (!this.namespaces().containsKey(nid)) {
            throw new UnreachableUrnException(
                urn,
                Logger.format(
                    "Namespace '%s' is not registered among %[list]s",
                    nid,
                    this.namespaces().keySet()
                )
            );
        }
        url = this.namespaces().get(nid).replace(this.MARKER, urn.nss());
        URL result;
        try {
            result = new URL(url);
        } catch (java.net.MalformedURLException ex) {
            throw new UnreachableUrnException(urn, ex);
        }
        Logger.debug(
            this,
            "#authority('%s'): resolved to '%s'",
            urn,
            result
        );
        return result;
    }

    /**
     * Load namespaces and URLs from DB helper.
     * @return The list of namespaces and templates
     */
    private ConcurrentMap<String, String> namespaces() {
        synchronized (this) {
            if (this.inamespaces.size() <= 2) {
                final List<String> names = this.hub.bus()
                    .make("get-all-namespaces")
                    .synchronously()
                    .asDefault(new ArrayList<String>())
                    .exec();
                for (String name : names) {
                    final String template = this.hub.bus()
                        .make("get-namespace-template")
                        .synchronously()
                        .arg(name)
                        .exec();
                    final Urn owner = this.hub.bus()
                        .make("get-namespace-owner")
                        .synchronously()
                        .arg(name)
                        .exec();
                    assert owner != null;
                    this.inamespaces.put(name, template);
                }
                Logger.info(
                    this,
                    "#load(): loaded %d namespaces: %[list]s",
                    this.inamespaces.size(),
                    this.inamespaces.keySet()
                );
            }
            return this.inamespaces;
        }
    }

}
