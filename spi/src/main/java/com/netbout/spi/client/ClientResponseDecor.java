/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.sun.jersey.api.client.ClientResponse;
import com.ymock.util.Decor;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;

/**
 * Decor for {@link ClientResponse}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Decor("ClientResponseDecor")
public final class ClientResponseDecor implements Formattable {

    /**
     * The response.
     */
    private final transient ClientResponse response;

    /**
     * Public ctor.
     * @param resp The response
     */
    public ClientResponseDecor(final Object resp) {
        this.response = (ClientResponse) resp;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public void formatTo(final Formatter formatter, final int flags,
        final int width, final int precision) {
        final StringBuilder builder = new StringBuilder();
        for (MultivaluedMap.Entry<String, List<String>> header
            : this.response.getHeaders().entrySet()) {
            builder.append(
                String.format(
                    "\t%s: %s%n",
                    header.getKey(),
                    StringUtils.join(header.getValue(), ", ")
                )
            );
        }
        formatter.format("%s", builder.toString());
    }

}
