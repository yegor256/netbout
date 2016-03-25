/**
 * Copyright (c) 2009-2016, netbout.com
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
import co.stateful.cached.CdSttc;
import co.stateful.retry.ReSttc;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Async;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.takes.facets.forward.RsFailure;

/**
 * Dynamo messages.
 * @todo #603:30min Refactor DyMessages class to avoid suppressing of
 *  PMD.TooManyMethods and PMD.ExcessiveImports warnings. for example
 *  there are some private static methods there, those could be easily
 *  extracted.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "bout")
@EqualsAndHashCode(of = { "counter", "region", "bout", "self", "start" })
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
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
     * Start.
     */
    private final transient long start;

    /**
     * Ctor.
     * @param reg Region
     * @param num Bout number
     * @param slf Self alias
     */
    DyMessages(final Region reg, final long num, final String slf) {
        this(reg, num, slf, DyMessages.sttc(), Inbox.NEVER);
    }

    /**
     * Ctor.
     * @param reg Region
     * @param num Bout number
     * @param slf Self alias
     * @param ctr Counter
     * @param strt Start message number
     * @since 2.7.1
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    DyMessages(final Region reg, final long num, final String slf,
        final Counter ctr, final long strt) {
        this.region = reg;
        this.bout = num;
        this.self = slf;
        this.counter = ctr;
        this.start = strt;
    }

    @Override
    public void post(final String text) throws IOException {
        final String clean = StringUtils.stripEnd(text, null);
        if (clean.isEmpty()) {
            throw new Messages.BrokenPostException(
                "empty message content is not allowed"
            );
        }
        if (clean.length() > Tv.TEN * Tv.THOUSAND) {
            throw new Messages.BrokenPostException(
                "message is too big, break it into parts or upload attachment"
            );
        }
        final long number = this.counter.incrementAndGet(1L);
        this.region.table(DyMessages.TBL).put(
            new Attributes()
                .with(DyMessages.HASH, this.bout)
                .with(DyMessages.RANGE, number)
                .with(DyMessages.ATTR_TEXT, clean)
                .with(DyMessages.ATTR_ALIAS, this.self)
                .with(DyMessages.ATTR_DATE, System.currentTimeMillis())
        );
        this.updated();
        Logger.info(this, "posted to #%d by @%s", this.bout, this.self);
    }

    @Override
    public long unread() throws IOException {
        final Iterator<Item> iterator = this.region.table(DyFriends.TBL)
            .frame()
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withAttributesToGet(DyFriends.ATTR_UNREAD)
            )
            .where(DyFriends.HASH, Conditions.equalTo(this.bout))
            .where(DyFriends.RANGE, Conditions.equalTo(this.self))
            .iterator();
        if (!iterator.hasNext()) {
            throw new RsFailure(
                new StringBuilder("you're not in bout #")
                .append(this.bout).toString()
            );
        }
        final Item item = iterator.next();
        final long unread;
        if (item.has(DyFriends.ATTR_UNREAD)) {
            unread = Long.parseLong(item.get(DyFriends.ATTR_UNREAD).getN());
        } else {
            unread = 0L;
        }
        return unread;
    }

    @Override
    public Pageable<Message> jump(final long number) {
        return new DyMessages(
            this.region, this.bout, this.self, this.counter, number
        );
    }

    @Override
    public Iterable<Message> iterate() {
        if (this.start != Long.MAX_VALUE) {
            this.seen();
        }
        return Iterables.transform(
            this.region.table(DyMessages.TBL)
                .frame()
                .through(
                    new QueryValve()
                        .withScanIndexForward(false)
                        .withLimit(Messages.PAGE)
                        .withAttributesToGet(
                            DyMessages.ATTR_TEXT,
                            DyMessages.ATTR_ALIAS,
                            DyMessages.ATTR_DATE
                        )
                )
                .where(DyMessages.HASH, Conditions.equalTo(this.bout))
                .where(
                    DyMessages.RANGE,
                    new Condition()
                        .withComparisonOperator(ComparisonOperator.LT)
                        .withAttributeValueList(
                            new AttributeValue().withN(
                                Long.toString(this.start)
                            )
                        )
                ),
            new Function<Item, Message>() {
                @Override
                public Message apply(final Item item) {
                    return new DyMessage(item);
                }
            }
        );
    }

    @Override
    public Iterable<Message> search(final String term) throws IOException {
        final List<Message> result = new ArrayList<>(16);
        for (final Message message : this.iterate()) {
            if (message.text().contains(term)) {
                result.add(message);
            }
        }
        return result;
    }

    /**
     * It was updated just now.
     */
    @Async
    private void updated() {
        final String alias = this.self;
        Iterables.all(
            this.region.table(DyFriends.TBL).frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.bout)),
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Predicate<Item>() {
                @Override
                public boolean apply(final Item input) {
                    AttributeUpdates updates = new AttributeUpdates().with(
                        DyFriends.ATTR_UPDATED,
                        System.currentTimeMillis()
                    );
                    try {
                        if (!input.get(DyFriends.RANGE).getS().equals(alias)) {
                            updates = updates.with(
                                DyFriends.ATTR_UNREAD,
                                new AttributeValueUpdate()
                                    .withAction(AttributeAction.ADD)
                                    .withValue(new AttributeValue().withN("1"))
                            );
                        }
                        input.put(updates);
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    return true;
                }
            }
        );
    }

    /**
     * It was seen just now.
     */
    @Async
    private void seen() {
        Iterables.all(
            this.region.table(DyFriends.TBL).frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.bout))
                .where(DyFriends.RANGE, this.self),
            new Predicate<Item>() {
                @Override
                public boolean apply(final Item input) {
                    try {
                        input.put(
                            new AttributeUpdates().with(
                                DyFriends.ATTR_UNREAD, 0L
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

    /**
     * Sttc counter.
     * @return Counter
     */
    @Cacheable(forever = true)
    private static Counter sttc() {
        try {
            return new CdSttc(
                new ReSttc(
                    RtSttc.make(
                        URN.create(Manifests.read("Netbout-SttcUrn")),
                        Manifests.read("Netbout-SttcToken")
                    )
                )
            ).counters().get("nb-message");
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
