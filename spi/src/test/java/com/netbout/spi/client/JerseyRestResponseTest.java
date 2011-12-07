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

import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link JerseyRestResponse}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JerseyRestResponseTest {

    /**
     * JerseyRestResponse can assert HTTP status on response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void assertsStatusOnClientResponse() throws Exception {
        final JerseyRestResponse resp = new JerseyRestResponse(
            Mockito.mock(RestClient.class),
            new ClientResponseMocker()
                .withStatus(HttpURLConnection.HTTP_OK)
                .mock()
        );
        resp.assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * JerseyRestResponse can assert HTTP status on response (with error).
     * @throws Exception If there is some problem inside
     */
    @Test(expected = AssertionError.class)
    public void assertsStatusOnClientResponseWithError() throws Exception {
        final JerseyRestResponse resp = new JerseyRestResponse(
            Mockito.mock(RestClient.class),
            new ClientResponseMocker()
                .withStatus(HttpURLConnection.HTTP_NOT_FOUND)
                .mock()
        );
        resp.assertStatus(HttpURLConnection.HTTP_OK);
    }

    /**
     * JerseyRestResponse can assert XPath on response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void assertsXpathOnClientResponse() throws Exception {
        final JerseyRestResponse resp = new JerseyRestResponse(
            Mockito.mock(RestClient.class),
            new ClientResponseMocker()
                .withEntity("<data><a href='#'/><b/></data>")
                .mock()
        );
        resp.assertXPath("/data/a[@href='#']");
    }

    /**
     * JerseyRestResponse can assert XPath on response (with error).
     * @throws Exception If there is some problem inside
     */
    @Test(expected = AssertionError.class)
    public void assertsXpathOnClientResponseWithError() throws Exception {
        final JerseyRestResponse resp = new JerseyRestResponse(
            Mockito.mock(RestClient.class),
            new ClientResponseMocker()
                .withEntity("<data><foo a='bar'/></data>")
                .mock()
        );
        resp.assertXPath("/data/foo[@a='foo']");
    }

    /**
     * JerseyRestResponse can retrieve data by XPath query.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void retrievesDataByXpathQuery() throws Exception {
        final JerseyRestResponse resp = new JerseyRestResponse(
            Mockito.mock(RestClient.class),
            new ClientResponseMocker()
                .withEntity("<data><foo a='1'>test</foo><foo/></data>")
                .mock()
        );
        MatcherAssert.assertThat(
            resp.xpath("/data/foo[@a='1']/text()").get(0),
            Matchers.equalTo("test")
        );
    }

}
