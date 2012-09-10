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
package com.netbout.rest;

import com.jcabi.log.Logger;
import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.text.SecureString;

/**
 * Encrypts and decrypts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Cryptor {

    /**
     * Encrypt user+identity into text.
     * @param identity The identity
     * @return Encrypted string
     */
    public String encrypt(final Identity identity) {
        return new SecureString(identity.name()).toString();
    }

    /**
     * Get identity from hash.
     * @param hub Hub where to get identities
     * @param hash The hash to use
     * @return The name found in it
     * @throws DecryptionException If we can't decrypt it
     */
    public Identity decrypt(final Hub hub, final String hash)
        throws DecryptionException {
        if (hash == null) {
            throw new DecryptionException("HASH is empty (NULL)");
        }
        String iname;
        try {
            iname = SecureString.valueOf(hash).text();
        } catch (com.netbout.spi.text.StringDecryptionException ex) {
            throw new DecryptionException(ex);
        }
        Identity identity;
        try {
            identity = hub.identity(new Urn(iname));
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new DecryptionException(ex);
        } catch (java.net.URISyntaxException ex) {
            throw new DecryptionException(ex);
        }
        Logger.debug(
            this,
            "#decrypt(%[type]s, %s): identity '%s' found",
            hub,
            hash,
            identity.name()
        );
        return identity;
    }

}
