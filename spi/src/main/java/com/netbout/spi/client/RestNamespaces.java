/**
 * Copyright (c) 2009-2012, Netbout.com
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

import com.rexsl.test.RestTester;
import com.rexsl.test.TestResponse;
import com.rexsl.test.XmlDocument;
import com.ymock.util.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Namespaces.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RestNamespaces extends AbstractMap<String, URL> {

    /**
     * Entry point.
     */
    private final transient UriBuilder home;

    /**
     * Public ctor.
     * @param uri Home of the person
     */
    public RestNamespaces(final UriBuilder uri) {
        super();
        this.home = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<Map.Entry<String, URL>> entrySet() {
        final Set<Map.Entry<String, URL>> namespaces =
            new HashSet<Map.Entry<String, URL>>();
        for (XmlDocument node
            : this.entry().nodes("/page/namespaces/namespace")) {
            try {
                namespaces.add(
                    new AbstractMap.SimpleEntry<String, URL>(
                        node.xpath("name/text()").get(0),
                        new URL(node.xpath("template/text()").get(0))
                    )
                );
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        return namespaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL put(final String name, final URL url) {
        final Set<Map.Entry<String, URL>> namespaces = this.entrySet();
        final Iterator<Map.Entry<String, URL>> iterator = namespaces.iterator();
        boolean exists = false;
        while (iterator.hasNext()) {
            final Map.Entry<String, URL> entry = iterator.next();
            if (entry.getKey().equals(name)) {
                if (entry.getValue().equals(url)) {
                    exists = true;
                    break;
                } else {
                    iterator.remove();
                }
            }
        }
        if (exists) {
            Logger.warn(
                this,
                "#put('%s', '%s'): already exists, won't repost",
                name,
                url
            );
        } else {
            namespaces.add(new AbstractMap.SimpleEntry<String, URL>(name, url));
            this.repost(namespaces);
        }
        return url;
    }

    /**
     * Entry client for namespaces's page.
     * @return The page
     */
    private TestResponse entry() {
        return RestTester.start(this.home)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .get("reading home page of the identity")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .rel("//link[@rel='profile']/@href")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .get("reading profile page")
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * Repost these namespaces.
     * @param namespaces What to repost
     */
    private void repost(final Set<Map.Entry<String, URL>> namespaces) {
        final StringBuilder post = new StringBuilder();
        for (Map.Entry<String, URL> entry : namespaces) {
            post.append(
                String.format(
                    "%s=%s\n",
                    entry.getKey(),
                    entry.getValue().toString()
                )
            );
        }
        this.entry()
            .rel("//link[@rel='namespaces']/@href")
            .post(
                "re-register namespaces",
                String.format("text=%s", URLEncoder.encode(post.toString()))
            )
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

}
