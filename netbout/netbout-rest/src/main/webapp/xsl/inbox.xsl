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

    <xsl:include href="/xsl/layout.xsl" />
    <xsl:include href="/xsl/dudes.xsl" />

    <xsl:template name="head">
        <title>
            <xsl:text>inbox</xsl:text>
            <xsl:variable name="unread">
                <xsl:value-of select="count(/page/bouts/bout[@unseen &gt; 0])"/>
            </xsl:variable>
            <xsl:if test="$unread &gt; 0">
                <xsl:text> (</xsl:text>
                <xsl:value-of select="$unread"/>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </title>
        <script src="/js/dudes.js">
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <link href="/css/inbox.css" rel="stylesheet" type="text/css"/>
        <link href="/css/dudes.css" rel="stylesheet" type="text/css"/>
        <link href="/css/periods.css" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template name="content">
        <xsl:choose>
            <xsl:when test="count(/page/bouts/bout) = 0 and /page/query = ''">
                <header>
                    <h1>
                        <span class="title"><xsl:text>Welcome to Netbout!</xsl:text></span>
                    </h1>
                </header>
                <p>
                    <xsl:text>
                        Netbout is the first system in the world
                        that makes software help us to talk online (not vise versa).
                    </xsl:text>
                </p>
                <p>
                    <xsl:text>
                        Let's start with a simple conversation with someone from
                        our team:
                    </xsl:text>
                </p>
                <form method="post">
                    <xsl:attribute name="action">
                        <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                    </xsl:attribute>
                    <p>
                        <div><xsl:text>We know who you are:</xsl:text></div>
                        <div><input name="name" size="40" disabled="true">
                            <xsl:attribute name="value">
                                <xsl:value-of select="/page/identity/alias"/>
                            </xsl:attribute>
                        </input></div>
                        <div><xsl:text>What we will talk about?</xsl:text></div>
                        <div><textarea name="starter" style="width: 30em; height: 5em;"></textarea></div>
                        <div><input type="submit" value="Start"/></div>
                    </p>
                </form>
                <p>
                    <xsl:text>Keep in mind, we are still testing :)</xsl:text>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="/page/view != ''">
                    <ul class="periods">
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                                </xsl:attribute>
                                <xsl:text>back to recent bouts</xsl:text>
                            </a>
                        </li>
                    </ul>
                </xsl:if>
                <nav>
                    <ul class="bouts">
                        <xsl:for-each select="/page/bouts/bout">
                            <xsl:apply-templates select="." />
                        </xsl:for-each>
                    </ul>
                </nav>
                <xsl:if test="/page/periods[count(link) &gt; 0]">
                    <nav>
                        <ul class="periods">
                            <xsl:for-each select="/page/periods/link">
                                <li>
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="@href"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="label" />
                                        <xsl:if test="@rel='earliest'">
                                            <xsl:text>...</xsl:text>
                                        </xsl:if>
                                    </a>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </nav>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="bout">
        <li class="bout">
            <xsl:attribute name="id">
                <xsl:text>bout</xsl:text>
                <xsl:value-of select="number"/>
            </xsl:attribute>
            <div class="header">
                <span class="num">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="number" />
                </span>
                <a class="title">
                    <xsl:attribute name="href">
                        <xsl:value-of select="link[@rel='page']/@href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="title = ''">
                            <xsl:text>(no title)</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="title" />
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <xsl:if test="@unseen &gt; 0">
                    <span class="red">
                        <xsl:value-of select="@unseen"/>
                        <xsl:text> new</xsl:text>
                    </span>
                </xsl:if>
            </div>
            <xsl:apply-templates select="participants" />
            <xsl:if test="bundled">
                <aside class="bundled">
                    <xsl:for-each select="bundled/link[@rel='bout']">
                        <xsl:if test="position() &gt; 1">
                            <span><xsl:text>, </xsl:text></span>
                        </xsl:if>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="@href"/>
                            </xsl:attribute>
                            <xsl:value-of select="number" />
                        </a>
                        <span>
                            <xsl:text>: </xsl:text>
                            <xsl:choose>
                                <xsl:when test="title = ''">
                                    <xsl:text>(no title)</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="title" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </span>
                    </xsl:for-each>
                    <xsl:if test="bundled/link[@rel='all']">
                        <span><xsl:text>, and </xsl:text></span>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="bundled/link[@rel='all']/@href"/>
                            </xsl:attribute>
                            <xsl:text>all of them</xsl:text>
                        </a>
                    </xsl:if>
                </aside>
            </xsl:if>
        </li>
    </xsl:template>

</xsl:stylesheet>
