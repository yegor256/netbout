<?xml version="1.0"?>
<!--
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
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:output method="html"/>

    <xsl:include href="/xsl/templates.xsl" />

    <xsl:template match="/">
        <html lang="en-US">
            <head>
                <link href="/css/global.css" rel="stylesheet" type="text/css"
                    media="all"></link>
                <link href="/css/login.css" rel="stylesheet" type="text/css"
                    media="all"></link>
                <link rel="icon" type="image/gif"
                    href="http://cdn.netbout.com/favicon.ico"/>
                <title>login</title>
            </head>
            <body>
                <aside id="version">
                    <xsl:text>r</xsl:text>
                    <xsl:value-of select="/page/version/revision"/>
                    <xsl:text> </xsl:text>
                    <xsl:call-template name="nano">
                        <xsl:with-param name="nano" select="/page/@nano" />
                    </xsl:call-template>
                </aside>
                <table id="wrapper">
                    <tr>
                        <td id="content">
                            <p>
                                <img src="http://img.netbout.com/logo-white.png"
                                    id="logo" alt="logo" />
                            </p>
                            <xsl:if test="/page/message != ''">
                                <aside id="error-message">
                                    <xsl:value-of select="/page/message"/>
                                </aside>
                            </xsl:if>
                            <p>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="/page/links/link[@rel='facebook']/@href" />
                                    </xsl:attribute>
                                    <img src="http://img.netbout.com/facebook.png" id="facebook"/>
                                </a>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
