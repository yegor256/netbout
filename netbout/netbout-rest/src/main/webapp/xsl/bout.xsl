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

    <xsl:variable name="title">
        <xsl:text>#</xsl:text>
        <xsl:value-of select="/page/bout/number"/>
        <xsl:text>: </xsl:text>
        <xsl:choose>
            <xsl:when test="/page/bout/title != ''">
                <xsl:value-of select="/page/bout/title"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>untitled</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="participant"
        select="/page/bout/participants/participant[identity=/page/identity/name]"/>

    <xsl:template name="head">
        <title><xsl:value-of select="$title"/></title>
        <link href="/css/bout.css" rel="stylesheet" type="text/css"></link>
        <link href="/css/dudes.css" rel="stylesheet" type="text/css"></link>
        <xsl:if test="/page/bout/stage">
            <xsl:apply-templates select="/page/bout/stage" mode="head" />
        </xsl:if>
    </xsl:template>

    <xsl:template name="content">
        <xsl:value-of select="$title"/>
        <xsl:apply-templates select="/page/bout/participants" />
        <xsl:if test="$participant/@confirmed = 'true'">
            <form method="get">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@name='suggest']/@href"/>
                </xsl:attribute>
                <input name="q">
                    <xsl:attribute name="value">
                        <xsl:value-of select="/page/keyword"/>
                    </xsl:attribute>
                </input>
                <input value="invite" type="submit"/>
            </form>
            <xsl:if test="/page/invitees">
                <ul>
                    <xsl:for-each select="/page/invitees/invitee">
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="@href"/>
                                </xsl:attribute>
                                <xsl:value-of select="alias"/>
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$participant/@confirmed = 'true'">
            <form method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@name='rename']/@href"/>
                </xsl:attribute>
                <input name="title" size="50">
                    <xsl:attribute name="value">
                        <xsl:value-of select="/page/bout/title"/>
                    </xsl:attribute>
                </input>
                <input value="rename" type="submit"/>
            </form>
        </xsl:if>
        <p>
            <xsl:choose>
                <xsl:when test="$participant/@confirmed = 'true'">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@name='leave']/@href"/>
                        </xsl:attribute>
                        <xsl:text>I want to leave this bout</xsl:text>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Do you agree to join this bout: </xsl:text>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@name='join']/@href"/>
                        </xsl:attribute>
                        <xsl:text>yes, of course</xsl:text>
                    </a>
                    <xsl:text> or </xsl:text>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@name='leave']/@href"/>
                        </xsl:attribute>
                        <xsl:text>no, I refuse</xsl:text>
                    </a>
                </xsl:otherwise>
            </xsl:choose>
        </p>
        <xsl:if test="/page/bout/stages">
            <ul id="titles">
                <xsl:for-each select="/page/bout/stages/stage">
                    <xsl:choose>
                        <xsl:when test=". = /page/bout/stage/@name">
                            <li active="true">
                                <xsl:value-of select="."/>
                            </li>
                        </xsl:when>
                        <xsl:otherwise>
                            <li>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="@href"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="."/>
                                </a>
                            </li>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </ul>
        </xsl:if>
        <xsl:if test="/page/bout/stage">
            <div id="stage">
                <xsl:apply-templates select="/page/bout/stage"/>
            </div>
        </xsl:if>
        <xsl:if test="$participant/@confirmed = 'true'">
            <form id="post" method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@name='post']/@href"/>
                </xsl:attribute>
                <dl><textarea name="text" cols="80" rows="5"></textarea></dl>
                <dl><input name="submit" type="submit" /></dl>
            </form>
        </xsl:if>
        <xsl:apply-templates select="/page/bout/messages/message" />
    </xsl:template>

    <xsl:template match="message">
        <xsl:variable name="msg" select="."/>
        <div class="message">
            <div class="header">
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/photo"/>
                    </xsl:attribute>
                </img>
                <xsl:text>by </xsl:text>
                <b>
                <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/alias"/>
                </b>
                <xsl:text> at </xsl:text>
                <xsl:value-of select="date"/>
                <span style="color: red;">
                    <xsl:if test="@seen = 'false'">
                        <xsl:text> new</xsl:text>
                    </xsl:if>
                </span>
            </div>
            <xsl:value-of select="text"/>
        </div>
    </xsl:template>

</xsl:stylesheet>
