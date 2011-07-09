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
package integration.css;

import com.jayway.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Here we test that CSS compression really works, and CSS comments
 * are not visible to public.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class CssCompressionIT {

    /**
     * Full list of URLs to test.
     */
    private static final String[] URLS = {
        "/css/global.css",
        "/css/front.css",
        "/css/bout.css",
    };

    private final String path;

    public CssCompressionIT(final String name) {
        this.path = name;
    }

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> paths() {
        final Collection<Object[]> paths = new ArrayList<Object[]>();
        for (String url : CssCompressionIT.URLS) {
            paths.add(new Object[] {url});
        }
        return paths;
    }

    @Test
    public void testOnePageRendering() throws Exception {
        final String css = RestAssured.get(this.path).asString();
        assertThat(css, not(containsString("/*")));
    }

}
