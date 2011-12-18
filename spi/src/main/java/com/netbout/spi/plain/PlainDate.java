/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.plain;

import com.netbout.spi.Plain;
import java.util.Date;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Plain date.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PlainDate implements Plain<Date> {

    /**
     * Formatter.
     */
    private static final DateTimeFormatter FORMATTER =
        ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    /**
     * Prefix.
     */
    private static final String PREFIX = "D";

    /**
     * The value.
     */
    private final transient Date date;

    /**
     * Public ctor.
     * @param text The text presentation
     */
    public PlainDate(final String text) {
        this.date = new Date(
            this.FORMATTER.parseMillis(text.substring(this.PREFIX.length()))
        );
    }

    /**
     * Public ctor.
     * @param dte The date
     */
    public PlainDate(final Date dte) {
        this.date = dte;
    }

    /**
     * Is it of our type?
     * @param text The text
     * @return Is it or not?
     */
    public static boolean isIt(final String text) {
        return text.startsWith(PlainDate.PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.date.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof PlainDate)
            && (this.hashCode() == obj.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date value() {
        return this.date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s%s",
            this.PREFIX,
            this.FORMATTER.print(this.date.getTime())
        );
    }

}
