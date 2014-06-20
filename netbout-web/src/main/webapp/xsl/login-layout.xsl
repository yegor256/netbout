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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:include href="/xsl/templates.xsl"/>
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <link rel="stylesheet" type="text/css" media="all" href="/css/style.css?{version/revision}"/>
                <link rel="shortcut icon" type="image/png" href="{links/link[@rel='favicon']/@href}"/>
                <script type="text/javascript" src="/js/supplementary.js?{version/revision}">
                    <xsl:text> </xsl:text>
                </script>
                <script type="text/javascript" src="//code.jquery.com/jquery-2.1.1-rc1.min.js">
                    <xsl:text> </xsl:text>
                </script>
                <xsl:apply-templates select="." mode="head"/>
            </head>
            <body>
                <xsl:apply-templates select="version"/>
                <div class="login-wrapper">
                    <div class="login-main">
                        <div>
                            <a href="{links/link[@rel='home']/@href}" title="back home">
                                <img class="login-logo" alt="back home" src="//img.netbout.com/logo.svg?{version/revision}"/>
                            </a>
                        </div>
                        <xsl:apply-templates select="message[text() != '']"/>
                        <xsl:apply-templates select="." mode="body"/>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="message">
        <div class="error-message">
            <xsl:value-of select="."/>
        </div>
    </xsl:template>
</xsl:stylesheet>
