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
package com.netbout.hub;

import com.jcabi.urn.URN;
import com.netbout.hub.inf.InfParticipant;
import com.netbout.spi.Participant;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Participant data type.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface ParticipantDt {

    /**
     * Get identity.
     * @return The identity
     */
    URN getIdentity();

    /**
     * Set status.
     * @param flag The flag
     */
    void setConfirmed(Boolean flag);

    /**
     * Is it confirmed?
     * @return The flag
     */
    Boolean isConfirmed();

    /**
     * Is he a leader?
     * @return The flag
     */
    Boolean isLeader();

    /**
     * Set status.
     * @param flag The flag
     */
    void setLeader(Boolean flag);

    /**
     * Collection of participants.
     */
    class Participants extends AbstractCollection<Participant> {
        /**
         * List of participants.
         */
        private final transient Collection<ParticipantDt> dudes;
        /**
         * Public ctor.
         * @param list List of them
         */
        public Participants(final Collection<ParticipantDt> list) {
            super();
            this.dudes = list;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Participant> iterator() {
            final Iterator<ParticipantDt> all = this.dudes.iterator();
            return new Iterator<Participant>() {
                @Override
                public Participant next() {
                    return new InfParticipant(all.next());
                }
                @Override
                public boolean hasNext() {
                    return all.hasNext();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.dudes.size();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final Object object) {
            boolean contains = false;
            for (ParticipantDt dude : this.dudes) {
                if (dude.getIdentity().equals(object.toString())) {
                    contains = true;
                    break;
                }
            }
            return contains;
        }
    }

}
