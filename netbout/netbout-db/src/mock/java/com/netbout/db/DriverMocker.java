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
package com.netbout.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@code Driver}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DriverMocker {

    /**
     * The mock.
     */
    private final transient Driver driver = Mockito.mock(Driver.class);

    /**
     * Public ctor.
     * @param mnemo Driver mnemo name
     */
    public DriverMocker(final String mnemo) {
        try {
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation) {
                        final String url =
                            (String) invocation.getArguments()[0];
                        return url.startsWith(String.format("jdbc:%s:", mnemo));
                    }
                }
            ).when(this.driver).acceptsURL(Mockito.anyString());
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation) {
                        final Properties props =
                            (Properties) invocation.getArguments()[0];
                        final DriverPropertyInfo[] info =
                            new DriverPropertyInfo[props.size()];
                        int pos = 0;
                        for (Object name : props.keySet()) {
                            info[pos] = new DriverPropertyInfo(
                                props.getProperty((String) name),
                                ""
                            );
                            pos += 1;
                        }
                        return info;
                    }
                }
            )
                .when(this.driver)
                .getPropertyInfo(
                    Mockito.anyString(),
                    Mockito.any(java.util.Properties.class)
                );
        } catch (java.sql.SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
        Mockito.doReturn(true).when(this.driver).jdbcCompliant();
        Mockito.doReturn(1).when(this.driver).getMajorVersion();
        Mockito.doReturn(0).when(this.driver).getMinorVersion();
        this.withConnection(new ConnectionMocker().mock());
    }

    /**
     * With this connection on board.
     * @param conn The connection
     * @return This object
     */
    public DriverMocker withConnection(final Connection conn) {
        try {
            Mockito.doReturn(conn).when(this.driver).connect(
                Mockito.anyString(),
                Mockito.any(java.util.Properties.class)
            );
        } catch (java.sql.SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Mock it and register in DriverManager.
     * @return Driver class name
     */
    public String mock() {
        try {
            DriverManager.registerDriver(this.driver);
        } catch (java.sql.SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this.driver.getClass().getName();
    }

}
