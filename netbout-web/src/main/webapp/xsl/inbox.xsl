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
    <xsl:param name="TEXTS"
        select="document(concat('/xml/lang/', /page/identity/locale, '.xml?', /page/version/revision))/texts"/>
    <xsl:include href="/xsl/layout.xsl" />
    <xsl:include href="/xsl/dudes.xsl" />
    <xsl:template name="head">
        <title>
            <xsl:value-of select="$TEXTS/inbox"/>
            <xsl:variable name="unread">
                <xsl:value-of select="count(/page/bouts/bout[@unseen = 'true']) +
                    count(//bout/bundled/link[@rel='bout' and unseen = 'true'])"/>
            </xsl:variable>
            <xsl:if test="$unread &gt; 0">
                <xsl:text> (</xsl:text>
                <xsl:value-of select="$unread"/>
                <xsl:text>)</xsl:text>
            </xsl:if>
        </title>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/dudes.js?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:text>/css/inbox.css?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
        </link>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:text>/css/dudes.css?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
        </link>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:text>/css/periods.css?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
        </link>
    </xsl:template>

    <xsl:template name="content">
        <xsl:choose>
            <xsl:when test="count(/page/bouts/bout) = 0 and /page/query = ''">
                <h1>
                    <span class="title"><xsl:text>Welcome to Netbout!</xsl:text></span>
                </h1>
                <p>
                    <xsl:value-of select="$TEXTS/Lets.start"/>
                </p>
                <form method="post">
                    <xsl:attribute name="action">
                        <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                    </xsl:attribute>
                    <p>
                        <label for="starter">
                            <xsl:value-of select="$TEXTS/What.to.talk.about"/>
                        </label>
                        <textarea name="starter" style="width: 30em; height: 5em;" id="starter">
                            <xsl:text>&#10;</xsl:text>
                        </textarea>
                        <label for="submit"><xsl:text> </xsl:text></label>
                        <input type="submit" id="submit">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$TEXTS/Start"/>
                            </xsl:attribute>
                        </input>
                    </p>
                </form>
                <p>
                    <xsl:value-of select="$TEXTS/We.are.still.testing"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="/page/view != ''">
                    <ul class="periods">
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="/page/links/link[@rel='home']/@href"/>
                                </xsl:attribute>
                                <xsl:value-of select="$TEXTS/back.to.recent.bouts"/>
                            </a>
                        </li>
                    </ul>
                </xsl:if>
                <xsl:if test="count(/page/bouts/bout) &gt; 0">
                    <ul class="bouts">
                        <xsl:for-each select="/page/bouts/bout">
                            <xsl:apply-templates select="." />
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                <xsl:if test="/page/periods[count(link) &gt; 0]">
                    <ul class="periods">
                        <xsl:for-each select="/page/periods/link">
                            <li>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="@href"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="title" />
                                    <xsl:if test="@rel='earliest'">
                                        <xsl:text>...</xsl:text>
                                    </xsl:if>
                                </a>
                            </li>
                        </xsl:for-each>
                    </ul>
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
            <h1 class="bout">
                <span>
                    <xsl:attribute name="class">
                        <xsl:text>num</xsl:text>
                        <xsl:if test="@unseen = 'true'">
                            <xsl:text> red</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="number" />
                </span>
                <a class="title">
                    <xsl:attribute name="href">
                        <xsl:value-of select="link[@rel='page']/@href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="title = ''">
                            <xsl:text>(</xsl:text>
                            <xsl:value-of select="$TEXTS/no.title"/>
                            <xsl:text>)</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="crop">
                                <xsl:with-param name="text" select="title" />
                                <xsl:with-param name="length" select="50" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </h1>
            <xsl:apply-templates select="participants" />
            <xsl:apply-templates select="bundled" />
        </li>
    </xsl:template>

    <xsl:template match="bundled">
        <div class="bundled">
            <xsl:for-each select="link[@rel='bout']">
                <xsl:if test="position() &gt; 1">
                    <span><xsl:text>; </xsl:text></span>
                </xsl:if>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="@href"/>
                    </xsl:attribute>
                    <span>
                        <xsl:if test="unseen = 'true'">
                            <xsl:attribute name="class">
                                <xsl:text>red</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="number" />
                    </span>
                </a>
                <span>
                    <xsl:text>: </xsl:text>
                    <xsl:choose>
                        <xsl:when test="title = ''">
                            <xsl:text>(</xsl:text>
                            <xsl:value-of select="$TEXTS/no.title"/>
                            <xsl:text>)</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="title" />
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </xsl:for-each>
            <xsl:if test="link[@rel='all']">
                <span><xsl:text>; </xsl:text></span>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="link[@rel='all']/@href"/>
                    </xsl:attribute>
                    <xsl:value-of select="$TEXTS/all.of.them"/>
                    <xsl:text>...</xsl:text>
                </a>
            </xsl:if>
        </div>
    </xsl:template>

</xsl:stylesheet>
