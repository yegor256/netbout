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
package com.netbout.inf;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.List;

/**
 * Infinity, with information about bouts and messages.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Infinity {

    /**
     * Find bundles and group them.
     * @param identity Where to search for them
     * @param query The predicate to use
     * @return The list of groups
     */
    List<Bundle> bundles(String query);

    /**
     * Find bouts for the given predicate.
     * @param identity Where to search for them
     * @param query The predicate to use
     * @return The list of bouts, ordered
     */
    List<Long> bouts(String query);

    /**
     * Find messages for the given predicate.
     * @param identity Where to search for them
     * @param bout Where to search for them
     * @param query The predicate to use
     * @return The list of messages, ordered
     */
    List<Long> messages(String query);

    /**
     * Update information about this identity
     * (something was changed there, maybe).
     * @param identity The identity to inform about
     */
    void see(Identity identity);

    /**
     * Update information about this bout (something was changed there, maybe).
     * @param bout The bout to inform about
     */
    void see(Bout bout);

    /**
     * Update information about this message.
     * @param message The message to inform about
     */
    void see(Message message);

}
