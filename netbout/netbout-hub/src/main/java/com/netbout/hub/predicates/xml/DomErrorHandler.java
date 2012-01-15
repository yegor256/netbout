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
package com.netbout.hub.predicates.xml;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Handler of validation errors.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DomErrorHandler implements ErrorHandler {

    /**
     * List of exceptions registered.
     */
    private final List<Exception> errors =
        new CopyOnWriteArrayList<Exception>();

    /**
     * Is it empty?
     * @return Is it?
     */
    public boolean isEmpty() {
        return this.errors.isEmpty();
    }

    /**
     * All found exceptions.
     * @return List of them
     */
    public List<Exception> exceptions() {
        return this.errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final SAXParseException err) {
        this.errors.add(err);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fatalError(final SAXParseException err) {
        this.errors.add(err);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(final SAXParseException err) {
        this.errors.add(err);
    }

}
