/**
 * Copyright (c) 2009-2017, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout;

import com.jcabi.email.Postman;
import com.jcabi.email.postman.PostNoLoops;
import com.jcabi.email.wire.SMTP;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.netbout.cached.CdBase;
import com.netbout.dynamo.DyBase;
import com.netbout.email.EmBase;
import com.netbout.rest.TkApp;
import org.takes.http.Exit;
import org.takes.http.FtCLI;

/**
 * Launch (used only for heroku).
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Launch {

    /**
     * Utility class.
     */
    private Launch() {
        // intentionally empty
    }

    /**
     * Entry point.
     * @param args Command line args
     * @throws Exception If fails
     */
    public static void main(final String... args) throws Exception {
        Logger.info(Launch.class, "starting...");
        new FtCLI(
            new TkApp(
                new EmBase(
                    new CdBase(new DyBase()),
                    new PostNoLoops(Launch.postman())
                )
            ),
            args
        ).start(Exit.NEVER);
    }

    /**
     * Create a postman.
     * @return Postman
     */
    private static Postman postman() {
        final int port = Integer.parseInt(Manifests.read("Netbout-SmtpPort"));
        final Postman postman;
        if (port == 0) {
            postman = Postman.CONSOLE;
        } else {
            postman = new Postman.Default(
                new SMTP(
                    Manifests.read("Netbout-SmtpHost"),
                    port,
                    Manifests.read("Netbout-SmtpUser"),
                    Manifests.read("Netbout-SmtpPassword")
                )
            );
        }
        return postman;
    }

}
