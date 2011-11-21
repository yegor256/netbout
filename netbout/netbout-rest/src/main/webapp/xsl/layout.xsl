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
    xmlns:nb="http://www.netbout.com"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:include href="/xsl/templates.xsl" />

    <xsl:template match="/">
        <html>
            <head>
                <title>
                    <xsl:call-template name="title" />
                </title>
                <xsl:call-template name="head" />
                <link href="/css/global.css" rel="stylesheet" type="text/css"></link>
                <link rel="icon" type="image/gif" href="http://img.netbout.com/favicon.ico"/>
            </head>
            <body>
                <div id="version">
                    <xsl:text>r</xsl:text>
                    <xsl:value-of select="/page/version/revision"/>
                    <xsl:text> </xsl:text>
                    <xsl:call-template name="nano">
                        <xsl:with-param name="nano" select="/page/@nano" />
                    </xsl:call-template>
                </div>
                <div id="bar">
                    <a id="logo">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@name='home']/@href"/>
                        </xsl:attribute>
                        <img src="http://img.netbout.com/logo.png"/>
                    </a>
                    <div id="crumbs">
                        <xsl:choose>
                            <xsl:when test="/page/identity">
                                <span>
                                    <img id="photo">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="/page/identity/photo"/>
                                        </xsl:attribute>
                                    </img>
                                </span>
                                <span>
                                    <xsl:value-of select="/page/identity/aliases/alias[position() = 1]"/>
                                </span>
                                <span>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="/page/links/link[@name='start']/@href"/>
                                        </xsl:attribute>
                                        <xsl:text>start</xsl:text>
                                    </a>
                                </span>
                                <span>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="/page/links/link[@name='logout']/@href"/>
                                        </xsl:attribute>
                                        <xsl:text>logout</xsl:text>
                                    </a>
                                </span>
                            </xsl:when>
                            <xsl:otherwise>
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>
                </div>
                <xsl:if test="/page/identity">
                    <form id="box">
                        <input name="q" />
                        <input value="find" type="submit" />
                    </form>
                </xsl:if>
                <xsl:if test="/page/message">
                    <div id="message">
                        <xsl:value-of select="/page/message"/>
                    </div>
                </xsl:if>
                <div id="content">
                    <xsl:call-template name="content" />
                </div>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
