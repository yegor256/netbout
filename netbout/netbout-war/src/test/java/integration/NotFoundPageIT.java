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
 * incident to the author by email: privacy@netbout.com.
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
package integration;

import com.jayway.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.Matchers.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #107 This test doesn't work because functionality is not
 *       implemented. We don't process exceptions thrown by servlet container
 *       and don't do any manipulations with them. We should catch every
 *       exceptional situation and convert it to a normal XML response,
 *       formatted by a specific XSL (like any other page on the site).
 */
@Ignore
@RunWith(Parameterized.class)
public final class NotFoundPageIT {

    /**
     * Full list of URLs to test.
     */
    private static final String[] URLS = {
        "/css/this-page-is-not-found.css",
        "/images/image-is-not-there.png",
        "/xsl/this-stylesheet-doesnt-exist.xsl",
        // this bout is not found for sure
        "/1746473847",
    };

    private final String path;

    public NotFoundPageIT(final String name) {
        this.path = name;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> paths() {
        final Collection<Object[]> paths = new ArrayList<Object[]>();
        for (String url : NotFoundPageIT.URLS) {
            paths.add(new Object[] {url});
        }
        return paths;
    }

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
    }

    @Test
    public void testOneNotFoundPage() throws Exception {
        RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_NOT_FOUND))
            .contentType(MediaType.APPLICATION_XML)
            .body(hasXPath("/page"))
            .when()
            .get(this.path);
    }

}
