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
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo friends.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "item")
@EqualsAndHashCode(of = { "table", "item" })
final class DyFriends implements Friends {

    /**
     * Table name.
     */
    public static final String TBL = "friends";

    /**
     * Index name.
     */
    public static final String INDEX = "inbox";

    /**
     * Bout attribute.
     */
    public static final String HASH = "bout";

    /**
     * Alias attribute.
     */
    public static final String RANGE = "alias";

    /**
     * Title of the bout.
     */
    public static final String ATTR_TITLE = "title";

    /**
     * Updated attribute.
     */
    public static final String ATTR_UPDATED = "updated";

    /**
     * Table to work with.
     */
    private final transient Table table;

    /**
     * Item in "friends" table.
     */
    private final transient Item item;

    /**
     * Ctor.
     * @param region Region
     * @param itm Item in "friends" table
     */
    DyFriends(final Region region, final Item itm) {
        this.table = region.table(DyFriends.TBL);
        this.item = itm;
    }

    @Override
    public void invite(final String friend) {
        this.table.put(
            new Attributes()
                .with(DyFriends.HASH, this.bout())
                .with(DyFriends.RANGE, this.self())
                .with(DyFriends.ATTR_TITLE, this.title())
                .with(DyFriends.ATTR_UPDATED, System.currentTimeMillis())
        );
    }

    @Override
    public void kick(final String friend) {
        Iterators.removeIf(
            this.table.frame()
                .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
                .where(DyFriends.RANGE, friend)
                .iterator(),
            Predicates.alwaysTrue()
        );
    }

    @Override
    public Iterator<Friend> iterator() {
        return Iterators.transform(
            this.table.frame()
                .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
                .iterator(),
            new Function<Item, Friend>() {
                @Override
                public Friend apply(final Item input) {
                    return new DyFriend(
                        DyFriends.this.table.region(),
                        input.get(DyFriends.RANGE).getS()
                    );
                }
            }
        );
    }

    /**
     * The bout we're in.
     * @return Bout number
     */
    private long bout() {
        return Long.parseLong(this.item.get(DyFriends.HASH).getN());
    }

    /**
     * My alias.
     * @return Alias
     */
    private String self() {
        return this.item.get(DyFriends.RANGE).getS();
    }

    /**
     * My title.
     * @return Title
     */
    private String title() {
        return this.item.get(DyFriends.ATTR_TITLE).getS();
    }

}
