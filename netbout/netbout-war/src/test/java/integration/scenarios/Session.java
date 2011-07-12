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
package integration.scenarios;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.response.Response;
import com.netbout.rest.AbstractRs;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Session {

    private String token;

    public Session() {
        RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
    }

    public void login(final Long user, final String pwd) throws Exception {
        this.token = RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_TEMPORARY_REDIRECT))
            .cookie(AbstractRs.COOKIE)
            .when()
            .get("/auth")
            .cookie(AbstractRs.COOKIE);
    }

    public Long start(final String subject) throws Exception {
        final String xml = RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_CREATED))
            .cookie(AbstractRs.COOKIE)
            .header(HttpHeaders.LOCATION, anyString())
            .with()
            .parameters("title", subject)
            .when()
            .cookie(AbstractRs.COOKIE, this.token)
            .post("/new")
            .andReturn()
            .asString();
        return XmlPath.with(xml).get("page.bout.number");
    }

    public void invite(final Long bout, final String recipient)
        throws Exception {
        RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_SEE_OTHER))
            .cookie(AbstractRs.COOKIE)
            .header(HttpHeaders.LOCATION, "/" + bout)
            .with()
            .parameters("identity", recipient)
            .when()
            .cookie(AbstractRs.COOKIE, this.token)
            .post("/{bout}/invite", bout);
    }

    public Long accept(final String url) throws Exception {
        final Response response = RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_ACCEPTED))
            .cookie(AbstractRs.COOKIE)
            .when()
            .get(url);
        final String xml = response.asString();
        this.token = response.cookie(AbstractRs.COOKIE);
        return XmlPath.with(xml).get("page.bout.number");
    }

    public void say(final Long bout, final String message)
        throws Exception {
        RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_SEE_OTHER))
            .cookie(AbstractRs.COOKIE)
            .header(HttpHeaders.LOCATION, "/" + bout)
            .with()
            .parameters("message", message)
            .when()
            .cookie(AbstractRs.COOKIE, this.token)
            .post("/{bout}", bout);
    }

    public String recent(final Long bout) throws Exception {
        final String xml = RestAssured
            .expect()
            .logOnError()
            .statusCode(equalTo(HttpStatus.SC_OK))
            .cookie(AbstractRs.COOKIE)
            .when()
            .cookie(AbstractRs.COOKIE, this.token)
            .get("/{bout}", bout)
            .andReturn()
            .asString();
        return XmlPath.with(xml).get(
            "page.bout.messages.message[page.bout.messages.message.size()-1]"
        );
    }

}
