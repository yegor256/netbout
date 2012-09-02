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
package com.netbout.rest.meta;

import java.util.Map;

/**
 * Abstract par.
 *
 * <p>This class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
abstract class AbstractPar implements Par {

    /**
     * Indentation prefix.
     */
    private static final String PREFIX = "    ";

    /**
     * Regular expressions.
     */
    private final transient Map<String, String> regexs;

    /**
     * The text collected so far.
     */
    private final transient StringBuilder text = new StringBuilder();

    /**
     * Line number we're processing now.
     */
    private transient int pos;

    /**
     * PRE-mode is now ON?
     */
    private transient boolean pre;

    /**
     * Bulles mode is ON.
     */
    private transient boolean bullets;

    /**
     * We're done with the paragraph?
     */
    private transient boolean closed;

    /**
     * Public ctor.
     * @param map Map of regexs
     */
    public AbstractPar(final Map<String, String> map) {
        this.regexs = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(final String line) {
        final String trimmed = line.replaceAll("\\p{Cntrl}+", "");
        if ((trimmed.isEmpty() && this.pos != 0)
            || (this.pre && !trimmed.startsWith(AbstractPar.PREFIX))) {
            this.closed = true;
        } else {
            if (trimmed.startsWith(AbstractPar.PREFIX) && this.pos == 0) {
                this.pre = true;
            } else if (trimmed.startsWith("* ") && this.pos == 0) {
                this.bullets = true;
            }
            this.append(trimmed);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ready() {
        return this.closed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.text.length() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String out() {
        final String out = this.pack();
        this.closed = false;
        this.pre = false;
        this.text.setLength(0);
        this.pos = 0;
        this.bullets = false;
        return out;
    }

    /**
     * Pack text into paragraph.
     * @return The text
     */
    protected abstract String pack();

    /**
     * Get text.
     * @return The text
     */
    protected final String getText() {
        return this.text.toString();
    }

    /**
     * Is in PRE mode?
     * @return True if yes
     */
    protected final boolean isPre() {
        return this.pre;
    }

    /**
     * Is in BULLETS mode?
     * @return True if yes
     */
    protected final boolean isBullets() {
        return this.bullets;
    }

    /**
     * Format the line.
     * @param line The line to format
     * @return Formatted line
     */
    private String format(final String line) {
        String formatted = line;
        for (Map.Entry<String, String> regex : this.regexs.entrySet()) {
            formatted = formatted.replaceAll(
                regex.getKey(),
                regex.getValue()
            );
        }
        return formatted;
    }

    /**
     * Append this line.
     * @param line The line to append
     */
    private void append(final String line) {
        if (this.pos > 0) {
            this.text.append('\n');
        }
        if (this.pre) {
            this.text.append(line.substring(AbstractPar.PREFIX.length()));
            ++this.pos;
        } else if (this.bullets) {
            this.text.append(this.format(line.substring(2)));
            ++this.pos;
        } else {
            final String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                this.text.append(this.format(line.trim()));
                ++this.pos;
            }
        }
    }

}
