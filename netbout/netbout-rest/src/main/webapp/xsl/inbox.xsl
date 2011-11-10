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

    <xsl:template name="title">
        <xsl:text>inbox</xsl:text>
    </xsl:template>

    <xsl:template name="head">
        <link href="/css/inbox.css" rel="stylesheet" type="text/css"></link>
        <link href="/css/dudes.css" rel="stylesheet" type="text/css"></link>
    </xsl:template>

    <xsl:template name="content">
        <xsl:for-each select="/page/bouts/bout">
            <div class="bout">
                <a class="title unread">
                    <xsl:attribute name="class">
                        <xsl:text>title </xsl:text>
                        <xsl:if test="@unread">
                            <xsl:text>unread</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="@href"/>
                    </xsl:attribute>
                    <xsl:value-of select="number" />
                    <xsl:text>: </xsl:text>
                    <xsl:choose>
                        <xsl:when test="title != ''">
                            <xsl:value-of select="title" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>untitled</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <xsl:call-template name="dudes">
                    <xsl:with-param name="participants" select="participants" />
                </xsl:call-template>
            </div>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
