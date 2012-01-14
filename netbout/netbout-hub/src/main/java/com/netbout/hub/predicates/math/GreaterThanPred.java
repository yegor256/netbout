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
package com.netbout.hub.predicates.math;

import com.netbout.hub.Predicate;
import com.netbout.hub.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.Date;
import java.util.List;
import org.joda.time.format.ISODateTimeFormat;

/**
 * First argument is greater than the second.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class GreaterThanPred extends AbstractVarargPred {

    /**
     * Public ctor.
     * @param args The arguments
     */
    public GreaterThanPred(final List<Predicate> args) {
        super("greater-than", args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Message msg, final int pos) {
        final Object left = this.arg(0).evaluate(msg, pos);
        final String right = this.arg(1).evaluate(msg, pos).toString();
        boolean greater;
        if (left instanceof Date) {
            greater = ((Date) left).after(
                ISODateTimeFormat.dateTime().parseDateTime(right).toDate()
            );
        } else if (left instanceof Long) {
            greater = ((Long) left) > Long.valueOf(right);
        } else {
            greater = left.toString().compareTo(right) > 0;
        }
        Logger.debug(
            this,
            "#evaluate(): is %[type]s > '%s': %B",
            left,
            right,
            greater
        );
        return greater;
    }

}
