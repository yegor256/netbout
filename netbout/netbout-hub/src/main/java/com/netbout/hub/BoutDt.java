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

import com.netbout.spi.Bout;
import com.netbout.spi.Urn;
import java.util.Collection;
import java.util.Date;

/**
 * Bout data type.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface BoutDt {

    /**
     * Get its number.
     * @return The number
     */
    Long getNumber();

    /**
     * Get date of creation.
     * @return The date
     */
    Date getDate();

    /**
     * Get title.
     * @return The title
     */
    String getTitle();

    /**
     * Set title.
     * @param text The title
     */
    void setTitle(String text);

    /**
     * Confirm participation.
     * @param identity Who confirms?
     */
    void confirm(Urn identity);

    /**
     * Kick off this identity of the bout.
     * @param identity Who leaves
     */
    void kickOff(Urn identity);

    /**
     * Set leader.
     * @param identity Who should be a leader now
     */
    void setLeader(Urn identity);

    /**
     * Add new participant.
     * @param name The name of participant
     * @return The participant just created/added
     */
    ParticipantDt addParticipant(Urn name);

    /**
     * Get list of participants.
     * @return The list
     */
    Collection<ParticipantDt> getParticipants();

    /**
     * Post new message.
     * @return The data
     */
    MessageDt addMessage();

    /**
     * Find message by number.
     * @param num The number of it
     * @return Message
     * @throws Bout.MessageNotFoundException If not found
     * @checkstyle RedundantThrows (4 lines)
     */
    MessageDt findMessage(Long num) throws Bout.MessageNotFoundException;

    /**
     * Get number of the latest message in the bout.
     * @return The number or ZERO if there are no messages
     */
    long getLatestMessage();

}
