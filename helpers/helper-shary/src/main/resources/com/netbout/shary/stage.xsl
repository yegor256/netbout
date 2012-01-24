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

    <xsl:template match="stage" mode="head">
        <!-- nothing -->
    </xsl:template>

    <xsl:template match="stage">
        <xsl:choose>
            <xsl:when test="data/docs[count(doc) &gt; 0]">
                <table>
                    <colgroup width="25%"/>
                    <colgroup width="25%"/>
                    <colgroup width="25%"/>
                    <colgroup width="25%"/>
                    <tbody>
                        <xsl:for-each select="data/docs/doc[position() mod 4 = 1]">
                            <tr>
                                <xsl:for-each select=".|following-sibling::doc[position() &lt; 4]">
                                    <td>
                                        <xsl:apply-templates select="."/>
                                    </td>
                                </xsl:for-each>
                                <xsl:call-template name="filler">
                                    <xsl:with-param name="rest"
                                        select="4 - count(.|following-sibling::doc[position() &lt; 4])" />
                                </xsl:call-template>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:text>No documents have been shared yet.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="data/place != ''">
            <xsl:apply-templates select="data/place"/>
        </xsl:if>
        <form method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="$stage-home-uri"/>
            </xsl:attribute>
            Name: <input name="name" size="22" maxlength="500"/>
            URI: <input name="uri" size="68" maxlength="500"/>
            <input value="Share it" type="submit"/>
        </form>
    </xsl:template>

    <xsl:template match="doc">
        <p>
            <img src="http://img.netbout.com/shary/doc.png"
                style="width: 2.5em; height: 2.5em;"/>
            <br/>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$stage-home-uri"/>
                    <xsl:value-of select="links/link[@rel='load']/@href"/>
                </xsl:attribute>
                <xsl:attribute name="type">
                    <xsl:value-of select="type"/>
                </xsl:attribute>
                <xsl:value-of select="name"/>
            </a>
            <br/>
            <xsl:text> shared by </xsl:text>
            <xsl:value-of select="author"/>
            <xsl:if test="links/link[@rel='unshare']">
                <xsl:text> (</xsl:text>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$stage-home-uri"/>
                        <xsl:value-of select="links/link[@rel='unshare']/@href"/>
                    </xsl:attribute>
                    <xsl:text>unshare</xsl:text>
                </a>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </p>
    </xsl:template>

    <xsl:template name="filler">
        <xsl:param name="rest" select="0" />
        <xsl:if test="$rest &gt; 0">
            <td/>
            <xsl:call-template name="filler">
                <xsl:with-param name="rest" select="$rest - 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="place">
        <p class="red">
            <xsl:choose>
                <xsl:when test=".='empty-args'">
                    <xsl:text>Both 'name' and 'URI' should be provided.</xsl:text>
                </xsl:when>
                <xsl:when test=".='illegal-uri'">
                    <xsl:text>Format of URI is not recognized, try again.</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Unknown error.</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </p>
    </xsl:template>

</xsl:stylesheet>
