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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.amazonaws.services.ec2.model.VolumeAttachmentState;
import com.rexsl.core.Manifests;
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;

/**
 * Amazon EBS mounted volume.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
final class EbsVolume implements Folder {

    /**
     * Mounting directory.
     */
    private final transient File directory = new File(
        System.getProperty("java.io.tmpdir"),
        "netbout-INF"
    );

    /**
     * MOUNT command.
     */
    private final transient File mounter = EbsVolume.bin("/sbin/mount");

    /**
     * EC2 entry point.
     */
    private final transient AmazonEC2 amazon = new AmazonEC2Client(
        new BasicAWSCredentials(
            Manifests.read("Netbout-AwsKey"),
            Manifests.read("Netbout-AwsSecret")
        )
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
    public File path() {
        this.directory.mkdirs();
        try {
            if (!this.mounted()) {
                this.mount(this.attach(Manifests.read("Netbout-EbsVolume")));
            }
        } catch (IOException ex) {
            Logger.error(this, "#path(): failed with %[exception]s", ex);
        }
        return this.directory;
    }

    /**
     * The directory is mounted already?
     * @return Yes or no?
     * @throws IOException If some IO problem inside
     */
    private boolean mounted() throws IOException {
        final String output = this.exec(
            new ProcessBuilder(this.mounter.getPath())
        );
        final boolean mounted = output.contains(this.directory.getPath());
        if (mounted) {
            Logger.info(
                this,
                "#mounted(): '%s' is already mounted:\n%s",
                this.directory,
                output
            );
        } else {
            Logger.info(
                this,
                "#mounted(): '%s' is not mounted yet:\n%s",
                this.directory,
                output
            );
        }
        return mounted;
    }

    /**
     * Mount this device to our directory.
     * @param device Name of device to mount
     * @throws IOException If some IO problem inside
     */
    private void mount(final String device) throws IOException {
        final String output = this.exec(
            new ProcessBuilder(
                this.mounter.getPath(),
                device,
                this.directory.getPath()
            )
        );
        Logger.info(
            this,
            "#mount(%s): mounted as %s:\n%s",
            device,
            this.directory,
            output
        );
    }

    /**
     * Attach this EBS volume and return name of device.
     * @param volume Name of volume
     * @return Name of device to mount
     */
    private String attach(final String volume) {
        this.amazon.attachVolume(
            new AttachVolumeRequest(volume, this.instance, "/dev/hda5")
        );
        return this.device(volume);
    }

    /**
     * Find the name of device which is attached with this volume.
     * @param volume Name of volume
     * @return Name of device
     */
    private String device(final String volume) {
        String device = null;
        int retry = 0;
        final DescribeVolumesRequest request = new DescribeVolumesRequest();
        request.setVolumeIds(Arrays.asList(new String[] {volume}));
        do {
            ++retry;
            try {
                TimeUnit.SECONDS.sleep((long) Math.pow(2, retry));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            for (Volume vol
                : this.amazon.describeVolumes(request).getVolumes()) {
                if (!vol.getVolumeId().equals(volume)) {
                    continue;
                }
                for (VolumeAttachment attachment : vol.getAttachments()) {
                    if (!attachment.getInstanceId().equals(this.instance)) {
                        continue;
                    }
                    Logger.info(
                        this,
                        "#device(%s): retry=%d, state='%s'",
                        volume,
                        retry,
                        attachment.getState()
                    );
                    if (VolumeAttachmentState.Attached.equals(
                        attachment.getState()
                    )) {
                        device = attachment.getDevice();
                        break;
                    }
                }
            }
        } while (device == null);
        return device;
    }

    /**
     * Get current EC2 instance ID.
     * @return EC2 instance ID.
     * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/index.html?AESDG-chapter-instancedata.html">here</a>.
     */
    private static String currentInstance() {
        String instance;
        Logger.info(EbsVolume.class, "#currentInstance(): trying to detect...");
        try {
            instance = (String) new URL(
                "http://169.254.169.254/latest/meta-data/instance-id"
            ).getContent();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.info(
            EbsVolume.class,
            "#currentInstance(): EC2 instance ID '%s'",
            instance
        );
        return instance;
    }

    /**
     * Execute unix command and return it's output as a string.
     * @param bldr The process builder to use
     * @return The output as a string (trimmed)
     * @throws IOException If some IO problem inside
     */
    private String exec(final ProcessBuilder bldr) throws IOException {
        final Process proc = bldr.start();
        int code;
        try {
            code = proc.waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        if (code != 0) {
            throw new IllegalStateException(
                String.format(
                    "Abnormal termination: %s",
                    IOUtils.toString(proc.getErrorStream())
                )
            );
        }
        return IOUtils.toString(proc.getInputStream()).trim();
    }

    /**
     * Find binary and return its full name.
     * @param name Short name
     * @return Full name
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static File bin(final String name) {
        final String[] paths = new String[] {
            "/bin",
            "/usr/bin",
            "/usr/local/bin",
            "/opt/local/bin",
            "/sbin",
            "/usr/sbin",
            "/usr/local/sbin",
            "/opt/local/sbin",
        };
        File file = null;
        for (String path : paths) {
            final File bin = new File(path, name);
            if (bin.exists() && bin.isFile()) {
                file = bin;
                break;
            }
        }
        if (file == null) {
            throw new IllegalStateException(
                String.format(
                    "Failed to find executable of '%s'",
                    name
                )
            );
        }
        return file;
    }

}
