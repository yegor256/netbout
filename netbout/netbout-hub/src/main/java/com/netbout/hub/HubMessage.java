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
package com.netbout.hub;

import com.netbout.hub.data.MessageData;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;

/**
 * Message in a hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class HubMessage implements Message {

    /**
     * The catalog.
     */
    private final transient Catalog catalog;

    /**
     * The bout where this message is located.
     */
    private final transient HubBout bout;

    /**
     * The data.
     */
    private final transient MessageData data;

    /**
     * Public ctor.
     * @param ctlg The catalog
     * @param holder The bout where this message is located
     * @param dat The data
     */
    private HubMessage(final Catalog ctlg, final HubBout holder,
        final MessageData dat) {
        this.catalog = ctlg;
        this.bout = holder;
        this.data = dat;
    }

    /**
     * Build new object.
     * @param ctlg The catalog
     * @param holder The bout where this message is located
     * @param dat The data
     * @return The object just built
     */
    public static HubMessage build(final Catalog ctlg, final HubBout holder,
        final MessageData dat) {
        return new HubMessage(ctlg, holder, dat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        return this.bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        try {
            return this.catalog.make(this.data.getAuthor());
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        this.data.addSeenBy(this.bout.getViewer().name());
        return this.data.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return this.data.isSeenBy(this.bout.getViewer().name());
    }

}
