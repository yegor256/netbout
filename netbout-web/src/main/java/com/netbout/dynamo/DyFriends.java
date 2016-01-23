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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Dynamo friends.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "item")
@EqualsAndHashCode(of = { "region", "item" })
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
     * Subscription of the bout.
     */
    public static final String ATTR_SUBSCRIPTION = "subscription";

    /**
     * Updated attribute.
     */
    public static final String ATTR_UPDATED = "updated";

    /**
     * How many messages unread here.
     */
    public static final String ATTR_UNREAD = "unread";

    /**
     * Names of attachments that are not seen yet.
     */
    public static final String ATTR_UNSEEN = "unseen";

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * Item in "friends" table.
     */
    private final transient Item item;

    /**
     * Ctor.
     * @param reg Region
     * @param itm Item in "friends" table
     */
    DyFriends(final Region reg, final Item itm) {
        this.region = reg;
        this.item = itm;
    }

    @Override
    public void invite(final String friend) throws IOException {
        final String alias = DyFriends.clean(friend);
        if (!new Everybody(this.region).occupied(alias)) {
            throw new Friends.UnknownAliasException(
                String.format("alias '%s' doesn't exist", friend)
            );
        }
        this.region.table(DyFriends.TBL).put(
            new Attributes()
                .with(DyFriends.HASH, this.bout())
                .with(DyFriends.RANGE, alias)
                .with(DyFriends.ATTR_TITLE, this.item.get(DyFriends.ATTR_TITLE))
                .with(DyFriends.ATTR_SUBSCRIPTION, true)
                .with(DyFriends.ATTR_UPDATED, System.currentTimeMillis())
        );
        Logger.info(this, "@%s invited to #%s", friend, this.bout().getN());
    }

    @Override
    public void kick(final String friend) throws IOException {
        final String alias = DyFriends.clean(friend);
        final Iterator<Item> items = this.region.table(DyFriends.TBL).frame()
            .through(new QueryValve())
            .where(DyFriends.HASH, Conditions.equalTo(this.bout()))
            .where(DyFriends.RANGE, alias)
            .iterator();
        if (!items.hasNext()) {
            throw new Friends.UnknownAliasException(
                String.format("alias '%s' is not in the bout", friend)
            );
        }
        items.next();
        items.remove();
        Logger.info(this, "@%s kicked off #%s", friend, this.bout().getN());
    }

    @Override
    public Iterable<Friend> iterate() throws IOException {
        return Iterables.transform(
            this.region.table(DyFriends.TBL)
                .frame()
                .through(new QueryValve())
                .where(DyFriends.HASH, Conditions.equalTo(this.bout())),
            new Function<Item, Friend>() {
                @Override
                public Friend apply(final Item input) {
                    try {
                        return new DyFriend(
                            DyFriends.this.region,
                            input.get(DyFriends.RANGE).getS()
                        );
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        );
    }

    /**
     * The bout we're in.
     * @return Bout number
     * @throws IOException If fails
     */
    private AttributeValue bout() throws IOException {
        return this.item.get(DyFriends.HASH);
    }

    /**
     * Clean alias.
     * @param friend Friend name
     * @return Alias
     */
    private static String clean(final String friend) {
        final String alias = StringUtils.strip(friend, " @\n\t\r");
        if (alias.isEmpty()) {
            throw new IllegalArgumentException("alias can't be empty");
        }
        if (alias.length() > Tv.HUNDRED) {
            throw new IllegalArgumentException("alias is too long");
        }
        return alias;
    }

}
