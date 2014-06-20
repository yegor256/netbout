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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.util.NoSuchElementException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo inbox.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "self")
@EqualsAndHashCode(of = { "counter", "region", "self" })
final class DyInbox implements Inbox {

    /**
     * Counter with bout number.
     */
    private final transient Counter counter;

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Alias of myself.
     */
    private final transient String self;

    /**
     * Start moment.
     */
    private final transient long since;

    /**
     * Ctor.
     * @param reg Region we're in
     * @param slf My alias
     */
    DyInbox(final Region reg, final String slf) {
        this(reg, slf, DyInbox.sttc(), System.currentTimeMillis() << 1);
    }

    /**
     * Ctor.
     * @param reg Region we're in
     * @param slf My alias
     * @param ctr Counter
     * @param strt Start
     * @since 2.7.1
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    DyInbox(final Region reg, final String slf, final Counter ctr,
        final long strt) {
        this.region = reg;
        this.self = slf;
        this.counter = ctr;
        this.since = strt;
    }

    @Override
    public long start() throws IOException {
        final long number = this.counter.incrementAndGet(1L);
        this.region.table(DyFriends.TBL).put(
            new Attributes()
                .with(DyFriends.RANGE, this.self)
                .with(DyFriends.HASH, number)
                .with(DyFriends.ATTR_UPDATED, System.currentTimeMillis())
                .with(DyFriends.ATTR_TITLE, "untitled")
        );
        return number;
    }

    /**
     * {@inheritDoc}
     * @todo #1 DynamoDBLocal doesn't work with all attributes
     *  in global index. If we remove this check for a local version,
     *  most tests fail. I'm not sure how to fix that. I suspect, it's
     *  a bug in DynamoDBLocal, but I don't even know how to report
     *  it to them :( Anyway, let's try to investigate and either fix
     *  property or introduce a better workaround. Pay attention that this
     *  code works correctly in production.
     */
    @Override
    public long unread() throws IOException {
        long unread = 0L;
        if (!"1.0-LOCAL".equals(Manifests.read("Netbout-Version"))) {
            final Iterable<Item> items = this.region.table(DyFriends.TBL)
                .frame()
                .where(DyFriends.RANGE, this.self)
                .through(
                    new QueryValve()
                        .withIndexName(DyFriends.INDEX)
                        .withConsistentRead(false)
                        .withSelect(Select.SPECIFIC_ATTRIBUTES)
                        .withAttributesToGet(DyFriends.ATTR_UNREAD)
                        .withScanIndexForward(false)
                );
            for (final Item item : items) {
                if (item.has(DyFriends.ATTR_UNREAD)) {
                    unread += Long.parseLong(
                        item.get(DyFriends.ATTR_UNREAD).getN()
                    );
                }
            }
        }
        return unread;
    }

    @Override
    public Bout bout(final long number) throws Inbox.BoutNotFoundException {
        try {
            return new DyBout(
                this.region,
                this.region.table(DyFriends.TBL)
                    .frame()
                    .through(
                        new QueryValve().withLimit(1)
                            .withSelect(Select.SPECIFIC_ATTRIBUTES)
                            .withAttributesToGet(
                                DyFriends.HASH, DyFriends.RANGE
                            )
                    )
                    .where(DyFriends.HASH, Conditions.equalTo(number))
                    .where(DyFriends.RANGE, this.self)
                    .iterator().next(),
                this.self
            );
        } catch (final NoSuchElementException ex) {
            throw new Inbox.BoutNotFoundException(number, ex);
        }
    }

    @Override
    public Pageable<Bout> jump(final long number) {
        return new DyInbox(this.region, this.self, this.counter, number);
    }

    @Override
    public Iterable<Bout> iterate() {
        return Iterables.transform(
            this.region.table(DyFriends.TBL)
                .frame()
                .where(DyFriends.RANGE, this.self)
                .where(
                    DyFriends.ATTR_UPDATED,
                    new Condition()
                        .withComparisonOperator(ComparisonOperator.LT)
                        .withAttributeValueList(
                            new AttributeValue().withN(
                                Long.toString(this.since)
                            )
                        )
                )
                .through(
                    new QueryValve()
                        .withIndexName(DyFriends.INDEX)
                        .withConsistentRead(false)
                        .withLimit(Inbox.PAGE)
                        .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                        .withScanIndexForward(false)
                ),
            new Function<Item, Bout>() {
                @Override
                public Bout apply(final Item item) {
                    return new Bout.ReadOnly(
                        new DyBout(
                            DyInbox.this.region,
                            item, DyInbox.this.self
                        )
                    );
                }
            }
        );
    }

    /**
     * Sttc counter.
     * @return Counter
     */
    @Cacheable(forever = true)
    private static Counter sttc() {
        try {
            return RtSttc.make(
                URN.create(Manifests.read("Netbout-SttcUrn")),
                Manifests.read("Netbout-SttcToken")
            ).counters().get("nb-bout");
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
