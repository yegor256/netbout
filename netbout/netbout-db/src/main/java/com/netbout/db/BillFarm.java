/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.db;

import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.util.Date;
import java.util.List;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Manipulations with bills.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class BillFarm {

    /**
     * Save a collection of incoming bills, from BUS.
     * @param lines Text forms of them
     * @checkstyle MagicNumber (20 lines)
     */
    @Operation("save-bills")
    public void saveBills(final List<String> lines) {
        for (String line : lines) {
            final String[] parts = line.split("[ ]+");
            Long bout = null;
            if (!"null".equals(parts[4])) {
                bout = Long.valueOf(parts[4].replaceAll("[^\\d]+", ""));
            }
            this.bill(
                ISODateTimeFormat.dateTime()
                    .parseDateTime(parts[0])
                    .toDate(),
                parts[1],
                Urn.create(parts[2]),
                Long.valueOf(parts[3]),
                bout
            );
        }
    }

    /**
     * Save one bill.
     * @param date The date
     * @param mnemo The mnemo
     * @param helper The helper
     * @param msec The milliseconds
     * @param bout The bout
     * @checkstyle ParameterNumber (3 lines)
     */
    private void bill(final Date date, final String mnemo, final Urn helper,
        final Long msec, final Long bout) {
        new DbSession()
            // @checkstyle LineLength (1 line)
            .sql("INSERT INTO bill (date, mnemo, helper, msec, bout) VALUES (?, ?, ?, ?, ?)")
            .set(date)
            .set(mnemo)
            .set(helper)
            .set(msec)
            .set(bout)
            .insert(new VoidHandler());
    }

}
