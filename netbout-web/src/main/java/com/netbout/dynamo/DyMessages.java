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

import co.stateful.Counter;
import co.stateful.RtSttc;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo messages.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "bout")
@EqualsAndHashCode(of = { "region", "bout" })
final class DyMessages implements Messages {

    /**
     * Table name.
     */
    public static final String TBL = "messages";

    /**
     * Bout attribute.
     */
    public static final String HASH = "bout";

    /**
     * Message attribute.
     */
    public static final String RANGE = "message";

    /**
     * Text of the message.
     */
    public static final String ATTR_TEXT = "text";

    /**
     * Author of the message.
     */
    public static final String ATTR_ALIAS = "alias";

    /**
     * Date of the message.
     */
    public static final String ATTR_DATE = "date";

    /**
     * Counter with message number.
     */
    private final transient Counter counter;

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * Bout number.
     */
    private final transient long bout;

    /**
     * My own alias.
     */
    private final transient String self;

    /**
     * Ctor.
     * @param reg Region
     * @param num Bout number
     * @param slf Self alias
     */
    DyMessages(final Region reg, final long num, final String slf) {
        try {
            this.counter = RtSttc.make(
                URN.create(Manifests.read("Netbout-SttcUrn")),
                Manifests.read("Netbout-SttcToken")
            ).counters().get("nb-message");
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        this.region = reg;
        this.bout = num;
        this.self = slf;
    }

    @Override
    public void post(final String text) throws IOException {
        final long number = this.counter.incrementAndGet(1L);
        this.region.table(DyMessages.TBL).put(
            new Attributes()
                .with(DyMessages.HASH, this.bout)
                .with(DyMessages.RANGE, number)
                .with(DyMessages.ATTR_TEXT, text)
                .with(DyMessages.ATTR_ALIAS, this.self)
                .with(DyMessages.ATTR_DATE, System.currentTimeMillis())
        );
        new SmartBout(this.region, this.bout).updated(this.self);
    }

    @Override
    public Pageable<Message> jump(final int idx) {
        throw new UnsupportedOperationException("#jump()");
    }

    @Override
    public Iterable<Message> iterate() {
        return Iterables.transform(
            this.region.table(DyMessages.TBL)
                .frame()
                .through(
                    new QueryValve()
                        .withScanIndexForward(false)
                        .withAttributesToGet(
                            DyMessages.ATTR_TEXT,
                            DyMessages.ATTR_ALIAS,
                            DyMessages.ATTR_DATE
                        )
                )
                .where(DyMessages.HASH, Conditions.equalTo(this.bout)),
            new Function<Item, Message>() {
                @Override
                public Message apply(final Item item) {
                    return new DyMessage(item);
                }
            }
        );
    }
}
