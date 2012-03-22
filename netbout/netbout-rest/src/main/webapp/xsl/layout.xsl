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
        <html>
            <xsl:attribute name="lang">
                <xsl:value-of select="/page/identity/locale"/>
            </xsl:attribute>
            <head>
                <meta charset="UTF-8" />
                <script type="text/javascript"
                    src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js">
                    <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
                </script>
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/global.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/layout.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:text>http://cdn.netbout.com/favicon.ico?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
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
                            <xsl:value-of select="$TEXTS/the.server.is.busy"/>
                            <xsl:choose>
                                <xsl:when test="identity/eta &gt; 60 * 1000 * 1000 * 1000">
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="$TEXTS/in.a.few.minutes"/>
                                </xsl:when>
                                <xsl:when test="identity/eta &gt; 5 * 1000 * 1000 * 1000">
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="$TEXTS/in"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="round(identity/eta div (1000 * 1000 * 1000))"/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="$TEXTS/sec"/>
                                </xsl:when>
                            </xsl:choose>
                            <xsl:text>.</xsl:text>
                        </aside>
                    </xsl:if>
                    <xsl:if test="links/link[@rel='re-login']">
                        <aside class="error-message">
                            <xsl:value-of select="$TEXTS/We.recommend.to.reauthenticate"/>
                            <xsl:text>: </xsl:text>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='re-login']/@href"/>
                                </xsl:attribute>
                                <xsl:value-of select="$TEXTS/click.here"/>
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
                <xsl:with-param name="nano" select="/page/nano" />
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
                        <xsl:value-of select="$TEXTS/back.to.inbox"/>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:text>background-image: url('http://cdn.netbout.com/logo/logo-</xsl:text>
                        <xsl:choose>
                            <xsl:when test="/page/identity/locale">
                                <xsl:value-of select="/page/identity/locale"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>en</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text>.png?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                        <xsl:text>');</xsl:text>
                    </xsl:attribute>
                    <xsl:text> </xsl:text> <!-- for W3C compliance -->
                </a>
                <xsl:if test="/page/@searcheable = 'true'">
                    <form id="search" method="get" role="search">
                        <xsl:attribute name="action">
                            <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                        </xsl:attribute>
                        <input name="q" id="search-input"
                            autocomplete="off" size="10" maxlength="120">
                            <xsl:attribute name="placeholder">
                                <xsl:value-of select="$TEXTS/Find"/>
                            </xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:value-of select="/page/query"/>
                            </xsl:attribute>
                            <xsl:if test="/page/query != ''">
                                <xsl:attribute name="autofocus">
                                    <xsl:text>autofocus</xsl:text>
                                </xsl:attribute>
                            </xsl:if>
                        </input>
                    </form>
                </xsl:if>
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
                            <xsl:call-template name="identity"/>
                            <xsl:if test="identity/@helper='true'">
                                <xsl:text>&#160;(h)</xsl:text>
                            </xsl:if>
                        </li>
                        <xsl:if test="links/link[@rel='start']">
                            <li>
                                <xsl:choose>
                                    <xsl:when test="/page/bouts and count(/page/bouts/bout) = 0 and /page/query = ''">
                                        <xsl:value-of select="$TEXTS/Start.later"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="links/link[@rel='start']/@href"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="title">
                                                <xsl:value-of select="$TEXTS/start.new.bout"/>
                                            </xsl:attribute>
                                            <xsl:value-of select="$TEXTS/Start"/>
                                        </a>
                                        <span class="start"><xsl:text>+</xsl:text></span>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </li>
                        </xsl:if>
                        <li>
                            <xsl:value-of select="$TEXTS/About"/>
                        </li>
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='logout']/@href"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$TEXTS/leave.right.now"/>
                                </xsl:attribute>
                                <xsl:value-of select="$TEXTS/Logout"/>
                            </a>
                        </li>
                    </ul>
                </nav>
            </xsl:if>
        </header>
    </xsl:template>

    <xsl:template name="identity">
        <xsl:choose>
            <xsl:when test="/page/links/link[@rel='profile']">
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="/page/links/link[@rel='profile']/@href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$TEXTS/settings.of.your.profile"/>
                    </xsl:attribute>
                    <xsl:call-template name="crop">
                        <xsl:with-param name="text" select="/page/identity/alias" />
                        <xsl:with-param name="length" select="25" />
                    </xsl:call-template>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="/page/identity/alias" />
                    <xsl:with-param name="length" select="25" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="format">
        <xsl:param name="text" as="xs:string"/>
        <xsl:param name="value" as="xs:string"/>
        <xsl:value-of select="substring-before($TEXTS/*[local-name()=$text], '%s')"/>
        <xsl:value-of select="$value"/>
        <xsl:value-of select="substring-after($TEXTS/*[local-name()=$text], '%s')"/>
    </xsl:template>

</xsl:stylesheet>
