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
package com.netbout.rest.jet;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * S3 jet.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class S3Jet implements Jet {

    /**
     * {@inheritDoc}
     */
    @Override
    public Response build(final URI uri) throws IOException {
        final String[] info = uri.getUserInfo().split(":", 2);
        final AWSCredentials creds = new BasicAWSCredentials(info[0], info[1]);
        AmazonS3Client client = new AmazonS3Client(creds);
        final S3Object object = client.getObject(
            uri.getHost(),
            StringUtils.substringAfter(uri.getPath(), "/")
        );
        final ObjectMetadata meta = object.getObjectMetadata();
        return Response
            .ok(new Output(object.getObjectContent()))
            .header(HttpHeaders.CONTENT_TYPE, meta.getContentType())
            .header(HttpHeaders.CONTENT_LENGTH, meta.getContentLength())
            .build();
    }

    /**
     * The content streamer.
     */
    private final class Output implements StreamingOutput {
        /**
         * The input stream with data.
         */
        private final transient InputStream input;
        /**
         * Public ctor.
         * @param stream The stream with data
         */
        public Output(final InputStream stream) {
            this.input = stream;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final OutputStream stream) throws IOException {
            try {
                IOUtils.copy(this.input, stream);
            } finally {
                this.input.close();
            }
        }
    }

}
