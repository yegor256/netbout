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

    <xsl:variable name="participant"
        select="/page/bout/participants/participant[identity=/page/identity/name]"/>

    <xsl:template name="head">
        <title>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="/page/bout/number"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="/page/bout/title"/>
        </title>
        <script src="/js/dudes.js">
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <script src="/js/bout.js">
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <link href="/css/bout.css" rel="stylesheet" type="text/css"/>
        <link href="/css/dudes.css" rel="stylesheet" type="text/css"/>
        <link href="/css/periods.css" rel="stylesheet" type="text/css"/>
        <xsl:if test="/page/bout/stage">
            <xsl:apply-templates select="/page/bout/stage" mode="head" />
        </xsl:if>
    </xsl:template>

    <xsl:template name="content">
        <header id="top1">
            <h1>
                <span class="num">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="/page/bout/number"/>
                </span>
                <span class="title">
                    <xsl:if test="$participant/@confirmed = 'true'">
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
        </header>
        <header id="top2">
            <xsl:apply-templates select="/page/bout/participants" />
            <xsl:if test="$participant/@confirmed = 'true' and not(/page/links/link[@rel='re-login'])">
                <xsl:call-template name="invite" />
                <xsl:call-template name="rename" />
            </xsl:if>
            <xsl:call-template name="options" />
        </header>
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
                    <input value="Post new message" type="submit" />
                </p>
            </form>
        </xsl:if>
        <xsl:if test="/page/bout/view != ''">
            <ul class="periods">
                <li>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                        </xsl:attribute>
                        <xsl:text>back to recent messages</xsl:text>
                    </a>
                </li>
            </ul>
        </xsl:if>
        <xsl:apply-templates select="/page/bout/messages/message" />
        <xsl:if test="/page/bout/periods[count(link) &gt; 0]">
            <nav>
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
            </nav>
        </xsl:if>
    </xsl:template>

    <xsl:template match="message">
        <xsl:variable name="msg" select="."/>
        <article class="message">
            <xsl:attribute name="id">
                <xsl:text>msg</xsl:text>
                <xsl:value-of select="$msg/number"/>
            </xsl:attribute>
            <aside class="left">
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
                                <xsl:text>http://img.netbout.com/someone.png</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:text>someone some time ago</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </img>
            </aside>
            <div class="right">
                <header class="meta">
                    <b>
                        <xsl:choose>
                            <xsl:when test="$msg/author = /page/identity/name">
                                <xsl:text>you</xsl:text>
                            </xsl:when>
                            <xsl:when test="/page/bout/participants/participant[$msg/author=identity]">
                                <xsl:value-of select="/page/bout/participants/participant[$msg/author=identity]/alias"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>someone</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </b>
                    <xsl:text> said </xsl:text>
                    <xsl:value-of select="when"/>
                    <xsl:if test="@seen = 'false'">
                        <span class="red">
                            <xsl:text> new</xsl:text>
                        </span>
                    </xsl:if>
                </header>
                <p class="text">
                    <xsl:value-of select="render" disable-output-escaping="yes" />
                </p>
            </div>
        </article>
    </xsl:template>

    <xsl:template name="invite">
        <aside id="invite-aside">
            <form method="get" id="invite">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                </xsl:attribute>
                <p>
                    <input name="mask" autocomplete="off" placeholder="Invite...">
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
            <xsl:if test="/page/invitees[count(invitee) &gt; 0]">
                <ul id="invite-list">
                    <xsl:for-each select="/page/invitees/invitee">
                        <li>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="@href"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:text>click to invite "</xsl:text>
                                    <xsl:value-of select="alias"/>
                                    <xsl:text>" to this bout</xsl:text>
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
                    </xsl:for-each>
                </ul>
            </xsl:if>
        </aside>
    </xsl:template>

    <xsl:template name="rename">
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
    </xsl:template>

    <xsl:template name="options">
        <aside id="options">
            <span>
                <xsl:choose>
                    <xsl:when test="$participant/@confirmed = 'true'">
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
                        <xsl:text>Do you agree to join this bout: </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='join']/@href"/>
                            </xsl:attribute>
                            <xsl:text>yes, of course</xsl:text>
                        </a>
                        <xsl:text> or </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='leave']/@href"/>
                            </xsl:attribute>
                            <xsl:text>no, I refuse</xsl:text>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </span>
        </aside>
    </xsl:template>

    <xsl:template name="stages">
        <xsl:if test="/page/bout/stage">
            <section id="stage">
                <xsl:apply-templates select="/page/bout/stage"/>
            </section>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
