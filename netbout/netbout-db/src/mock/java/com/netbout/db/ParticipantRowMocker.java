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
package com.netbout.db;

import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;

/**
 * Mocker of {@code PARTICIPANT} row in a database.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ParticipantRowMocker {

    /**
     * The bout it is related to.
     */
    private final transient Long bout;

    /**
     * The name of it.
     */
    private transient Urn identity;

    /**
     * Public ctor.
     * @param number The bout
     */
    public ParticipantRowMocker(final Long number) {
        this.identity = new UrnMocker().mock();
        this.bout = number;
    }

    /**
     * With this name.
     * @param name The name of participant
     * @return This object
     */
    public ParticipantRowMocker namedAs(final String name) {
        return this.namedAs(Urn.create(name));
    }

    /**
     * With this name.
     * @param name The name of participant
     * @return This object
     */
    public ParticipantRowMocker namedAs(final Urn name) {
        this.identity = name;
        return this;
    }

    /**
     * Mock it and return its name.
     * @return The URN of just created participant
     */
    public Urn mock() {
        final IdentityFarm ifarm = new IdentityFarm();
        ifarm.identityMentioned(this.identity);
        final ParticipantFarm farm = new ParticipantFarm();
        farm.addedBoutParticipant(this.bout, this.identity);
        return this.identity;
    }

}
