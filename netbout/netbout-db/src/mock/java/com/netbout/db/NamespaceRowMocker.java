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

/**
 * Mocker of {@code NAMESPACE} row in a database.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NamespaceRowMocker {

    /**
     * Owner of namespace.
     */
    private transient Urn identity;

    /**
     * Name of namespace.
     */
    private transient String name = "foo";

    /**
     * Template of it.
     */
    private transient String template = "http://localhost/foo";

    /**
     * Public ctor.
     */
    public NamespaceRowMocker() {
        this.identity = new IdentityRowMocker().mock();
    }

    /**
     * With this name.
     * @param nam The name
     * @return THis object
     */
    public NamespaceRowMocker namedAs(final String nam) {
        this.name = nam;
        return this;
    }

    /**
     * With this owner.
     * @param txt The owner
     * @return This object
     */
    public NamespaceRowMocker withOwner(final Urn txt) {
        this.identity = txt;
        return this;
    }

    /**
     * With this template.
     * @param txt The name
     * @return This object
     */
    public NamespaceRowMocker withTemplate(final String txt) {
        this.template = txt;
        return this;
    }

    /**
     * Mock it and return its name.
     * @return Just created namespace
     */
    public String mock() {
        final NamespaceFarm farm = new NamespaceFarm();
        farm.namespaceWasRegistered(
            this.identity,
            this.name,
            this.template
        );
        return this.name;
    }

}
