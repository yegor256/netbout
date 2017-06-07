/**
 * Copyright (c) 2009-2017, netbout.com
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

import com.amazonaws.services.dynamodbv2.model.Select;
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
import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Aliases;
import java.io.IOException;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo Aliases.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "urn")
@EqualsAndHashCode(of = { "region", "urn" })
final class DyAliases implements Aliases {

    /**
     * Table name.
     */
    public static final String TBL = "aliases";

    /**
     * HASH attribute.
     */
    public static final String HASH = "alias";

    /**
     * URN attribute.
     */
    public static final String ATTR_URN = "urn";

    /**
     * Locale attribute.
     */
    public static final String ATTR_LOCALE = "locale";

    /**
     * Photo attribute.
     */
    public static final String ATTR_PHOTO = "photo";

    /**
     * Photo attribute.
     * @since 2.12
     */
    public static final String ATTR_EMAIL = "email";

    /**
     * Index name.
     */
    private static final String INDEX = "users";

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN urn;

    /**
     * Ctor.
     * @param reg Region we're in
     * @param user URN of the user
     */
    DyAliases(final Region reg, final URN user) {
        this.region = reg;
        this.urn = user;
    }

    @Override
    public String check(final String name) {
        final String answer;
        if (name.length() < Tv.FOUR) {
            answer = "too short, must be 4 letters at least";
        } else if (name.length() > Tv.TWENTY) {
            answer = "too long, must be 20 letters at most";
        } else if (name.matches("[a-z0-9]+")) {
            if (new Everybody(this.region).occupied(name)) {
                answer = "this alias is occupied";
            } else {
                answer = "";
            }
        } else {
            answer = "only lower case English letters and numbers are accepted";
        }
        return answer;
    }

    @Override
    public Alias add(final String name) throws IOException {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("alias can't be empty");
        }
        if (name.length() > Tv.HUNDRED) {
            throw new IllegalArgumentException("alias is too long");
        }
        if (new Everybody(this.region).occupied(name)) {
            throw new IllegalArgumentException(
                String.format("alias '%s' is occupied", name)
            );
        }
        final Item item = this.region.table(DyAliases.TBL).put(
            new Attributes()
                .with(DyAliases.ATTR_URN, this.urn)
                .with(DyAliases.HASH, name)
                .with(DyAliases.ATTR_PHOTO, Alias.BLANK)
                .with(DyAliases.ATTR_LOCALE, Locale.ENGLISH)
        );
        Logger.info(this, "alias @%s added to %s", name, this.urn);
        return new DyAlias(this.region, item);
    }

    @Override
    public Iterable<Alias> iterate() {
        return Iterables.transform(
            this.region
                .table(DyAliases.TBL)
                .frame()
                .where(DyAliases.ATTR_URN, Conditions.equalTo(this.urn))
                .through(
                    new QueryValve()
                        .withIndexName(DyAliases.INDEX)
                        .withConsistentRead(false)
                        .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                ),
            new Function<Item, Alias>() {
                @Override
                public Alias apply(final Item item) {
                    return new DyAlias(DyAliases.this.region, item);
                }
            }
        );
    }

}
