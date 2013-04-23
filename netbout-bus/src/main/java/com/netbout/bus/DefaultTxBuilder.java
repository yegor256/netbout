/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.bus;

import com.netbout.bus.attrs.AsDefaultAttr;
import com.netbout.bus.attrs.AsPreliminaryAttr;
import com.netbout.bus.attrs.CacheAttr;
import com.netbout.bus.attrs.InBoutAttr;
import com.netbout.bus.attrs.PriorityAttr;
import com.netbout.bus.attrs.ProgressAttr;
import com.netbout.spi.Bout;
import com.netbout.spi.Plain;
import com.netbout.spi.PlainBuilder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Transaction builder, default implementation.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTxBuilder implements TxBuilder {

    /**
     * List of attributes added.
     */
    private final transient TxAttributes attributes = new TxAttributes();

    /**
     * List of arguments.
     */
    private final transient List<Plain<?>> args =
        new CopyOnWriteArrayList<Plain<?>>();

    /**
     * Transaction controller.
     */
    private final transient TxController controller;

    /**
     * Transaction mnemo.
     */
    private final transient String mnemo;

    /**
     * Public ctor.
     * @param ctlr The controller
     * @param name The mnemo
     * @see Bus#make(String)
     */
    public DefaultTxBuilder(final TxController ctlr, final String name) {
        this.controller = ctlr;
        this.mnemo = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder asap() {
        this.attributes
            .get(PriorityAttr.class)
            .withPriority(PriorityAttr.Priority.ASAP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder synchronously() {
        this.attributes
            .get(PriorityAttr.class)
            .withPriority(PriorityAttr.Priority.SYNC);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder inBout(final Bout bout) {
        this.attributes
            .get(InBoutAttr.class)
            .withBout(bout);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder progress(final TxProgress progress) {
        this.attributes
            .get(ProgressAttr.class)
            .withProgress(progress);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder arg(final Object arg) {
        this.args.add(PlainBuilder.fromObject(arg));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder asDefault(final Object value) {
        this.attributes
            .get(AsDefaultAttr.class)
            .withValue(PlainBuilder.fromObject(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder asPreliminary(final Object value) {
        this.attributes
            .get(AsPreliminaryAttr.class)
            .withValue(PlainBuilder.fromObject(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder expire(final String regex) {
        this.attributes
            .get(CacheAttr.class)
            .expireByPattern(Pattern.compile(regex));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder noCache() {
        this.attributes
            .get(CacheAttr.class)
            .disableCache();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T exec() {
        final Transaction trans = new DefaultTransaction(
            this.mnemo,
            this.args,
            this.attributes
        );
        return (T) this.controller.exec(trans).value();
    }

}
