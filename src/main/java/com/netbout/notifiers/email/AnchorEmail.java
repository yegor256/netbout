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
package com.netbout.notifiers.email;

import com.jcabi.urn.URN;
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.text.SecureString;
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;

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
    private final transient Friend receiver;

    /**
     * Where it's happening.
     */
    private final transient Bout where;

    /**
     * Public ctor.
     * @param recipient Who is receiving
     * @param bout Where it's happening
     */
    public AnchorEmail(final Friend recipient, final Bout bout) {
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
        this(StringUtils.substringBefore(recipient.getAddress(), "@"), hub);
    }

    /**
     * Public ctor.
     * @param hash The hash we received
     * @param hub Where this happened
     * @throws BrokenAnchorException If can't parse it
     */
    public AnchorEmail(final String hash, final Hub hub)
        throws BrokenAnchorException {
        try {
            final String[] parts = SecureString.valueOf(hash).text()
                .split(Pattern.quote(AnchorEmail.SEPARATOR), 2);
            if (parts.length != 2) {
                throw new BrokenAnchorException("Invalid text inside hash");
            }
            final Identity identity = hub.identity(new URN(parts[1]));
            this.receiver = identity;
            this.where = identity.bout(Long.valueOf(parts[0]));
        } catch (com.netbout.spi.text.StringDecryptionException ex) {
            throw new BrokenAnchorException(ex);
        } catch (java.net.URISyntaxException ex) {
            throw new BrokenAnchorException(ex);
        } catch (Identity.UnreachableURNException ex) {
            throw new BrokenAnchorException(ex);
        } catch (Identity.BoutNotFoundException ex) {
            throw new BrokenAnchorException(ex);
        }
    }

    /**
     * Get email.
     * @return The address
     */
    public String email() {
        return String.format("%s@%s", this.hash(), AnchorEmail.DOMAIN);
    }

    /**
     * Get hash.
     * @return The hash
     */
    public String hash() {
        return new SecureString(
            String.format(
                "%d%s%s",
                this.where.number(),
                AnchorEmail.SEPARATOR,
                this.receiver.name()
            )
        ).toString();
    }

    /**
     * Who is the person.
     * @return The identity
     */
    public Friend identity() {
        return this.receiver;
    }

    /**
     * Where it's happening.
     * @return The bout number
     */
    public Bout bout() {
        return this.where;
    }

}
