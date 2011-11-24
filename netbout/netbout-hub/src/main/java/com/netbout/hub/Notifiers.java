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

import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Holder of all notifiers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Notifiers {

    /**
     * All identities known for us at the moment, and their objects.
     */
    private static final Set<HubNotifier> ALL =
        new CopyOnWriteArraySet<HubNotifier>();

    /**
     * It's a utility class.
     */
    private Notifiers() {
        // intentionally empty
    }

    /**
     * Add new notifier.
     * @param notifier The notifier to add
     */
    public static void register(final HubNotifier notifier) {
        Notifiers.ALL.add(notifier);
    }

    /**
     * This identity needs notifier?
     * @param identity The identity
     * @return It needs it?
     */
    public static boolean needsNotifier(final Identity identity) {
        return true;
    }

    /**
     * We can notify this identity?
     * @param identity The identity
     * @return Can we?
     */
    public static boolean canNotify(final Identity identity) {
        return true;
    }

    /**
     * Notify {@link Identity} about new {@link Message}.
     * @param identity The identity who should be notified
     * @param message The message just posted
     */
    public static void notify(final Identity identity, final Message message) {
    }

}
