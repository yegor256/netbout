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
            <xsl:text>helper</xsl:text>
        </title>
    </xsl:template>

    <xsl:template name="content">
        <header>
            <h1>
                <span class="title">
                    <xsl:text>"</xsl:text>
                    <xsl:value-of select="/page/identity/name"/>
                    <xsl:text>"</xsl:text>
                    <xsl:if test="/page/identity/@helper='true'">
                        <xsl:text> (helper)</xsl:text>
                    </xsl:if>
                </span>
            </h1>
        </header>
        <article id="plain">
            <xsl:choose>
                <xsl:when test="/page/identity/@helper='true'">
                    <p>
                        <xsl:text>You're a helper already with this URL: </xsl:text>
                        <span class="tt"><xsl:value-of select="/page/identity/location"/></span>
                    </p>
                    <p>
                        <xsl:text>These operations are supported:</xsl:text>
                    </p>
                    <ul>
                        <xsl:for-each select="/page/identity/supports/operation">
                            <li>
                                <span class="tt"><xsl:value-of select="."/></span>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:when>
                <xsl:otherwise>
                    <p>
                        <xsl:text>
                            You're not a helper yet. Fill this form with
                            a URL of your JAR and get promoted:
                        </xsl:text>
                    </p>
                    <form method="post">
                        <xsl:attribute name="action">
                            <xsl:value-of select="/page/links/link[@rel='promote']/@href"/>
                        </xsl:attribute>
                        <p>
                            <input name="url" type="url" size="50" autocomplete="off">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="/page/identity/location"/>
                                </xsl:attribute>
                            </input>
                            <input value="promote" type="submit"/>
                        </p>
                    </form>
                </xsl:otherwise>
            </xsl:choose>
            <p>
                <xsl:text>Namespaces registered for you (</xsl:text>
                <span class="tt"><xsl:text>&lt;namespace&gt; "=" &lt;URL template&gt;</xsl:text></span>
                <xsl:text> per line):</xsl:text>
            </p>
            <form method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='namespaces']/@href"/>
                </xsl:attribute>
                <p>
                    <textarea name="text" style="width: 50em; height: 6em;">
                        <xsl:for-each select="/page/namespaces/namespace">
                            <xsl:value-of select="name"/>
                            <xsl:text>=</xsl:text>
                            <xsl:value-of select="template"/>
                            <xsl:text>&#x0d;</xsl:text>
                        </xsl:for-each>
                    </textarea>
                </p>
                <p>
                    <input value="register" type="submit"/>
                </p>
            </form>
        </article>
    </xsl:template>

</xsl:stylesheet>
