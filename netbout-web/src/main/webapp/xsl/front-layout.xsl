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
    version="2.0" exclude-result-prefixes="xs">
    <xsl:include href="/xsl/templates.xsl"/>
    <xsl:template match="/">
        <!-- see http://stackoverflow.com/questions/3387127 -->
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html>
            <xsl:attribute name="lang">
                <xsl:value-of select="/page/alias/locale"/>
            </xsl:attribute>
            <head>
                <meta charset="UTF-8" />
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/style.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:call-template name="cdn">
                            <xsl:with-param name="name">
                                <xsl:text>favicon.ico</xsl:text>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </link>
                <script type="text/javascript" src="https://code.jquery.com/jquery-2.1.1-rc1.min.js">
                    <xsl:text> </xsl:text>
                    <!-- this is for W3C compliance -->
                </script>
                <xsl:call-template name="head"/>
            </head>
            <body>
                <xsl:apply-templates select="version" />
                <div id="wrapper">
                    <div id="content">
                        <div>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="/page/links/link[@rel='home']/@href"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:text>back home</xsl:text>
                                </xsl:attribute>
                                <img class="logo" alt="back home">
                                    <xsl:attribute name="src">
                                        <xsl:text>http://img.netbout.com/logo/logo-en.png?</xsl:text>
                                        <xsl:value-of select="/page/version/revision"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                        </div>
                        <xsl:if test="/page/message != ''">
                            <div class="error-message">
                                <xsl:value-of select="/page/message"/>
                            </div>
                        </xsl:if>
                        <xsl:call-template name="content"/>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
