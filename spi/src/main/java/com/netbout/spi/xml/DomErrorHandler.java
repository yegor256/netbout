/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.spi.xml;

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
    private final transient List<Exception> errors =
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
