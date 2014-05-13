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
    xmlns="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="TEXTS"
        select="document(concat('/lang/', /page/alias/locale, '.xml?', /page/version/revision))/texts"/>
    <xsl:include href="/xsl/layout.xsl" />
    <xsl:include href="/xsl/friends.xsl" />
    <xsl:template match="page" mode="head">
        <title>
            <xsl:value-of select="$TEXTS/inbox"/>
            <xsl:variable name="unread">
                <xsl:value-of select="count(bouts/bout[@unseen = 'true'])"/>
            </xsl:variable>
            <xsl:if test="$unread &gt; 0">
                <xsl:text> (</xsl:text>
                <xsl:value-of select="$unread"/>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </title>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/friends.js?</xsl:text>
                <xsl:value-of select="version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <xsl:choose>
            <xsl:when test="count(bouts/bout) = 0">
                <h1>
                    <span class="title"><xsl:text>Welcome to netbout!</xsl:text></span>
                </h1>
                <p>
                    <xsl:value-of select="$TEXTS/Lets.start"/>
                </p>
                <p>
                    <xsl:value-of select="$TEXTS/We.are.still.testing"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <ul class="bouts">
                    <xsl:for-each select="bouts/bout">
                        <xsl:apply-templates select="." />
                    </xsl:for-each>
                </ul>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="bout">
        <li class="bout">
            <xsl:attribute name="id">
                <xsl:text>bout</xsl:text>
                <xsl:value-of select="number"/>
            </xsl:attribute>
            <h1 class="bout">
                <span>
                    <xsl:attribute name="class">
                        <xsl:text>num</xsl:text>
                        <xsl:if test="@unseen = 'true'">
                            <xsl:text> red</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="number" />
                </span>
                <a class="title">
                    <xsl:attribute name="href">
                        <xsl:value-of select="links/link[@rel='open']/@href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="title = ''">
                            <xsl:text>(</xsl:text>
                            <xsl:value-of select="$TEXTS/no.title"/>
                            <xsl:text>)</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="crop">
                                <xsl:with-param name="text" select="title" />
                                <xsl:with-param name="length" select="50" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </h1>
            <xsl:apply-templates select="friends" />
        </li>
    </xsl:template>
</xsl:stylesheet>
