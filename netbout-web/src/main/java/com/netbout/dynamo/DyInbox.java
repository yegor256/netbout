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
import com.amazonaws.services.dynamodbv2.model.Select;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
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
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo inbox.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
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
     * Ctor.
     * @param reg Region we're in
     * @param slf My alias
     */
    DyInbox(final Region reg, final String slf) {
        try {
            this.counter = RtSttc.make(
                URN.create(Manifests.read("Netbout-SttcUrn")),
                Manifests.read("Netbout-SttcToken")
            ).counters().get("nb-bout");
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        this.region = reg;
        this.self = slf;
    }

    @Override
    public long start() {
        final long number;
        try {
            number = this.counter.incrementAndGet(1L);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        this.region.table(DyFriends.TBL).put(
            new Attributes()
                .with(DyFriends.RANGE, this.self)
                .with(
                    DyFriends.HASH,
                    new AttributeValue().withN(Long.toString(number))
                )
                .with(DyFriends.ATTR_UPDATED, System.currentTimeMillis())
                .with(DyFriends.ATTR_TITLE, "untitled")
        );
        return number;
    }

    @Override
    public Bout bout(final long number) throws Inbox.BoutNotFoundException {
        try {
            return new DyBout(
                this.region,
                this.region.table(DyFriends.TBL).frame()
                    .through(new QueryValve().withLimit(1))
                    .where(
                        DyFriends.HASH,
                        Conditions.equalTo(
                            new AttributeValue().withN(Long.toString(number))
                        )
                    )
                    .where(DyFriends.RANGE, this.self)
                    .iterator().next(),
                this.self
            );
        } catch (final NoSuchElementException ex) {
            throw new Inbox.BoutNotFoundException(number, ex);
        }
    }

    @Override
    public Pageable<Bout> jump(final int pos) {
        throw new UnsupportedOperationException("#jump()");
    }

    @Override
    public Iterator<Bout> iterator() {
        return Iterators.transform(
            this.region.table(DyFriends.TBL).frame()
                .where(DyFriends.RANGE, this.self)
                .through(
                    new QueryValve()
                        .withIndexName(DyFriends.INDEX)
                        .withConsistentRead(false)
                        .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                        .withScanIndexForward(false)
                )
                .iterator(),
            new Function<Item, Bout>() {
                @Override
                public Bout apply(final Item item) {
                    return new DyBout(
                        DyInbox.this.region,
                        item, DyInbox.this.self
                    );
                }
            }
        );
    }
}
