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

import com.netbout.spi.Bout;
import com.netbout.spi.Helper;
import com.netbout.spi.Participant;
import com.ymock.util.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 * Executor of operation.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ChainedHelperFactory {

    /**
     * The singleton.
     */
    public static final ChainedHelperFactory INSTANCE =
        new ChainedHelperFactory();

    /**
     * List of running chains.
     */
    private final Map<ChainedHelper, String> chains =
        new HashMap<ChainedHelper, String>();

    /**
     * Private ctor.
     */
    private ChainedHelperFactory() {
        // intentionally empty
    }

    /**
     * This hash code already exists? If yes, we remove the chain from the list.
     * @param chain The chain
     * @param hash Hash code
     * @return It's a duplicate?
     */
    public boolean isDuplicate(final ChainedHelper chain, final String hash) {
        ChainedHelper dup = null;
        for (Map.Entry<ChainedHelper, String> entry : this.chains.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(hash)) {
                dup = entry.getKey();
            }
        }
        boolean response;
        if (dup == null) {
            this.chains.put(chain, hash);
            response = false;
        } else {
            this.chains.remove(chain);
            response = true;
        }
        Logger.info(
            this,
            "#isDuplicate('%s'): %b",
            hash,
            response
        );
        return response;
    }

    /**
     * Create helper chain for specified bout.
     * @param bout The bout
     * @param fallback Fallback value to return
     * @return The chain to execute an operation
     */
    public ChainedHelper local(final Bout bout, final Object fallback) {
        final ChainedHelper chain = new ChainedHelper(fallback);
        for (Participant dude : bout.participants()) {
            final Helper helper =
                ((SimpleIdentity) dude.identity()).getHelper();
            if (helper != null) {
                chain.add(helper);
            }
        }
        this.chains.put(chain, null);
        Logger.info(
            this,
            "#local('%s', '%s'): created",
            bout.title(),
            fallback
        );
        return chain;
    }

}
