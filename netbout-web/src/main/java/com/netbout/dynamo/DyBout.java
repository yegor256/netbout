/**
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.Messages;
import java.util.Date;

/**
 * Dynamo bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
final class DyBout implements Bout {

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
     * @param region Region we're in
     * @param itm Item in "friends" table
     */
    DyBout(final Region region, final Item itm, final String slf) {
        this.item = itm;
        this.self = slf;
    }

    @Override
    public long number() {
        return Long.parseLong(this.item.get(DyFriends.HASH).getN());
    }

    @Override
    public Date date() {
        return new Date(
            Long.parseLong(
                this.item.get(DyFriends.ATTR_UPDATED).getN()
            )
        );
    }

    @Override
    public String title() {
        return this.item.get(DyFriends.ATTR_TITLE).getS();
    }

    @Override
    public void rename(final String text) {
        this.item.put(DyFriends.ATTR_TITLE, new AttributeValue(text));
    }

    @Override
    public Messages messages() {
        throw new UnsupportedOperationException("#messages()");
    }

    @Override
    public Friends friends() {
        return new DyFriends(
            this.item.frame().table().region(),
            this.item
        );
    }

    @Override
    public Attachments attachments() {
        throw new UnsupportedOperationException("#attachments()");
    }
}
