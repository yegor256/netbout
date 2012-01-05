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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@code Connection}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ConnectionMocker {

    /**
     * The mock.
     */
    private final transient Connection connection =
        Mockito.mock(Connection.class);

    /**
     * Public ctor.
     */
    public ConnectionMocker() {
        try {
            Mockito.doAnswer(
                new Answer() {
                    private boolean closed;
                    public Object answer(final InvocationOnMock invocation) {
                        // this.closed = !this.closed;
                        // return this.closed;
                        System.out.println("isClosed()");
                        return false;
                    }
                }
            ).when(this.connection).isClosed();
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation) {
                        System.out.println("close()");
                        return false;
                    }
                }
            ).when(this.connection).close();
            Mockito.doReturn(this.meta()).when(this.connection).getMetaData();
            final Statement stmt = Mockito.mock(Statement.class);
            Mockito.doReturn(stmt).when(this.connection).createStatement();
            final PreparedStatement pstmt =
                Mockito.mock(PreparedStatement.class);
            Mockito.doReturn(pstmt).when(this.connection)
                .prepareStatement(Mockito.anyString());
            final ResultSet set = Mockito.mock(ResultSet.class);
            Mockito.doReturn(set).when(stmt).executeQuery(Mockito.anyString());
        } catch (java.sql.SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Mock it.
     * @return The connection
     */
    public Connection mock() {
        return this.connection;
    }

    /**
     * Make database meta data.
     * @return The data
     */
    private DatabaseMetaData meta() {
        final DatabaseMetaData meta = Mockito.mock(DatabaseMetaData.class);
        try {
            Mockito.doReturn("mysql").when(meta).getDatabaseProductName();
            Mockito.doReturn(this.connection).when(meta).getConnection();
            Mockito.doReturn("jdbc:foo").when(meta).getURL();
            Mockito.doReturn(Mockito.mock(ResultSet.class))
                .when(meta).getTables(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    (String[]) Mockito.anyObject()
                );
        } catch (java.sql.SQLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return meta;
    }

}
