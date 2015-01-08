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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    version="1.0" exclude-result-prefixes="xs">
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
        <div class="version">
            <span>
                <xsl:attribute name="style">
                    <xsl:choose>
                        <xsl:when test="contains(name, '-LOCAL')">
                            <xsl:text>color:magenta</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- nothing -->
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="name"/>
            </span>
            <span>
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>https://github.com/yegor256/netbout/commit/</xsl:text>
                        <xsl:value-of select="revision"/>
                    </xsl:attribute>
                    <i class="ico ico-github">
                        <xsl:comment>github</xsl:comment>
                    </i>
                </a>
            </span>
            <span>
                <xsl:attribute name="style">
                    <xsl:choose>
                        <xsl:when test="number(/page/millis) &gt; 3000">
                            <xsl:text>color:red</xsl:text>
                        </xsl:when>
                        <xsl:when test="number(/page/millis) &gt; 1000">
                            <xsl:text>color:orange</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- nothing -->
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:call-template name="millis">
                    <xsl:with-param name="millis" select="/page/millis"/>
                </xsl:call-template>
            </span>
            <span>
                <xsl:attribute name="style">
                    <xsl:choose>
                        <xsl:when test="number(/page/@sla) &gt; 6">
                            <xsl:text>color:red</xsl:text>
                        </xsl:when>
                        <xsl:when test="number(/page/@sla) &gt; 3">
                            <xsl:text>color:orange</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- nothing -->
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="/page/@sla"/>
            </span>
        </div>
    </xsl:template>
</xsl:stylesheet>
