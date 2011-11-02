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
package com.netbout.spi.stub;

import com.netbout.spi.Helper;
import com.netbout.spi.OperationFailureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Chain of execution.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ChainedHelper implements Helper {

    /**
     * Fallback value.
     */
    private final Object fallback;

    /**
     * Was it started already?
     */
    private boolean started;

    /**
     * List of helpers.
     */
    private final List<Helper> helpers = new ArrayList<Helper>();

    /**
     * Public ctor.
     * @param val The fallback value
     */
    public ChainedHelper(final Object val) {
        this.fallback = val;
    }

    /**
     * Add new helper.
     * @param helper The helper
     */
    public void add(final Helper helper) {
        this.helpers.add(helper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> supports() {
        final Collection<String> mnemos = new ArrayList<String>();
        for (Helper helper : this.helpers) {
            mnemos.addAll(helper.supports());
        }
        return mnemos;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public <T> T execute(final String mnemo, final Class<T> type,
        final Object... args) throws OperationFailureException {
        final boolean dup = ChainedHelperProvider.INSTANCE.isDuplicate(
            this,
            this.hash(mnemo, type, args)
        );
        if (dup) {
            return (T) this.fallback;
        }
        this.started = true;
        for (Helper helper : this.helpers) {
            if (!helper.supports().contains(mnemo)) {
                continue;
            }
            return (T) helper.execute(mnemo, type, args);
        }
        throw new IllegalArgumentException("Operation not supported");
    }

    /**
     * Calculate unique hash code of these args.
     * @param mnemo The mnemo
     * @param type The type
     * @param args Arguments
     * @return The hash (unique string for these three args)
     */
    private String hash(final String mnemo, final Class type,
        final Object... args) {
        return String.format(
            "%s:%s:%s",
            mnemo,
            type.getName(),
            StringUtils.join(args, ":")
        );
    }

}
