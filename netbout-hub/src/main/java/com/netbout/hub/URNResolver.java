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

import com.jcabi.urn.URN;
import com.netbout.spi.Identity;
import java.net.URL;
import java.util.Map;

/**
 * URN resolver.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface URNResolver {

    /**
     * Marker for URL template.
     */
    String MARKER = "{nss}";

    /**
     * Get some statistics, for the stage.
     * @return The text
     */
    String statistics();

    /**
     * If exception is already registered.
     */
    class DuplicateNamespaceException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA789EEEE479L;
        /**
         * Public ctor.
         * @param owner Already registered owner
         * @param nsp The namespace
         */
        public DuplicateNamespaceException(final URN owner, final String nsp) {
            super(String.format("'%s' registered by '%s'", nsp, owner));
        }
    }

    /**
     * Register namespace.
     * @param owner Who is registering
     * @param namespace The namespace to register
     * @param template URL template
     * @throws URNResolver.DuplicateNamespaceException If registered
     */
    void register(Identity owner, String namespace, String template)
        throws URNResolver.DuplicateNamespaceException;

    /**
     * Get all namespaces registered for the given identity.
     * @param owner Who is asking
     * @return The list of them, as a map
     */
    Map<String, String> registered(Identity owner);

    /**
     * Resolve URN to URL (get is authority).
     * @param urn The URN
     * @return The authority
     * @throws Identity.UnreachableURNException If we can't reach it
     * @checkstyle RedundantThrows (2 lines)
     */
    URL authority(URN urn) throws Identity.UnreachableURNException;

}
