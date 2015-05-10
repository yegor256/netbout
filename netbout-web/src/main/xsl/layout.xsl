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
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml"
    version="1.0" exclude-result-prefixes="xs">
    <xsl:include href="/xsl/templates.xsl"/>
    <xsl:template match="/">
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html>
            <xsl:attribute name="lang">
                <xsl:value-of select="alias/locale"/>
            </xsl:attribute>
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <script type="text/javascript" src="//code.jquery.com/jquery-2.1.1-rc1.min.js">
                    <xsl:text> </xsl:text>
                </script>
                <script type="text/javascript" src="/js/supplementary.js?{version/name}">
                    <xsl:text> </xsl:text>
                </script>
                <link rel="stylesheet" type="text/css" media="all" href="/css/style.css?{version/name}"/>
                <link rel="shortcut icon" type="image/png" href="{links/link[@rel='favicon']/@href}"/>
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
                <a href="{/page/links/link[@rel='home']/@href}" title="{$TEXTS/back.to.inbox}">
                    <img class="logo" alt="netbout logo">
                        <xsl:attribute name="src">
                            <xsl:text>//img.netbout.com/logo/logo-</xsl:text>
                            <xsl:choose>
                                <xsl:when test="/page/alias/locale">
                                    <xsl:value-of select="/page/alias/locale"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>en</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>.svg</xsl:text>
                        </xsl:attribute>
                    </img>
                    <i class="ico ico-home"><xsl:comment>home</xsl:comment></i>
                </a>
                <xsl:if test="/page/links/link[@rel='search']">
                    <form class="search" method="get" role="search" action="{/page/links/link[@rel='search']/@href}">
                        <input name="q" class="search-input" autocomplete="off" size="10" maxlength="120"
                            placeholder="{$TEXTS/Find}" value="{/page/query}">
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
                                <a href="{links/link[@rel='about']/@href}" title="{$TEXTS/About}">
                                    <xsl:value-of select="$TEXTS/About"/>
                                </a>
                            </li>
                        </xsl:if>
                        <li>
                            <a href="{links/link[@rel='rexsl:logout']/@href}" title="{$TEXTS/leave.right.now}">
                                <i class="ico ico-exit">
                                    <xsl:comment>exit</xsl:comment>
                                </i>
                            </a>
                        </li>
                    </ul>
                </div>
            </xsl:if>
        </div>
    </xsl:template>
    <xsl:template name="identity">
        <xsl:choose>
            <xsl:when test="/page/links/link[@rel='account']">
                <a href="{/page/links/link[@rel='account']/@href}" title="{$TEXTS/settings.of.your.profile}">
                    <xsl:call-template name="crop">
                        <xsl:with-param name="text" select="/page/alias/name"/>
                        <xsl:with-param name="length" select="25"/>
                    </xsl:call-template>
                </a>
                <xsl:if test="alias/email = ''">
                    <span id="notice">
                        <xsl:value-of select="$TEXTS/email.empty"/>
                    </span>
                </xsl:if>
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
