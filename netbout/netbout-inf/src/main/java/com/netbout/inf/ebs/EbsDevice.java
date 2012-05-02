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
package com.netbout.inf.ebs;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.amazonaws.services.ec2.model.VolumeAttachmentState;
import com.rexsl.core.Manifests;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Device in UNIX.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class EbsDevice {

    /**
     * How many nanoseconds of waiting we can afford.
     */
    private static final Long MAX_NANO = 2L * 60 * 1000 * 1000 * 1000;

    /**
     * Device to attach to.
     */
    private static final String DEVICE = "/dev/sda5";

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
     * EC2 instance to work with.
     */
    private final transient String instance;

    /**
     * EBS volume to work with.
     */
    private final transient String volume;

    /**
     * Public ctor.
     * @param inst Name of EC2 instance we're working on
     * @param vol Volume to attach
     */
    public EbsDevice(final String inst, final String vol) {
        this.instance = inst;
        this.volume = vol;
    }

    /**
     * Get the name of device (attach beforehand, if necessary).
     * @return Name of device
     * @throws IOException If some IO problem inside
     */
    public String name() throws IOException {
        int retry = 0;
        final long start = System.nanoTime();
        while (true) {
            if (System.nanoTime() - start > this.MAX_NANO) {
                throw new IOException(
                    String.format(
                        "Volume '%s' not attached to '%s', time out",
                        this.volume,
                        this.instance
                    )
                );
            }
            ++retry;
            try {
                TimeUnit.SECONDS.sleep((long) Math.pow(2, retry));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            final VolumeAttachmentState state = this.attach();
            Logger.info(this, "#name(): retry=%d, state='%s'", retry, state);
            if (state.equals(VolumeAttachmentState.Attached)) {
                Logger.info(this, "#name(): attached '%s'", EbsDevice.DEVICE);
                break;
            }
        }
        return EbsDevice.DEVICE;
    }

    /**
     * Attach the device, if necessary.
     * @return The state of device
     * @throws IOException If some IO problem inside
     */
    public VolumeAttachmentState attach() throws IOException {
        final VolumeAttachment attachment = this.attachment();
        VolumeAttachmentState state = null;
        if (attachment == null) {
            state = this.request();
        } else {
            state = VolumeAttachmentState.fromValue(attachment.getState());
        }
        return state;
    }

    /**
     * Send request for attachment.
     * @return The state of device
     * @throws IOException If some IO problem inside
     */
    public VolumeAttachmentState request() throws IOException {
        final String state = this.amazon.attachVolume(
            new AttachVolumeRequest(
                this.volume,
                this.instance,
                EbsDevice.DEVICE
            )
        ).getAttachment().getState();
        Logger.info(
            this,
            "#request(): sent request to attach '%s' to '%s' as '%s'",
            this.volume,
            this.instance,
            EbsDevice.DEVICE
        );
        return VolumeAttachmentState.fromValue(state);
    }

    /**
     * Get attachment for this volume.
     * @return The attachment or NULL if there is no one
     * @throws IOException If some IO problem inside
     */
    private VolumeAttachment attachment() throws IOException {
        final DescribeVolumesRequest request = new DescribeVolumesRequest();
        request.setVolumeIds(Arrays.asList(new String[] {this.volume}));
        VolumeAttachment found = null;
        for (Volume vol : this.amazon.describeVolumes(request).getVolumes()) {
            if (!vol.getVolumeId().equals(this.volume)) {
                continue;
            }
            for (VolumeAttachment attachment : vol.getAttachments()) {
                if (!attachment.getInstanceId().equals(this.instance)) {
                    continue;
                }
                found = attachment;
                break;
            }
        }
        if (found == null) {
            Logger.info(
                this,
                "#attachment(): NULL for volume '%s'",
                this.volume
            );
        } else {
            Logger.info(
                this,
                "#attachment(): volume=%s, state=%s, device=%s",
                found.getVolumeId(),
                found.getState(),
                found.getDevice()
            );
        }
        return found;
    }

}
