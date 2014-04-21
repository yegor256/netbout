/**
 * Copyright (c) 2009-2014, Netbout.com
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
package com.netbout.rest;

import com.jcabi.urn.URN;
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Participant;
import com.netbout.spi.text.SecureString;
import java.util.HashSet;
import java.util.Set;

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
     * List of all stages.
     */
    private transient Set<Friend> stages;

    /**
     * Name of stage.
     */
    private transient URN istage = new URN();

    /**
     * Place of stage.
     */
    private transient String iplace = "";

    /**
     * Get stage.
     * @return The name of it
     */
    public URN stage() {
        if (this.stages == null) {
            throw new IllegalStateException(
                "Call #normalize() before #stage()"
            );
        }
        return this.istage;
    }

    /**
     * Create and return a new object, which is a copy of this one.
     * @return New coordinates object
     */
    public StageCoordinates copy() {
        final StageCoordinates coords = new StageCoordinates();
        coords.stages = new HashSet<Friend>(this.stages);
        coords.istage = this.istage;
        coords.iplace = this.iplace;
        return coords;
    }

    /**
     * Set stage.
     * @param name The name of it
     * @return This object
     */
    public StageCoordinates setStage(final URN name) {
        this.istage = name;
        this.iplace = "";
        return this;
    }

    /**
     * Get place.
     * @return The name of it
     */
    public String place() {
        if (this.stages == null) {
            throw new IllegalStateException(
                "Call #normalize() before #place()"
            );
        }
        return this.iplace;
    }

    /**
     * Set stage place.
     * @param place The place name
     * @return This object
     */
    public StageCoordinates setPlace(final String place) {
        this.iplace = place;
        return this;
    }

    /**
     * Create stage coordinates from string.
     * @param pair The information from cookie
     * @return The object just built
     */
    public static StageCoordinates valueOf(final String pair) {
        final StageCoordinates coords = new StageCoordinates();
        if (pair != null && pair.contains(StageCoordinates.SEPARATOR)) {
            try {
                coords.setStage(
                    new URN(
                        SecureString.valueOf(
                            pair.substring(
                                0,
                                pair.indexOf(StageCoordinates.SEPARATOR)
                            )
                        ).text()
                    )
                );
                coords.setPlace(
                    SecureString.valueOf(
                        pair.substring(
                            pair.indexOf(StageCoordinates.SEPARATOR)
                            + StageCoordinates.SEPARATOR.length()
                        )
                    ).text()
                );
            } catch (java.net.URISyntaxException ex) {
                coords.setStage(new URN());
                coords.setPlace("");
            } catch (com.netbout.spi.text.StringDecryptionException ex) {
                coords.setStage(new URN());
                coords.setPlace("");
            }
        }
        return coords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.stages == null) {
            throw new IllegalStateException("Call #normalize() before");
        }
        return String.format(
            "%s%s%s",
            new SecureString(this.stage()),
            StageCoordinates.SEPARATOR,
            new SecureString(this.place())
        );
    }

    /**
     * List of all stages, their names.
     * @return The list
     */
    public Set<Friend> all() {
        if (this.stages == null) {
            throw new IllegalStateException("Call #normalize() before #all()");
        }
        return this.stages;
    }

    /**
     * Normalize it according to the bout.
     * @param hub The hub
     * @param bout The bout
     */
    public void normalize(final Hub hub, final Bout bout) {
        if (this.stages != null) {
            throw new IllegalStateException("Duplicate call to #normalize()");
        }
        this.stages = new HashSet<Friend>();
        for (Participant dude : bout.participants()) {
            final Boolean exists = hub.make("does-stage-exist")
                .synchronously()
                .arg(bout.number())
                .arg(dude.name())
                .inBout(bout)
                .asDefault(false)
                .exec();
            if (exists) {
                this.stages.add(dude);
            }
        }
        if (this.istage.isEmpty() && this.stages.size() > 0) {
            this.istage = this.stages.iterator().next().name();
        }
        this.discharge();
    }

    /**
     * Check current stage value and set it to VOID if such a stage
     * is absent in the list of available stages.
     * @see #normalize(Hub,Bout)
     */
    private void discharge() {
        boolean found = false;
        for (Friend identity : this.stages) {
            if (identity.name().equals(this.istage)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.istage = new URN();
        }
    }

}
