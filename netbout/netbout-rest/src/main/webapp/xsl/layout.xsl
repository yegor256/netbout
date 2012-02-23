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

    <xsl:include href="/xsl/templates.xsl" />

    <xsl:template match="/">
        <!-- see http://stackoverflow.com/questions/3387127 -->
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page" />
    </xsl:template>

    <xsl:template match="page">
        <html lang="en-US">
            <head>
                <meta charset="UTF-8" />
                <script type="text/javascript"
                    src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js">
                    <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
                </script>
                <link href="/css/global.css" rel="stylesheet" type="text/css"
                    media="all"/>
                <link href="/css/layout.css" rel="stylesheet" type="text/css"
                    media="all"/>
                <link rel="icon" type="image/gif"
                    href="http://cdn.netbout.com/favicon.ico"/>
                <xsl:call-template name="head" />
            </head>
            <body>
                <xsl:apply-templates select="version" />
                <div id="cap">
                    <div id="incap">
                        <xsl:call-template name="header" />
                    </div>
                </div>
                <section id="content" role="main">
                    <xsl:if test="message != ''">
                        <aside class="error-message">
                            <xsl:value-of select="message"/>
                        </aside>
                    </xsl:if>
                    <xsl:if test="identity/eta != 0">
                        <aside class="error-message">
                            <xsl:text>The server is currently updating your account,
                                some data may look not as fresh as they should be. Try
                                to refresh the page</xsl:text>
                            <xsl:choose>
                                <xsl:when test="identity/eta &gt; 60000">
                                    <xsl:text> in a few minutes</xsl:text>
                                </xsl:when>
                                <xsl:when test="identity/eta &gt; 5000">
                                    <xsl:text> in </xsl:text>
                                    <xsl:value-of select="round(identity/eta div 1000)"/>
                                    <xsl:text> seconds</xsl:text>
                                </xsl:when>
                            </xsl:choose>
                            <xsl:text>.</xsl:text>
                        </aside>
                    </xsl:if>
                    <xsl:if test="links/link[@rel='re-login']">
                        <aside class="error-message">
                            <xsl:text>We recommend you to re-authenticate yourself: </xsl:text>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='re-login']/@href"/>
                                </xsl:attribute>
                                <xsl:text>click here</xsl:text>
                            </a>
                            <xsl:text>.</xsl:text>
                        </aside>
                    </xsl:if>
                    <xsl:if test="count(log/event) &gt; 0">
                        <aside id="log">
                            <xsl:for-each select="log/event">
                                <p><xsl:value-of select="."/></p>
                            </xsl:for-each>
                        </aside>
                    </xsl:if>
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

    <xsl:template name="header">
        <header id="header">
            <div id="left">
                <a id="logo">
                    <xsl:attribute name="href">
                        <xsl:value-of select="links/link[@rel='home']/@href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:text>back to inbox</xsl:text>
                    </xsl:attribute>
                    <xsl:text> </xsl:text> <!-- for W3C compliance -->
                </a>
                <form id="search" method="get" role="search">
                    <xsl:attribute name="action">
                        <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                    </xsl:attribute>
                    <input name="q" id="search-input" placeholder="Find..."
                        autocomplete="off" size="10" maxlength="120">
                        <xsl:attribute name="value">
                            <xsl:value-of select="/page/query"/>
                        </xsl:attribute>
                        <xsl:if test="/page/query != ''">
                            <xsl:attribute name="autofocus">
                                <xsl:text>true</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                    </input>
                </form>
            </div>
            <xsl:if test="identity">
                <nav id="right" role="navigation">
                    <ul>
                        <li>
                            <img id="photo">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="identity/photo"/>
                                </xsl:attribute>
                                <xsl:attribute name="alt">
                                    <xsl:value-of select="identity/alias"/>
                                </xsl:attribute>
                            </img>
                            <span>
                                <xsl:call-template name="alias">
                                    <xsl:with-param name="alias" select="identity/alias" />
                                </xsl:call-template>
                            </span>
                            <xsl:if test="identity/@helper='true'">
                                <span><xsl:text>&#160;(h)</xsl:text></span>
                            </xsl:if>
                        </li>
                        <xsl:if test="links/link[@rel='start']">
                            <li>
                                <xsl:choose>
                                    <xsl:when test="/page/bouts and count(/page/bouts/bout) = 0 and /page/query = ''">
                                        <span><xsl:text>Start (later)</xsl:text></span>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="links/link[@rel='start']/@href"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:text>start new bout</xsl:text>
                                            </xsl:attribute>
                                            <span><xsl:text>Start</xsl:text></span>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </li>
                        </xsl:if>
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='logout']/@href"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:text>leave Netbout.com right now</xsl:text>
                                </xsl:attribute>
                                <span><xsl:text>Logout</xsl:text></span>
                            </a>
                        </li>
                    </ul>
                </nav>
            </xsl:if>
        </header>
    </xsl:template>

</xsl:stylesheet>
