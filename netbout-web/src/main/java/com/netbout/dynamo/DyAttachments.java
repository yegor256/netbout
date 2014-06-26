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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import java.io.IOException;
import java.util.Iterator;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo attachments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "bout")
@EqualsAndHashCode(of = { "region", "bout", "self" })
final class DyAttachments implements Attachments {

    /**
     * Table name.
     */
    public static final String TBL = "attachments";

    /**
     * Bout attribute.
     */
    public static final String HASH = "bout";

    /**
     * Attachment name attribute.
     */
    public static final String RANGE = "name";

    /**
     * Owner of the attachment.
     */
    public static final String ATTR_ALIAS = "alias";

    /**
     * Ctype of the attachment.
     */
    public static final String ATTR_CTYPE = "ctype";

    /**
     * Etag of the attachment.
     */
    public static final String ATTR_ETAG = "etag";

    /**
     * Data.
     */
    public static final String ATTR_DATA = "data";

    /**
     * S3 key.
     */
    public static final String ATTR_KEY = "s3";

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * Bout number.
     */
    private final transient long bout;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * Ctor.
     * @param reg Region
     * @param num Bout number
     * @param slf Self
     */
    DyAttachments(final Region reg, final long num, final String slf) {
        this.region = reg;
        this.bout = num;
        this.self = slf;
    }

    @Override
    public Attachment get(final String name)
        throws Attachments.NotFoundException {
        final Iterator<Item> items = this.region.table(DyAttachments.TBL)
            .frame()
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withAttributesToGet(
                        DyAttachments.ATTR_CTYPE,
                        DyAttachments.ATTR_ETAG,
                        DyAttachments.ATTR_KEY
                    )
            )
            .where(DyAttachments.HASH, Conditions.equalTo(this.bout))
            .where(DyAttachments.RANGE, name)
            .iterator();
        if (!items.hasNext()) {
            throw new Attachments.NotFoundException(
                String.format("attachment '%s' not found", name)
            );
        }
        return new DyAttachment(this.region, items.next(), this.self);
    }

    @Override
    public Iterable<Attachment> iterate() {
        return Iterables.transform(
            this.region.table(DyAttachments.TBL)
                .frame()
                .through(
                    new QueryValve().withAttributesToGet(
                        DyAttachments.ATTR_ALIAS,
                        DyAttachments.ATTR_CTYPE,
                        DyAttachments.ATTR_ETAG
                    )
                )
                .where(DyAttachments.HASH, Conditions.equalTo(this.bout)),
            new Function<Item, Attachment>() {
                @Override
                public Attachment apply(final Item item) {
                    return new DyAttachment(
                        DyAttachments.this.region,
                        item, DyAttachments.this.self
                    );
                }
            }
        );
    }

    @Override
    public int unseen() throws IOException {
        final Item itm = this.region.table(DyFriends.TBL)
            .frame()
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withAttributesToGet(DyFriends.ATTR_UNSEEN)
            )
            .where(DyFriends.HASH, Conditions.equalTo(this.bout))
            .where(DyFriends.RANGE, Conditions.equalTo(this.self))
            .iterator().next();
        final int unseen;
        if (itm.has(DyFriends.ATTR_UNSEEN)) {
            unseen = itm.get(DyFriends.ATTR_UNSEEN).getSS().size();
        } else {
            unseen = 0;
        }
        return unseen;
    }

    @Override
    public void create(final String name) throws IOException {
        if (!name.matches("[a-zA-Z\\.\\-0-9]{3,100}")) {
            throw new Attachments.InvalidNameException(
                String.format("invalid attachment name '%s'", name)
            );
        }
        this.region.table(DyAttachments.TBL).put(
            new Attributes()
                .with(DyAttachments.HASH, this.bout)
                .with(DyAttachments.RANGE, name)
                .with(DyAttachments.ATTR_ALIAS, this.self)
                .with(DyAttachments.ATTR_CTYPE, MediaType.TEXT_PLAIN)
                .with(DyAttachments.ATTR_DATA, " ")
                .with(DyAttachments.ATTR_ETAG, "empty")
        );
    }

    @Override
    public void delete(final String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name can't be empty");
        }
        if (name.length() > Tv.HUNDRED) {
            throw new IllegalArgumentException("name is too long");
        }
        Iterables.removeIf(
            this.region.table(DyAttachments.TBL)
                .frame()
                .where(DyAttachments.HASH, Conditions.equalTo(this.bout))
                .where(DyAttachments.RANGE, name),
            Predicates.alwaysTrue()
        );
    }

}
