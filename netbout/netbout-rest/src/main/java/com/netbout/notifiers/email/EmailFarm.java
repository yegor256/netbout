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

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.netbout.utils.Cipher;
import com.netbout.utils.TextUtils;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.core.UriBuilder;
import org.apache.velocity.VelocityContext;

/**
 * Email farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class EmailFarm implements IdentityAware {

    /**
     * Namespace ID.
     */
    public static final String NID = "email";

    /**
     * Email validating regex.
     */
    public static final String EMAIL_REGEX =
        // @checkstyle LineLength (1 line)
        "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";

    /**
     * Email sender.
     */
    private final transient Sender sender = new Sender();

    /**
     * Me.
     */
    private transient Identity identity;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Identity idnt) {
        this.identity = idnt;
    }

    /**
     * Notify bout participants.
     * @param bnum Bout where it's happening
     * @param mnum Message number to notify about
     * @throws Exception If some problem inside
     */
    @Operation("notify-bout-participants")
    public void notifyBoutParticipants(final Long bnum, final Long mnum)
        throws Exception {
        final Bout bout = this.identity.bout(bnum);
        final Message message = bout.message(mnum);
        for (Participant participant : bout.participants()) {
            if (this.NID.equals(participant.identity().name().nid())) {
                this.send(participant, message);
            }
        }
    }

    /**
     * Construct extra identities, if necessary.
     * @param keyword The keyword they are searching for
     * @return List of URNs
     */
    @Operation("find-identities-by-keyword")
    public List<Urn> findIdentitiesByKeyword(final String keyword) {
        List<Urn> urns = null;
        if (keyword.matches(this.EMAIL_REGEX)) {
            urns = new ArrayList<Urn>();
            urns.add(new Urn(this.NID, keyword));
        }
        return urns;
    }

    /**
     * Get list of aliases that belong to some identity.
     * @param name The identity of bout participant
     * @return List of aliases
     */
    @Operation("get-aliases-of-identity")
    public List<String> getAliasesOfIdentity(final Urn name) {
        List<String> aliases = null;
        if (this.NID.equals(name.nid())) {
            aliases = new ArrayList<String>();
            aliases.add(name.nss());
        }
        return aliases;
    }

    /**
     * Notify this identity.
     * @param dude The recepient
     * @param message The message
     */
    private void send(final Participant dude, final Message message) {
        assert dude != null;
        final VelocityContext context = new VelocityContext();
        context.put("bout", dude.bout());
        context.put("recepient", dude.identity());
        context.put("message", message);
        context.put(
            "href",
            UriBuilder.fromUri("http://www.netbout.com/")
                .path("/auth")
                .queryParam("identity", "{urn}")
                .queryParam("secret", "{secret}")
                .queryParam("goto", "{path}")
                .build(
                    dude.identity().name(),
                    new Cipher().encrypt(dude.identity().name().toString()),
                    String.format("/%d", dude.bout().number())
                )
                .toString()
        );
        final String text = TextUtils.format(
            "com/netbout/notifiers/email/email-notification.vm",
            context
        );
        final javax.mail.Message email = this.sender.newMessage();
        try {
            final Address reply = new InternetAddress(
                String.format(
                    "%s@netbout.com",
                    TextUtils.pack(dude.identity().name().toString())
                ),
                message.author().name().toString()
            );
            email.addFrom(new Address[] {reply});
            email.setReplyTo(new Address[] {reply});
            email.addRecipient(
                javax.mail.Message.RecipientType.TO,
                new InternetAddress(dude.identity().name().nss())
            );
            email.setText(text);
            email.setSubject(
                String.format(
                    "#%d: %s",
                    dude.bout().number(),
                    dude.bout().title()
                )
            );
            this.sender.send(email);
        } catch (javax.mail.internet.AddressException ex) {
            throw new IllegalArgumentException(ex);
        } catch (javax.mail.MessagingException ex) {
            throw new IllegalArgumentException(ex);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
