<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2012, Netbout.com
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
 * @author Yegor Bugayenko (yegor@netbout.com)
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

    <xsl:variable name="participant"
        select="/page/bout/participants/participant[identity=/page/identity/name]"/>

    <xsl:template name="head">
        <title>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="/page/bout/number"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="/page/bout/title"/>
        </title>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/dudes.js?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/bout.js?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:text>/css/bout.css?</xsl:text>
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
        <xsl:if test="/page/bout/stage">
            <xsl:apply-templates select="/page/bout/stage" mode="head" />
        </xsl:if>
    </xsl:template>

    <xsl:template name="content">
        <h1 class="bout">
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='top']/@href"/>
                </xsl:attribute>
                <span class="num">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="/page/bout/number"/>
                </span>
            </a>
            <span class="title">
                <xsl:if test="$participant/@confirmed = 'true' and /page/links/link[@rel='rename']">
                    <xsl:attribute name="contenteditable">
                        <xsl:text>true</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="/page/bout/title" />
                    <xsl:with-param name="length" select="50" />
                </xsl:call-template>
            </span>
        </h1>
        <div id="top2">
            <xsl:apply-templates select="/page/bout/participants" />
            <xsl:if test="$participant/@confirmed = 'true' and not(/page/links/link[@rel='re-login'])">
                <xsl:call-template name="invite" />
                <xsl:call-template name="rename" />
            </xsl:if>
            <xsl:call-template name="options" />
        </div>
        <xsl:call-template name="stages" />
        <xsl:if test="$participant/@confirmed = 'true'">
            <form id="post" method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='post']/@href"/>
                </xsl:attribute>
                <p>
                    <textarea name="text" cols="80" rows="5">
                        <xsl:text>&#10;</xsl:text>
                    </textarea>
                </p>
                <p>
                    <input type="submit">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$TEXTS/Post.new.message"/>
                        </xsl:attribute>
                    </input>
                </p>
            </form>
        </xsl:if>
        <xsl:if test="/page/bout/view != ''">
            <ul class="periods">
                <li>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='top']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="$TEXTS/back.to.recent.messages"/>
                    </a>
                </li>
            </ul>
        </xsl:if>
        <xsl:apply-templates select="/page/bout/messages/message" />
        <xsl:if test="/page/bout/periods[count(link) &gt; 0]">
            <ul class="periods">
                <xsl:for-each select="/page/bout/periods/link">
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
    </xsl:template>

    <xsl:template match="message">
        <xsl:variable name="msg" select="."/>
        <div class="message">
            <xsl:attribute name="id">
                <xsl:text>msg</xsl:text>
                <xsl:value-of select="$msg/number"/>
            </xsl:attribute>
            <div class="left">
                <img class="photo">
                    <xsl:choose>
                        <xsl:when test="/page/bout/participants/participant[$msg/author=identity]">
                            <xsl:attribute name="src">
                                <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/photo"/>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/alias"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="src">
                                <xsl:text>http://cdn.netbout.com/someone.png?</xsl:text>
                                <xsl:value-of select="/page/version/revision"/>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:text>someone some time ago</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </img>
            </div>
            <div class="right">
                <div class="meta">
                    <b>
                        <xsl:choose>
                            <xsl:when test="$msg/author = /page/identity/name">
                                <xsl:value-of select="$TEXTS/you"/>
                            </xsl:when>
                            <xsl:when test="/page/bout/participants/participant[$msg/author=identity]">
                                <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/alias"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$TEXTS/someone"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </b>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$TEXTS/said"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="when"/>
                    <xsl:if test="@seen = 'false'">
                        <span class="red">
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="$TEXTS/new.message"/>
                        </span>
                    </xsl:if>
                </div>
                <div class="text">
                    <xsl:choose>
                        <xsl:when test="render/@namespace">
                            <p>
                                <span class="tt">
                                    <xsl:text>&lt;</xsl:text>
                                    <xsl:value-of select="render/@name"/>
                                    <xsl:text> xmlns="</xsl:text>
                                    <xsl:value-of select="render/@namespace"/>
                                    <xsl:text>"&gt;</xsl:text>
                                </span>
                                <xsl:text> </xsl:text>
                                <span class="xml-toggle" style="cursor: pointer;"
                                    title="click to see full content">
                                    <xsl:text>... </xsl:text>
                                    <xsl:variable name="length">
                                        <xsl:value-of select="string-length(render)"/>
                                    </xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="$length &gt; 5 * 1024">
                                            <xsl:value-of select="format-number($length div 1024, '0.#')"/>
                                            <xsl:text>Kb</xsl:text>
                                        </xsl:when>
                                        <xsl:when test="$length &gt; 1024 * 1024">
                                            <xsl:value-of select="format-number($length div (1024 * 1024), '0.##')"/>
                                            <xsl:text>Mb</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$length"/>
                                            <xsl:text> bytes</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:text> ...</xsl:text>
                                </span>
                                <xsl:text> </xsl:text>
                                <span class="tt">
                                    <xsl:text>&lt;/</xsl:text>
                                    <xsl:value-of select="render/@name"/>
                                    <xsl:text>&gt;</xsl:text>
                                </span>
                            </p>
                            <p class="fixed" style="display: none;">
                                <xsl:value-of select="render" disable-output-escaping="yes" />
                            </p>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="render" disable-output-escaping="yes" />
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="invite">
        <div id="invite-aside">
            <form method="get" id="invite">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                </xsl:attribute>
                <p>
                    <input name="mask" autocomplete="off">
                        <xsl:attribute name="placeholder">
                            <xsl:value-of select="$TEXTS/Invite"/>
                        </xsl:attribute>
                        <xsl:attribute name="value">
                            <xsl:value-of select="/page/mask"/>
                        </xsl:attribute>
                        <xsl:if test="/page/mask != ''">
                            <xsl:attribute name="autofocus">
                                <xsl:text>true</xsl:text>
                            </xsl:attribute>
                        </xsl:if>
                    </input>
                </p>
            </form>
            <ul id="invite-list">
                <xsl:if test="not(/page/invitees/invitee)">
                    <xsl:attribute name="style">
                        <xsl:text>display: none;</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <xsl:comment>to make this UL element non-empty</xsl:comment>
                <xsl:apply-templates select="/page/invitees/invitee" />
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="invitee">
        <li>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="@href"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:call-template name="format">
                        <xsl:with-param name="text" select="'click.to.invite.X.to.this.bout'" />
                        <xsl:with-param name="value" select="alias" />
                    </xsl:call-template>
                </xsl:attribute>
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="alias" />
                    <xsl:with-param name="length" select="25" />
                </xsl:call-template>
            </a>
            <img>
                <xsl:attribute name="src">
                    <xsl:value-of select="photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="alias"/>
                </xsl:attribute>
            </img>
        </li>
    </xsl:template>

    <xsl:template name="rename">
        <xsl:if test="/page/links/link[@rel='rename']">
            <form id="rename" method="post" style="display: none;">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='rename']/@href"/>
                </xsl:attribute>
                <input name="title" size="50" autocomplete="off">
                    <xsl:attribute name="value">
                        <xsl:value-of select="/page/bout/title"/>
                    </xsl:attribute>
                </input>
            </form>
        </xsl:if>
    </xsl:template>

    <xsl:template name="options">
        <div id="options">
            <span>
                <xsl:choose>
                    <xsl:when test="$participant/@confirmed = 'true'">
                        <xsl:text> </xsl:text>
                        <!--
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='leave']/@href"/>
                            </xsl:attribute>
                            <xsl:text>I want to leave this bout</xsl:text>
                        </a>
                        -->
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$TEXTS/Do.you.agree.to.join"/>
                        <xsl:text>: </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='join']/@href"/>
                            </xsl:attribute>
                            <xsl:value-of select="$TEXTS/yes"/>
                        </a>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="$TEXTS/or"/>
                        <xsl:text> </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='leave']/@href"/>
                            </xsl:attribute>
                            <xsl:value-of select="$TEXTS/no"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </span>
        </div>
    </xsl:template>

    <xsl:template name="stages">
        <xsl:if test="/page/bout/stage">
            <div id="stage">
                <xsl:apply-templates select="/page/bout/stage"/>
            </div>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
