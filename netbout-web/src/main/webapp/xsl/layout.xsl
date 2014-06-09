<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs">
    <xsl:include href="/xsl/templates.xsl"/>
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html>
            <xsl:attribute name="lang">
                <xsl:value-of select="/page/alias/locale"/>
            </xsl:attribute>
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <script type="text/javascript" src="//code.jquery.com/jquery-2.1.1-rc1.min.js">
                    <xsl:text> </xsl:text>
                    <!-- this is for W3C compliance -->
                </script>
                <script type="text/javascript">
                    <xsl:attribute name="src">
                        <xsl:call-template name="cdn">
                            <xsl:with-param name="name">
                                <xsl:text>supplementary.js?</xsl:text>
                                <xsl:value-of select="version/revision"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:text> </xsl:text>
                    <!-- this is for W3C compliance -->
                </script>
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/style.css?</xsl:text>
                        <xsl:value-of select="version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:call-template name="cdn">
                            <xsl:with-param name="name">
                                <xsl:text>favicon.ico</xsl:text>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </link>
                <xsl:apply-templates select="." mode="head"/>
            </head>
            <body>
                <xsl:apply-templates select="version"/>
                <div class="cap">
                    <div class="incap">
                        <xsl:call-template name="cap"/>
                    </div>
                </div>
                <div class="content" role="main">
                    <xsl:apply-templates select="flash"/>
                    <xsl:apply-templates select="." mode="body"/>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="cap">
        <div class="header">
            <div class="left">
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="links/link[@rel='home']/@href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="$TEXTS/back.to.inbox"/>
                    </xsl:attribute>
                    <img class="logo" alt="netbout logo">
                        <xsl:attribute name="src">
                            <xsl:call-template name="cdn">
                                <xsl:with-param name="name">
                                    <xsl:text>logo/logo-</xsl:text>
                                    <xsl:choose>
                                        <xsl:when test="/page/alias/locale">
                                            <xsl:value-of select="/page/alias/locale"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text>en</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:text>.svg</xsl:text>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:attribute>
                    </img>
                </a>
                <xsl:if test="/page/links/link[@rel='search']">
                    <form class="search" method="get" role="search">
                        <xsl:attribute name="action">
                            <xsl:value-of select="/page/links/link[@rel='search']/@href"/>
                        </xsl:attribute>
                        <input name="q" class="search-input" autocomplete="off" size="10" maxlength="120">
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
                <div class="right">
                    <ul>
                        <li>
                            <img class="photo">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="alias/photo"/>
                                </xsl:attribute>
                                <xsl:attribute name="alt">
                                    <xsl:value-of select="alias/name"/>
                                </xsl:attribute>
                            </img>
                            <xsl:call-template name="identity"/>
                            <xsl:if test="identity/@helper='true'">
                                <xsl:text>&#xA0;(h)</xsl:text>
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
                                        <span class="start">
                                            <xsl:text>+</xsl:text>
                                        </span>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </li>
                        </xsl:if>
                        <xsl:if test="links/link[@rel='about']">
                            <li>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="links/link[@rel='about']/@href"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:value-of select="$TEXTS/About"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="$TEXTS/About"/>
                                </a>
                            </li>
                        </xsl:if>
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="links/link[@rel='rexsl:logout']/@href"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$TEXTS/leave.right.now"/>
                                </xsl:attribute>
                                <i class="ico ico-exit"><xsl:comment>exit</xsl:comment></i>
                            </a>
                        </li>
                    </ul>
                </div>
            </xsl:if>
        </div>
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
                        <xsl:with-param name="text" select="/page/alias/alias"/>
                        <xsl:with-param name="length" select="25"/>
                    </xsl:call-template>
                </a>
            </xsl:when>
            <xsl:when test="/page/alias/name">
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="/page/alias/name"/>
                    <xsl:with-param name="length" select="25"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>unknown</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="flash">
        <div class="flash {level}">
            <xsl:value-of select="message"/>
        </div>
    </xsl:template>
    <xsl:template name="format">
        <xsl:param name="text" as="xs:string"/>
        <xsl:param name="value" as="xs:string"/>
        <xsl:value-of select="substring-before($TEXTS/*[local-name()=$text], '%s')"/>
        <xsl:value-of select="$value"/>
        <xsl:value-of select="substring-after($TEXTS/*[local-name()=$text], '%s')"/>
    </xsl:template>
</xsl:stylesheet>
