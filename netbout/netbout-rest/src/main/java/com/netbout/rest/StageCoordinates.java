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
package com.netbout.rest;

import com.netbout.utils.TextUtils;

/**
 * Coordinates of a stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StageCoordinates {

    /**
     * Separator between stage and place.
     */
    private static final String SEPARATOR = "::";

    /**
     * Encoding to be used.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Name of stage.
     */
    private transient String stage;

    /**
     * Place of stage.
     */
    private transient String place = "";

    /**
     * Does it have stage?
     * @return Yes or no?
     */
    public boolean hasStage() {
        return this.stage != null;
    }

    /**
     * Get stage.
     * @return The name of it
     */
    public String getStage() {
        assert this.hasStage();
        return this.stage;
    }

    /**
     * Set stage.
     * @param name The name of it
     */
    public void setStage(final String name) {
        this.stage = name;
    }

    /**
     * Get place.
     * @return The name of it
     */
    public String getPlace() {
        assert this.hasStage();
        return this.place;
    }

    /**
     * Set stage place.
     * @param plce The place name
     */
    public void setPlace(final String plce) {
        assert this.hasStage();
        this.place = plce;
    }

    /**
     * Create stage coordicates from string.
     * @param pair The information from cookie
     * @return The object just built
     */
    public static StageCoordinates valueOf(final String pair) {
        final StageCoordinates coords = new StageCoordinates();
        if (pair != null && pair.contains(StageCoordinates.SEPARATOR)) {
            coords.setStage(
                TextUtils.fromBase(
                    pair.substring(0, pair.indexOf(StageCoordinates.SEPARATOR))
                )
            );
            coords.setPlace(
                TextUtils.fromBase(
                    pair.substring(
                        pair.indexOf(StageCoordinates.SEPARATOR)
                        + StageCoordinates.SEPARATOR.length()
                    )
                )
            );
        }
        return coords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String text = "";
        if (this.hasStage()) {
            text = String.format(
                "%s%s%s",
                TextUtils.toBase(this.stage),
                this.SEPARATOR,
                TextUtils.toBase(this.place)
            );
        }
        return text;
    }

}
