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

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Async;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.netbout.spi.Attachment;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Dynamo attachment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "item")
@EqualsAndHashCode(of = { "region", "item", "self" })
@SuppressWarnings("PMD.TooManyMethods")
final class DyAttachment implements Attachment {

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Item with data.
     */
    private final transient Item item;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Ctor.
     * @param reg Region
     * @param itm Item
     * @param slf Self alias
     */
    DyAttachment(final Region reg, final Item itm, final String slf) {
        this.region = reg;
        this.item = itm;
        this.self = slf;
    }

    @Override
    public String name() throws IOException {
        return this.item.get(DyAttachments.RANGE).getS();
    }

    @Override
    public String ctype() throws IOException {
        return this.item.get(DyAttachments.ATTR_CTYPE).getS();
    }

    @Override
    public boolean unseen() throws IOException {
        final Item itm = this.region.table(DyFriends.TBL)
            .frame().through(new QueryValve())
            .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
            .where(DyFriends.RANGE, Conditions.equalTo(this.self))
            .iterator().next();
        return itm.has(DyFriends.ATTR_UNSEEN)
            && itm.get(DyFriends.ATTR_UNSEEN).getSS().contains(this.name());
    }

    @Override
    public InputStream read() throws IOException {
        this.seen();
        return IOUtils.toInputStream(
            this.item.get(DyAttachments.ATTR_DATA).getS(),
            CharEncoding.UTF_8
        );
    }

    @Override
    public void write(final InputStream stream,
        final String ctype) throws IOException {
        this.item.put(
            new AttributeUpdates()
                .with(DyAttachments.ATTR_CTYPE, ctype)
                .with(
                    DyAttachments.ATTR_DATA,
                    IOUtils.toString(stream, CharEncoding.UTF_8)
                )
        );
        this.updated();
    }

    /**
     * Bout we're in.
     * @return Bout number
     * @throws IOException If fails
     */
    private long bout() throws IOException {
        return Long.parseLong(this.item.get(DyAttachments.HASH).getN());
    }

    /**
     * It was updated just now.
     * @throws IOException If fails
     */
    @Async
    private void updated() throws IOException {
        Iterables.all(
            this.region.table(DyFriends.TBL).frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.bout())),
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Predicate<Item>() {
                @Override
                public boolean apply(final Item input) {
                    AttributeUpdates updates = new AttributeUpdates().with(
                        DyFriends.ATTR_UPDATED,
                        System.currentTimeMillis()
                    );
                    try {
                        final String alias = input.get(DyFriends.RANGE).getS();
                        if (!alias.equals(DyAttachment.this.self)) {
                            final Set<String> list =
                                DyAttachment.this.list(alias);
                            list.add(DyAttachment.this.name());
                            updates = updates.with(
                                DyFriends.ATTR_UNSEEN,
                                new AttributeValueUpdate()
                                    .withAction(AttributeAction.PUT)
                                    .withValue(
                                        new AttributeValue().withSS(list)
                                    )
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
     * @throws IOException If fails
     */
    @Async
    private void seen() throws IOException {
        Iterables.all(
            this.region.table(DyFriends.TBL).frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
                .where(DyFriends.RANGE, this.self),
            new Predicate<Item>() {
                @Override
                public boolean apply(final Item input) {
                    try {
                        final Set<String> list =
                            DyAttachment.this.list(DyAttachment.this.self);
                        list.remove(DyAttachment.this.name());
                        if (list.isEmpty()) {
                            input.put(
                                new AttributeUpdates().with(
                                    DyFriends.ATTR_UNSEEN,
                                    new AttributeValueUpdate().withAction(
                                        AttributeAction.DELETE
                                    )
                                )
                            );
                        } else {
                            input.put(
                                new AttributeUpdates().with(
                                    DyFriends.ATTR_UNSEEN,
                                    new AttributeValueUpdate()
                                        .withAction(AttributeAction.PUT)
                                        .withValue(
                                            new AttributeValue().withSS(list)
                                        )
                                )
                            );
                        }
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    return true;
                }
            }
        );
    }

    /**
     * Existing list of unseen attachments.
     * @param alias Alias
     * @return List of attachments that are unseen
     * @throws IOException If fails
     */
    private Set<String> list(final String alias) throws IOException {
        final Item itm = this.region.table(DyFriends.TBL)
            .frame().through(new QueryValve())
            .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
            .where(DyFriends.RANGE, Conditions.equalTo(alias))
            .iterator().next();
        final Set<String> list = new HashSet<String>(0);
        if (itm.has(DyFriends.ATTR_UNSEEN)) {
            list.addAll(itm.get(DyFriends.ATTR_UNSEEN).getSS());
        }
        return list;
    }

}
