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

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link Msg}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultMsg implements Msg {

    /**
     * Number of message.
     */
    private final transient Long num;

    /**
     * Number of bout.
     */
    private final transient Long bnum;

    /**
     * Props.
     */
    private final transient Map<String, Object> properties;

    /**
     * Public ctor.
     * @param msg Number of message
     * @param bout Number of bout
     * @param props List of properties
     */
    public DefaultMsg(final Long msg, final Long bout,
        final Map<String, Object> props) {
        this.num = msg;
        this.bnum = bout;
        this.properties = props;
    }

    /**
     * Create a copy of this message with additional properties.
     * @param extra Extra properties to add
     * @return New Msg
     */
    public Msg copy(final Map<String, Object> extra) {
        final Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(this.properties);
        props.putAll(extra);
        return new DefaultMsg(this.num, this.bnum, props);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long bout() {
        return this.bnum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final String name) {
        return (T) this.properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(final String name) {
        return this.properties.containsKey(name);
    }

}
