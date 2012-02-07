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
package com.netbout.notifiers.email;

import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.netbout.utils.Cipher;
import com.netbout.utils.TextUtils;
import javax.mail.internet.InternetAddress;

/**
 * The email we use to identify senders and recipients.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class AnchorEmail {

    /**
     * The domain to use.
     */
    private static final String DOMAIN = "netbout.com";

    /**
     * Separator between bout number and identity.
     */
    private static final String SEPARATOR = "|";

    /**
     * The recipient.
     */
    private final transient Identity receiver;

    /**
     * Where it's happening.
     */
    private final transient Bout where;

    /**
     * Public ctor.
     * @param recipient Who is receiving
     * @param bout Where it's happening
     */
    public AnchorEmail(final Identity recipient, final Bout bout) {
        this.receiver = recipient;
        this.where = bout;
    }

    /**
     * Public ctor.
     * @param recipient Who is receiving
     * @param hub Where this happened
     * @throws BrokenAnchorException If can't parse it
     */
    public AnchorEmail(final InternetAddress recipient, final Hub hub)
        throws BrokenAnchorException {
        final String email = recipient.getAddress();
        final String hash = email.substring(0, email.lastIndexOf('@'));
        try {
            final String[] parts = new Cipher().decrypt(TextUtils.unpack(hash))
                .split(this.SEPARATOR, 2);
            this.receiver = hub.identity(new Urn(parts[1]));
            this.where = this.receiver.bout(Long.valueOf(parts[0]));
        } catch (java.net.URISyntaxException ex) {
            throw new BrokenAnchorException(ex);
        } catch (com.netbout.utils.DecryptionException ex) {
            throw new BrokenAnchorException(ex);
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new BrokenAnchorException(ex);
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            throw new BrokenAnchorException(ex);
        }
    }

    /**
     * Get email.
     * @return The address
     */
    public String email() {
        final String hash = TextUtils.pack(
            new Cipher().encrypt(
                String.format(
                    "%d%s%s",
                    this.where.number(),
                    this.SEPARATOR,
                    this.receiver.name()
                )
            )
        );
        return String.format("%s@%s", hash, AnchorEmail.DOMAIN);
    }

    /**
     * Where it's happening.
     * @return The bout number
     */
    public Bout bout() {
        return this.where;
    }
}
