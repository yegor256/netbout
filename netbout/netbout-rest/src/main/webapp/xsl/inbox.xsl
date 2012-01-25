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

    <xsl:output method="xhtml"/>

    <xsl:include href="/xsl/layout.xsl" />
    <xsl:include href="/xsl/dudes.xsl" />

    <xsl:variable name="unread">
        <xsl:value-of select="count(/page/bouts/bout[@seen &lt; @messages])"/>
    </xsl:variable>

    <xsl:template name="head">
        <title>
            <xsl:text>inbox</xsl:text>
            <xsl:if test="$unread &gt; 0">
                <xsl:text> (</xsl:text>
                <xsl:value-of select="$unread"/>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </title>
        <link href="/css/inbox.css" rel="stylesheet" type="text/css"></link>
        <link href="/css/dudes.css" rel="stylesheet" type="text/css"></link>
        <link href="/css/periods.css" rel="stylesheet" type="text/css"></link>
    </xsl:template>

    <xsl:template name="content">
        <xsl:if test="/page/view != ''">
            <ul class="periods">
                <li>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                        </xsl:attribute>
                        <xsl:text>back to recent bouts</xsl:text>
                    </a>
                </li>
            </ul>
        </xsl:if>
        <nav>
            <ul class="bouts">
                <xsl:for-each select="/page/bouts/bout">
                    <xsl:apply-templates select="." />
                </xsl:for-each>
            </ul>
        </nav>
        <nav>
            <ul class="periods">
                <xsl:for-each select="/page/periods/link">
                    <li>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="@href"/>
                            </xsl:attribute>
                            <xsl:value-of select="@label" />
                            <xsl:if test="@rel='earliest'">
                                <xsl:text>...</xsl:text>
                            </xsl:if>
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </nav>
    </xsl:template>

    <xsl:template match="bout">
        <li class="bout">
            <xsl:attribute name="id">
                <xsl:text>bout</xsl:text>
                <xsl:value-of select="number"/>
            </xsl:attribute>
            <div class="header">
                <span class="num">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="number" />
                </span>
                <a class="title">
                    <xsl:attribute name="href">
                        <xsl:value-of select="link[@rel='page']/@href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="title != ''">
                            <xsl:value-of select="title" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>untitled</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <xsl:if test="@seen &lt; @messages">
                    <span class="red">
                        <xsl:value-of select="@messages - @seen"/>
                        <xsl:text> new</xsl:text>
                    </span>
                </xsl:if>
            </div>
            <xsl:apply-templates select="participants" />
        </li>
    </xsl:template>

</xsl:stylesheet>
