/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.dynamo;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo messages.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "number")
@EqualsAndHashCode(of = { "region", "number" })
final class SmartBout {

    /**
     * Region.
     */
    private final transient Region region;

    /**
     * Number of the bout.
     */
    private final transient long number;

    /**
     * Ctor.
     * @param reg Region
     * @param num Bout number
     */
    SmartBout(final Region reg, final long num) {
        this.region = reg;
        this.number = num;
    }

    /**
     * It was updated just now.
     * @param alias Who updated it
     */
    public void updated(final String alias) {
        assert alias != null;
        Iterables.all(
            this.region.table(DyFriends.TBL).frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.number)),
            new Predicate<Item>() {
                @Override
                public boolean apply(final Item input) {
                    try {
                        input.put(
                            new AttributeUpdates().with(
                                DyFriends.ATTR_UPDATED,
                                System.currentTimeMillis()
                            )
                        );
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    return true;
                }
            }
        );
    }

}
