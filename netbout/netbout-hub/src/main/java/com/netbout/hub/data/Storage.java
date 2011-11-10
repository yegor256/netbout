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
package com.netbout.hub.data;

import com.netbout.hub.HelpQueue;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage of data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Storage {

    /**
     * The singleton.
     */
    public static final Storage INSTANCE = new Storage();

    /**
     * All bouts existing in the system.
     */
    private final Map<Long, BoutData> bouts = new HashMap<Long, BoutData>();

    /**
     * Private ctor.
     */
    private Storage() {
        // empty
    }

    /**
     * Create new bout in the storage.
     * @return It's number (unique)
     */
    public Long create() {
        final Long max = HelpQueue.exec(
            "get-next-bout-number",
            Long.class,
            HelpQueue.SYNCHRONOUSLY
        );
        final BoutData data = new BoutData();
        data.setNumber(max);
        this.bouts.put(max, data);
        HelpQueue.exec(
            "start-new-bout",
            Boolean.class,
            HelpQueue.SYNCHRONOUSLY,
            max
        );
        Logger.info(
            this,
            "#create(): bout #%d created",
            max
        );
        return max;
    }

    /**
     * Find and return bout from collection.
     * @param num Number of the bout
     * @return The bout found
     * @throws BoutNotFoundException If this bout is not found
     * @checkstyle RedundantThrows (4 lines)
     */
    public BoutData find(final Long num) throws BoutNotFoundException {
        if (!this.bouts.containsKey(num)) {
            final Boolean exists = HelpQueue.exec(
                "check-bout-existence",
                Boolean.class,
                HelpQueue.SYNCHRONOUSLY,
                num
            );
            if (exists) {
                final BoutData data = new BoutData();
                data.setNumber(num);
                this.bouts.put(num, data);
                return data;
            }
            throw new BoutNotFoundException(
                "Bout #%d doesn't exist",
                num
            );
        }
        Logger.info(
            this,
            "#find(#%d): bout found",
            num
        );
        return this.bouts.get(num);
    }

}
