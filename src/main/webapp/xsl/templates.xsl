<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:template name="millis">
        <xsl:param name="millis" as="xs:integer"/>
        <xsl:choose>
            <xsl:when test="$millis &gt; 1000">
                <xsl:value-of select="format-number($millis div 1000, '0.0')"/>
                <xsl:text>s</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($millis, '#')"/>
                <xsl:text>ms</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="crop">
        <xsl:param name="text" as="xs:string"/>
        <xsl:param name="length" as="xs:integer"/>
        <xsl:choose>
            <xsl:when test="string-length($text) &gt; $length">
                <xsl:value-of select="substring($text, 0, $length - 3)"/>
                <xsl:text>...</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="version">
        <xsl:if test="contains(name, '-')">
            <div style="position: fixed; left: 0px; bottom: 0px; color: gray;
                font-size: 5em; text-align: left;">
                <xsl:value-of select="name"/>
            </div>
        </xsl:if>
        <div id="version">
            <span>
                <xsl:attribute name="style">
                    <xsl:choose>
                        <xsl:when test="contains(name, '-LOCAL')">
                            color: magenta;
                        </xsl:when>
                        <xsl:when test="contains(name, '-SNAPSHOT')">
                            color: red;
                        </xsl:when>
                        <xsl:when test="contains(name, '-RC')">
                            color: green;
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- nothing -->
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="name"/>
            </span>
            <xsl:text> r</xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="millis">
                <xsl:with-param name="millis" select="/page/millis" />
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="cdn">
        <xsl:param name="name" as="xs:string"/>
        <xsl:text>http</xsl:text>
        <xsl:if test="/page/identity">
            <xsl:text>s</xsl:text>
        </xsl:if>
        <xsl:text>://</xsl:text>
        <xsl:text>dxe6yfv2r7pzd.cloudfront.net/</xsl:text>
        <xsl:value-of select="$name"/>
        <xsl:text>?</xsl:text>
        <xsl:value-of select="/page/version/revision"/>
    </xsl:template>

</xsl:stylesheet>
