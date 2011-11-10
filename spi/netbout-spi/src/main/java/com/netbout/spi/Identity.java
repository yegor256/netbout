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

import java.net.URL;
import java.util.List;

/**
 * Identity.
 *
 * <p>This is the main entry point to all bouts which belong to the user. An
 * instance of this interface can be obtained from
 * {@link User#identify(String)}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see User#identify(String)
 */
public interface Identity {

    /**
     * User.
     * @return The user
     */
    User user();

    /**
     * Start new bout.
     * @return The bout just created
     */
    Bout start();

    /**
     * Get an ordered list of all bouts this identity is taking
     * participation in.
     * @param query Search query, if necessary
     * @return The list of bouts
     */
    List<Bout> inbox(String query);

    /**
     * Get bout by its unique ID.
     * @param number The number of the bout
     * @return The bout
     * @throws BoutNotFoundException If this bout doesn't exist
     */
    Bout bout(Long number) throws BoutNotFoundException;

    /**
     * Get name of the identity.
     * @return The name
     */
    String name();

    /**
     * Get a photo of this identity.
     * @return The URL of the photo
     */
    URL photo();

    /**
     * This identity should be promoted to an active helper.
     * @param helper The helper that can help us to process data
     * @throws PromotionException If there is some problem
     */
    void promote(Helper helper) throws PromotionException;

    /**
     * Set photo of the identity.
     * @param photo The photo
     */
    void setPhoto(URL photo);

}
