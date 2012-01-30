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

import com.netbout.hub.Hub;
import com.netbout.spi.MessageNotFoundException;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Searcher, which keeps all data in memory.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MemInfinity implements Infinity {

    /**
     * Find bouts for the given predicate.
     * @param predicate The predicate to use
     * @return The list of bouts, ordered
     */
    List<Long> bouts(final Predicate predicate);

    /**
     * Find messages for the given predicate.
     * @param predicate The predicate to use
     * @return The list of messages, ordered
     */
    List<Long> messages(final Predicate predicate);

    // /**
    //  * Filter list of messages with a predicate.
    //  * @param list The list to filter
    //  * @param query The query
    //  * @return New list of them
    //  */
    // @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    // public List<Message> filter(final List<Message> list,
    //     final String query) {
    //     final List<Message> result = new ArrayList<Message>();
    //     final Predicate predicate = new PredicateBuilder(this.hub).parse(query);
    //     for (Message msg : list) {
    //         boolean visible = true;
    //         if (!query.isEmpty()) {
    //             final Object response = predicate.evaluate(msg, result.size());
    //             if (response instanceof Boolean) {
    //                 visible = (Boolean) response;
    //             } else if (response instanceof String) {
    //                 result.add(new PlainMessage(this, (String) response));
    //                 break;
    //             } else {
    //                 throw new IllegalArgumentException(
    //                     Logger.format(
    //                         "Can't understand %[type]s response from '%s'",
    //                         response,
    //                         query
    //                     )
    //                 );
    //             }
    //         }
    //         if (visible) {
    //             result.add(msg);
    //         }
    //     }
    //     if (list.isEmpty()) {
    //         final Object response = predicate.evaluate(
    //             new PlainMessage(this, ""), 0
    //         );
    //         if (response instanceof String) {
    //             result.add(new PlainMessage(this, (String) response));
    //         }
    //     }
    //     return result;
    // }

}
