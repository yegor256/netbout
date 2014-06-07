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
    <xsl:include href="/xsl/login-layout.xsl" />
    <xsl:template match="page" mode="head">
        <title>netbout - private talks made easy</title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <nav class="buttons">
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='rexsl:facebook']/@href" />
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>click to authenticate yourself via Facebook</xsl:text>
                </xsl:attribute>
                <i class="ico ico-facebook"><xsl:comment>facebook</xsl:comment></i>
            </a>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='rexsl:google']/@href" />
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>click to authenticate yourself via Google+</xsl:text>
                </xsl:attribute>
                <i class="ico ico-google"><xsl:comment>google</xsl:comment></i>
            </a>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='rexsl:github']/@href" />
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>click to authenticate yourself via Github</xsl:text>
                </xsl:attribute>
                <i class="ico ico-github"><xsl:comment>github</xsl:comment></i>
            </a>
        </nav>
    </xsl:template>
</xsl:stylesheet>
