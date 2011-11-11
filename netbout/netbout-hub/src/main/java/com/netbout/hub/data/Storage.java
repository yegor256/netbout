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

import com.netbout.hub.queue.HelpQueue;
import com.ymock.util.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage of data.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Storage {

    /**
     * All bouts existing in the system.
     */
    private static final Map<Long, BoutData> BOUTS =
        new ConcurrentHashMap<Long, BoutData>();

    /**
     * It's utility class.
     */
    private Storage() {
        //
    }

    /**
     * Create new bout in the storage.
     * @return It's number (unique)
     */
    public static Long create() {
        final Long number = HelpQueue.make("get-next-bout-number")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .exec(Long.class);
        final BoutData data = new BoutData(number);
        data.setTitle("");
        Storage.BOUTS.put(number, data);
        HelpQueue.make("started-new-bout")
            .priority(HelpQueue.Priority.ASAP)
            .arg(number.toString())
            .exec(Boolean.class);
        Logger.debug(
            Storage.class,
            "#create(): bout #%d created",
            number
        );
        return number;
    }

    /**
     * Find and return bout from collection.
     * @param number Number of the bout
     * @return The bout found or restored
     * @throws BoutMissedException If this bout is not found
     */
    public static BoutData find(final Long number) throws BoutMissedException {
        BoutData data;
        if (Storage.BOUTS.containsKey(number)) {
            data = Storage.BOUTS.get(number);
            Logger.debug(
                Storage.class,
                "#find(#%d): bout data found",
                number
            );
        } else {
            final Long exists = HelpQueue.make("check-bout-existence")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(number.toString())
                .exec(Long.class);
            if (exists != number) {
                throw new BoutMissedException(number);
            }
            data = new BoutData(number);
            Storage.BOUTS.put(number, data);
            Logger.debug(
                Storage.class,
                "#find(#%d): bout data restored",
                number
            );
        }
        return data;
    }

}
