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

import com.netbout.bus.TxBuilder;
import com.netbout.inf.Infinity;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import java.io.Closeable;
import java.net.URL;
import java.util.Set;

/**
 * Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Hub extends Closeable {

    /**
     * Find identity by URN.
     * @param name The name of the identity
     * @return The identity found
     * @throws UnreachableUrnException If we can't reach it
     * @checkstyle RedundantThrows (3 lines)
     */
    Identity identity(Urn name) throws UnreachableUrnException;

    /**
     * Get URN resolver.
     * @return The resolver
     */
    UrnResolver resolver();

    /**
     * Start new transaction.
     * @param mnemo Transaction mnemo
     * @return The transaction builder
     */
    TxBuilder make(final String mnemo);

    /**
     * Get manager of bouts.
     * @return The manager
     */
    BoutMgr manager();

    /**
     * Get infinity.
     * @return The infinity
     */
    Infinity infinity();

    /**
     * Promote existing identity to the helper.
     * @param identity The identity to promote
     * @param location The location to use
     * @return The helper just created
     */
    Helper promote(Identity identity, URL location);

    /**
     * Join two identities, make the first one primary one.
     * @param main The identity to make primary
     * @param child The secondary one
     * @return The primary identity
     */
    Identity join(Identity main, Identity child);

    /**
     * Find identities by keyword.
     * @param keyword The keyword
     * @return The identities found
     */
    Set<Identity> findByKeyword(String keyword);

}
