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

    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <xsl:param name="TEXTS"
        select="document(concat('/xml/lang/', /page/identity/locale, '.xml'))/texts"/>

    <xsl:include href="/xsl/layout.xsl" />

    <xsl:template name="head">
        <title>
            <xsl:value-of select="$TEXTS/profile"/>
        </title>
        <link href="/css/profile.css" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template name="content">
        <header>
            <h1>
                <span class="title"><xsl:value-of select="$TEXTS/Profile.settings"/></span>
            </h1>
        </header>
        <p>
            <img class="photo">
                <xsl:attribute name="src">
                    <xsl:value-of select="/page/identity/photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="/page/identity/alias"/>
                </xsl:attribute>
            </img>
        </p>
        <p>
            <xsl:value-of select="$TEXTS/Identity"/>
            <xsl:text>: </xsl:text>
            <span class="tt"><xsl:value-of select="/page/identity/name"/></span>
        </p>
        <p>
            <xsl:value-of select="$TEXTS/AKA"/>
            <xsl:text>: </xsl:text>
            <xsl:for-each select="/page/identity/aliases/alias">
                <xsl:if test="position() &gt; 1">
                    <xsl:text>, </xsl:text>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="/page/identity/alias = .">
                        <b><xsl:value-of select="."/></b>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </p>
        <p>
            <xsl:value-of select="$TEXTS/Language"/>
            <xsl:text>: </xsl:text>
            <img class="flag">
                <xsl:attribute name="src">
                    <xsl:text>http://cdn.netbout.com/lang/</xsl:text>
                    <xsl:value-of select="/page/identity/locale"/>
                    <xsl:text>.png</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="/page/identity/locale"/>
                </xsl:attribute>
            </img>
            <xsl:text> </xsl:text>
            <xsl:value-of select="$TEXTS/switch.to"/>
            <xsl:text> : </xsl:text>
            <xsl:for-each select="/page/profile/locales/link">
                <xsl:if test="code != /page/identity/locale">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="@href"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="code" />
                        </xsl:attribute>
                        <img class="flag">
                            <xsl:attribute name="src">
                                <xsl:text>http://cdn.netbout.com/lang/</xsl:text>
                                <xsl:value-of select="code"/>
                                <xsl:text>.png</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:value-of select="code"/>
                            </xsl:attribute>
                        </img>
                    </a>
                    <xsl:text> </xsl:text>
                </xsl:if>
            </xsl:for-each>
        </p>
    </xsl:template>

</xsl:stylesheet>
