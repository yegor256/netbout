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
package com.netbout.inf.index;

import com.rexsl.core.Manifests;
import com.rexsl.test.RestTester;
import com.ymock.util.Logger;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Amazon EBS mounted volume.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
final class EbsVolume implements Folder {

    /**
     * The directory.
     */
    private final transient EbsDirectory directory = new EbsDirectory(
        new File(System.getProperty("java.io.tmpdir"), "netbout-INF")
    );

    /**
     * EBS instance to work with.
     */
    private final transient String instance;

    /**
     * Default public ctor.
     */
    public EbsVolume() {
        this(EbsVolume.currentInstance());
    }

    /**
     * Public ctor.
     * @param name Name of EC2 instance we're working on
     */
    public EbsVolume(final String name) {
        this.instance = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public File path() {
        try {
            if (!this.directory.mounted()) {
                this.directory.mount(
                    new EbsDevice(
                        this.instance,
                        Manifests.read("Netbout-EbsVolume")
                    ).name()
                );
            }
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            Logger.error(this, "#path(): failed with %[exception]s", ex);
        }
        return this.directory.path();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String statistics() {
        final StringBuilder text = new StringBuilder();
        text.append("hi");
        return text.toString();
    }

    /**
     * Get current EC2 instance ID.
     * @return EC2 instance ID.
     * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/index.html?AESDG-chapter-instancedata.html">here</a>.
     */
    private static String currentInstance() {
        String instance;
        try {
            // @checkstyle LineLength (1 line)
            instance = RestTester.start(URI.create("http://169.254.169.254/latest/meta-data/instance-id"))
                .get("loading current EC2 instance ID")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .getBody();
        } catch (AssertionError ex) {
            instance = "unknown";
        }
        Logger.info(
            EbsVolume.class,
            "#currentInstance(): detected '%s'",
            instance
        );
        return instance;
    }

}
