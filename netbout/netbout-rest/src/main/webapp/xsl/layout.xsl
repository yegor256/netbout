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
        <xsl:apply-templates select="page" />
    </xsl:template>

    <xsl:template match="page">
        <html lang="en-US">
            <head>
                <link href="/css/global.css" rel="stylesheet" type="text/css"
                    media="all"></link>
                <link rel="icon" type="image/gif" href="http://img.netbout.com/favicon.ico"/>
                <xsl:call-template name="head" />
            </head>
            <body>
                <xsl:apply-templates select="version" />
                <header>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='home']/@href"/>
                        </xsl:attribute>
                        <img src="http://img.netbout.com/logo.png"/>
                    </a>
                    <xsl:if test="identity">
                        <nav id="crumbs" role="navigation">
                            <ul>
                                <li>
                                    <img id="photo">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="identity/photo"/>
                                        </xsl:attribute>
                                    </img>
                                </li>
                                <li>
                                    <xsl:value-of select="identity/alias"/>
                                </li>
                                <li>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="links/link[@rel='start']/@href"/>
                                        </xsl:attribute>
                                        <xsl:text>start</xsl:text>
                                    </a>
                                </li>
                                <li>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="links/link[@rel='logout']/@href"/>
                                        </xsl:attribute>
                                        <xsl:text>logout</xsl:text>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                        <form id="search" method="get" role="search">
                            <xsl:attribute name="action">
                                <xsl:value-of select="/page/links/link[@rel='self']"/>
                            </xsl:attribute>
                            <input name="q" autofocus="true" type="search" required="true">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="/page/query"/>
                                </xsl:attribute>
                            </input>
                            <input value="find" type="submit" />
                        </form>
                    </xsl:if>
                </header>
                <xsl:if test="message != ''">
                    <aside id="message">
                        <xsl:value-of select="message"/>
                    </aside>
                </xsl:if>
                <section id="content" role="main">
                    <xsl:call-template name="content" />
                </section>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="version">
        <aside id="version">
            <xsl:text>r</xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="nano">
                <xsl:with-param name="nano" select="/page/@nano" />
            </xsl:call-template>
        </aside>
    </xsl:template>

</xsl:stylesheet>
