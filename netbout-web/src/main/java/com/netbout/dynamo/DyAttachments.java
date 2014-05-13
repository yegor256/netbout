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
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
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
@EqualsAndHashCode(of = { "region", "bout" })
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
     * Data.
     */
    public static final String ATTR_DATA = "data";

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
    public Attachment get(final String name) throws IOException {
        final Iterator<Item> items = this.region.table(DyAttachments.TBL)
            .frame()
            .where(DyAttachments.HASH, Conditions.equalTo(this.bout))
            .where(DyAttachments.RANGE, name)
            .iterator();
        final Item item;
        if (items.hasNext()) {
            item = items.next();
        } else {
            item = this.create(name);
        }
        return new DyAttachment(item);
    }

    @Override
    public Iterable<Attachment> iterate() {
        return Iterables.transform(
            this.region.table(DyAttachments.TBL)
                .frame()
                .through(
                    new QueryValve().withAttributesToGet(
                        DyAttachments.ATTR_ALIAS, DyAttachments.ATTR_CTYPE
                    )
                )
                .where(DyAttachments.HASH, Conditions.equalTo(this.bout)),
            new Function<Item, Attachment>() {
                @Override
                public Attachment apply(final Item item) {
                    return new DyAttachment(item);
                }
            }
        );
    }

    /**
     * Create a new one with this name.
     * @param name Name of it
     * @return Item just created
     * @throws IOException If fails
     */
    private Item create(final String name) throws IOException {
        return this.region.table(DyAttachments.TBL).put(
            new Attributes()
                .with(DyAttachments.HASH, this.bout)
                .with(DyAttachments.RANGE, name)
                .with(DyAttachments.ATTR_ALIAS, this.self)
                .with(DyAttachments.ATTR_CTYPE, MediaType.TEXT_PLAIN)
                .with(DyAttachments.ATTR_DATA, " ")
        );
    }

}
