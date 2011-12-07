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
package com.netbout.bus.cache;

import com.netbout.bus.TokenCache;
import com.netbout.spi.Plain;
import com.netbout.spi.Token;
import com.ymock.util.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Default implementation of cache of tokens results.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultTokenCache implements TokenCache {

    /**
     * Cached values.
     */
    private final transient ConcurrentMap<Token, Plain<?>> cache =
        new ConcurrentHashMap<Token, Plain<?>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final Token token) {
        if (this.cache.containsKey(token)) {
            final Plain<?> data = this.cache.get(token);
            token.result(data);
            Logger.debug(
                this,
                "#resolve(%s): resolved as %s",
                token,
                data
            );
        } else {
            Logger.debug(
                this,
                "#resolve(%s): nothing found in cache (among %d records)",
                token,
                this.cache.size()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final Token token, final Plain<?> data) {
        this.cache.put(token, data);
        Logger.debug(
            this,
            "#save(%s, %s): saved (%d total records now)",
            token,
            data,
            this.cache.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Pattern pattern) {
        for (Token token : this.cache.keySet()) {
            if (pattern.matcher(token.mnemo()).matches()) {
                this.cache.remove(token);
                Logger.debug(
                    this,
                    "#delete(%s): token %s removed",
                    pattern,
                    token
                );
            }
        }
    }

}
