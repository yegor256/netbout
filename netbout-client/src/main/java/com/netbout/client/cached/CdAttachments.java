/**
 * Copyright (c) 2009-2014, netbout.com
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
package com.netbout.client.cached;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached attachments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.3
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "origin")
public final class CdAttachments implements Attachments {

    /**
     * Original object.
     */
    private final transient Attachments origin;

    /**
     * Public ctor.
     * @param orgn Original object
     */
    public CdAttachments(final Attachments orgn) {
        this.origin = orgn;
    }

    @Override
    @Cacheable
    public int unseen() throws IOException {
        return this.origin.unseen();
    }

    @Override
    @Cacheable.FlushBefore
    public void create(final String name) throws IOException {
        this.origin.create(name);
    }

    @Override
    @Cacheable.FlushBefore
    public void delete(final String name) throws IOException {
        this.origin.delete(name);
    }

    @Override
    @Cacheable
    public Attachment get(final String name) throws IOException {
        return new CdAttachment(this.origin.get(name));
    }

    @Override
    @Cacheable
    public Iterable<Attachment> iterate() throws IOException {
        return Iterables.transform(
            this.origin.iterate(),
            new Function<Attachment, Attachment>() {
                @Override
                public Attachment apply(final Attachment attachment) {
                    return new CdAttachment(attachment);
                }
            }
        );
    }
}
