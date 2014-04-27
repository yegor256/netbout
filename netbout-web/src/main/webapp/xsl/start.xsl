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
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:include href="/xsl/front-layout.xsl" />
    <xsl:template name="head">
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/start.js?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <link rel="stylesheet" type="text/css" media="all">
            <xsl:attribute name="href">
                <xsl:text>/css/start.css?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
        </link>
        <title>Netbout - private talks made easy</title>
    </xsl:template>
    <xsl:template name="content">
        <p>
            <img style="width: 32px;">
                <xsl:attribute name="src">
                    <xsl:value-of select="identity/photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="identity/alias"/>
                </xsl:attribute>
            </img>
        </p>
        <form method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="/page/links/link[@rel='register']/@href"/>
            </xsl:attribute>
            <fieldset>
                <label for="alias">
                    <xsl:text>What will be your unique name visible to everybody?</xsl:text>
                </label>
                <input id="alias" name="alias" size="35" maxlength="100">
                    <xsl:attribute name="data-check">
                        <xsl:value-of select="/page/links/link[@rel='check']/@href"/>
                    </xsl:attribute>
                </input>
                <label for="submit">
                    <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
                </label>
                <input id="submit" type="submit" value="register" disabled="disabled" />
            </fieldset>
        </form>
        <p id="error"/>
        <p>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='auth-logout']/@href"/>
                </xsl:attribute>
                <xsl:text>logout</xsl:text>
            </a>
        </p>
    </xsl:template>

</xsl:stylesheet>
