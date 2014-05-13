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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "self")
@EqualsAndHashCode(of = { "region", "item", "self" })
final class DyBout implements Bout {

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Item with bout.
     */
    private final transient Item item;

    /**
     * Alias of myself.
     */
    private final transient String self;

    /**
     * Ctor.
     * @param reg Region we're in
     * @param itm Item in "friends" table
     * @param slf Self alias
     */
    DyBout(final Region reg, final Item itm, final String slf) {
        this.region = reg;
        this.item = itm;
        this.self = slf;
    }

    @Override
    public long number() throws IOException {
        return Long.parseLong(this.item.get(DyFriends.HASH).getN());
    }

    @Override
    public Date date() throws IOException {
        return new Date(
            Long.parseLong(
                this.item.get(DyFriends.ATTR_UPDATED).getN()
            )
        );
    }

    @Override
    public String title() throws IOException {
        return this.item.get(DyFriends.ATTR_TITLE).getS();
    }

    @Override
    public void rename(final String text) throws IOException {
        this.item.put(
            new AttributeUpdates().with(DyFriends.ATTR_TITLE, text)
        );
    }

    @Override
    public Messages messages() throws IOException {
        return new DyMessages(this.region, this.number(), this.self);
    }

    @Override
    public Friends friends() {
        return new DyFriends(this.region, this.item);
    }

    @Override
    public Attachments attachments() throws IOException {
        return new DyAttachments(this.region, this.number(), this.self);
    }
}
