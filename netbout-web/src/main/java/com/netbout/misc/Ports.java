/**
 * Copyright (c) 2009-2016, netbout.com
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
package com.netbout.misc;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Random TCP ports.
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Ports {

    /**
     * Already assigned ports.
     */
    private static final Collection<Integer> ASSIGNED =
        new ConcurrentSkipListSet<Integer>();

    /**
     * Hide the default no args constructor from the utility class.
     */
    private Ports() {
        super();
    }

    /**
     * Allocate a new random TCP port.
     * @return TCP port
     * @throws IOException If fails
     */
    public static int allocate() throws IOException {
        synchronized (Ports.class) {
            int attempts = 0;
            int prt;
            do {
                prt = random();
                ++attempts;
                // @checkstyle MagicNumber (1 line)
                if (attempts > 100) {
                    throw new IllegalStateException(
                        String.format(
                            "failed to allocate TCP port after %d attempts",
                            attempts
                        )
                    );
                }
            } while (Ports.ASSIGNED.contains(prt));
            return prt;
        }
    }

    /**
     * Release it.
     * @param port Port
     */
    public static void release(final int port) {
        Ports.ASSIGNED.remove(port);
    }

    /**
     * Allocate a new random TCP port.
     * @return TCP port
     * @throws IOException If fails
     */
    private static int random() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        try {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } finally {
            socket.close();
        }
    }

}
