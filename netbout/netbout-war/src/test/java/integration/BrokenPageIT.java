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
import com.jayway.restassured.specification.RequestSpecification;
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
 *       implemented. We don't process exceptions thrown by JAX-RS
 *       and don't do any manipulations with them. We should catch every
 *       exceptional situation and convert it to a normal XML response,
 *       formatted by a specific XSL (like any other page on the site).
 */
@Ignore
@RunWith(Parameterized.class)
public final class BrokenPageIT {

    private final String path;

    private final RequestSpecification spec;

    public BrokenPageIT(final String page, final RequestSpecification spc) {
        this.path = page;
        this.spec = spc;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> specs() {
        final Collection<Object[]> specs = new ArrayList<Object[]>();
        specs.add(
            new Object[] {
                "/auth",
                // "password" is not provided, the URL is broken
                RestAssured.given().queryParam("login", "John Doe"),
            }
        );
        specs.add(
            new Object[] {
                // this bout can't be found since the number is too long
                "/637363747382473284973289473778979872",
                RestAssured.given(),
            }
        );
        return specs;
    }

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
    }

    @Test
    public void testOneBrokenPage() throws Exception {
        this.spec
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_BAD_REQUEST))
            .contentType(MediaType.APPLICATION_XML)
            .body(hasXPath("/page"))
            .when()
            .get(this.path);
    }

}
