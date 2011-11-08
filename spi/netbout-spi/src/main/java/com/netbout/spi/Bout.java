/**
 * Copyright (c) 2009-2011, NetBout.com
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
package com.netbout.spi;

import java.util.Collection;
import java.util.List;

/**
 * Bout, a conversation room.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Bout {

    /**
     * Who is viewing this bout now.
     * @return The identity of the viewer
     */
    Identity identity();

    /**
     * Get its unique number.
     * @return The number of the bout
     */
    Long number();

    /**
     * Get its title.
     * @return The title of the bout
     */
    String title();

    /**
     * Set its title.
     * @param text The title of the bout
     */
    void rename(String text);

    /**
     * Get all its participants.
     * @return The list of them
     */
    Collection<Participant> participants();

    /**
     * Invite new participant.
     * @param identity Identity of the participant
     * @return This new participant
     * @throws UnknownIdentityException If this identity is not found
     */
    Participant invite(String identity) throws UnknownIdentityException;

    /**
     * Get ordered list of all messages of the bout.
     * @param query Search query, if necessary
     * @return The list of them
     */
    List<Message> messages(String query);

    /**
     * Post a new message.
     * @param text The text of the new message
     * @return The message just posted
     */
    Message post(String text);

}
