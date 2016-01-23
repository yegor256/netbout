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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo Alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "item")
@EqualsAndHashCode(of = { "region", "item" })
final class DyAlias implements Alias {

    /**
     * Valid email pattern.
     */
    private static final Pattern MAIL;

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Item we're working with.
     */
    private final transient Item item;

    static {
        //@checkstyle LineLengthCheck (1 line)
        final String valid = "([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}";
        MAIL = Pattern.compile(
            String.format("!?%s|%s!%s", valid, valid, valid),
            Pattern.CASE_INSENSITIVE
        );
    }

    /**
     * Ctor.
     * @param reg Region
     * @param itm Item
     */
    DyAlias(final Region reg, final Item itm) {
        this.region = reg;
        this.item = itm;
    }

    @Override
    public String name() throws IOException {
        return this.item.get(DyAliases.HASH).getS();
    }

    @Override
    public URI photo() throws IOException {
        return URI.create(
            this.item.get(DyAliases.ATTR_PHOTO).getS()
        );
    }

    @Override
    public Locale locale() throws IOException {
        return new Locale(
            this.item.get(DyAliases.ATTR_LOCALE).getS()
        );
    }

    @Override
    public void photo(final URI uri) throws IOException {
        this.item.put(
            new AttributeUpdates().with(DyAliases.ATTR_PHOTO, uri)
        );
    }

    @Override
    public String email() throws IOException {
        final String email;
        if (this.item.has(DyAliases.ATTR_EMAIL)) {
            email = this.item.get(DyAliases.ATTR_EMAIL).getS();
        } else {
            email = "";
        }
        return email;
    }

    @Override
    public void email(final String email) throws IOException {
        if (!MAIL.matcher(email).matches()) {
            throw new Alias.InvalidEmailException(email);
        }
        this.item.put(
            new AttributeUpdates().with(DyAliases.ATTR_EMAIL, email)
        );
        Logger.info(this, "@%s changed email to %s", this.name(), email);
    }

    @Override
    public void email(final String email, final String urn, final Bout bout) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public void email(final String email, final String link)
        throws IOException {
        this.email(email);
    }

    @Override
    public Inbox inbox() throws IOException {
        return new DyInbox(this.region, this.name());
    }
}
