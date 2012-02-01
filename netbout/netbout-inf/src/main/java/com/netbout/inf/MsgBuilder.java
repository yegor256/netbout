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
package com.netbout.inf;

import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Participant;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds {@link Msg} from {@link Messsage}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class MsgBuilder {

    /**
     * The message to get data from.
     */
    private final transient Message message;

    /**
     * Public ctor.
     * @param msg The message
     */
    public MsgBuilder(final Message msg) {
        this.message = msg;
    }

    /**
     * Build Msg and return.
     * @return The msg
     */
    public Msg build() {
        final Map<String, Object> props = new HashMap<String, Object>();
        props.put("text", this.message.text());
        props.put("date", this.message.date());
        props.put("author.name", this.message.author().name());
        props.put("author.alias", NetboutUtils.aliasOf(this.message.author()));
        props.put("bout.date", this.message.bout().date());
        props.put("bout.recent", NetboutUtils.dateOf(this.message.bout()));
        props.put("bout.title", this.message.bout().title());
        for (Participant dude : this.message.bout().participants()) {
            props.put(
                String.format("talks-with:%s", dude.identity().name()),
                true
            );
        }
        return new DefaultMsg(
            this.message.number(),
            this.message.bout().number(),
            props
        );
    }

}
