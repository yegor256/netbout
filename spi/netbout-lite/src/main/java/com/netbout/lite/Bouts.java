/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.lite;

import com.jcabi.log.Logger;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Query;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Holder of bouts.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Bouts {

    /**
     * All known bouts and their messages.
     */
    private final transient ConcurrentMap<Long, Messages> all =
        new ConcurrentSkipListMap<Long, Messages>();

    /**
     * Start new bout.
     * @param who Who is staring this bout
     * @return The bout just started
     */
    public Bout start(final Identity who) {
        synchronized (this.all) {
            long num;
            if (this.all.isEmpty()) {
                num = 1;
            } else {
                num = Collections.max(this.all.keySet()) + 1;
            }
            final Messages msgs = new Messages();
            msgs.invite(who);
            this.all.put(num, msgs);
            Logger.info(
                this,
                "#start('%s'): started bout #%d",
                who,
                num
            );
            return new LiteBout(msgs, who, num);
        }
    }

    /**
     * Find bouts by query.
     * @param query Query
     * @param who Who is asking
     * @return Bouts
     */
    public Iterable<Bout> query(final Query query, final Identity who) {
        final List<Bout> bouts = new LinkedList<Bout>();
        for (ConcurrentMap.Entry<Long, Messages> entry : this.all.entrySet()) {
            if (entry.getValue().query(query).iterator().hasNext()) {
                try {
                    bouts.add(this.get(entry.getKey(), who));
                } catch (Identity.BoutNotFoundException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return bouts;
    }

    /**
     * Get bout by number.
     * @param num Bout number
     * @param who Who is staring this bout
     * @return The bout found
     * @throws Identity.BoutNotFoundException If not found
     * @checkstyle RedundantThrows (4 lines)
     */
    public Bout get(final Long num, final Identity who)
        throws Identity.BoutNotFoundException {
        final Messages msg = this.all.get(num);
        if (msg == null) {
            throw new Identity.BoutNotFoundException(num);
        }
        return new LiteBout(msg, who, num);
    }

}
