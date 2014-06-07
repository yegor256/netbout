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
    <xsl:param name="TEXTS"
        select="document(concat('/lang/', /page/alias/locale, '.xml?', /page/version/revision))/texts"/>
    <xsl:include href="/xsl/layout.xsl" />
    <xsl:include href="/xsl/friends.xsl" />
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="bout/number"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="bout/title"/>
        </title>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/friends.js?</xsl:text>
                <xsl:value-of select="version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
        <script>
            <xsl:attribute name="src">
                <xsl:text>/js/bout.js?</xsl:text>
                <xsl:value-of select="version/revision"/>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- this is for W3C compliance -->
        </script>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <xsl:apply-templates select="bout" />
    </xsl:template>
    <xsl:template match="bout">
        <h1 class="bout">
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='self']/@href"/>
                </xsl:attribute>
                <span class="num">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="number"/>
                </span>
            </a>
            <span class="title">
                <xsl:if test="/page/links/link[@rel='rename']">
                    <xsl:attribute name="contenteditable">
                        <xsl:text>true</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="title" />
                    <xsl:with-param name="length" select="50" />
                </xsl:call-template>
            </span>
        </h1>
        <div class="top2">
            <xsl:apply-templates select="friends" />
            <xsl:if test="not(/page/links/link[@rel='re-login'])">
                <xsl:call-template name="invite" />
                <xsl:call-template name="rename" />
            </xsl:if>
        </div>
        <xsl:apply-templates select="attachments"/>
        <xsl:apply-templates select="attachments/attachment[html]" mode="page"/>
        <div class="post">
            <form method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='post']/@href"/>
                </xsl:attribute>
                <fieldset>
                    <label for="text">
                        <xsl:text> </xsl:text>
                    </label>
                    <textarea name="text" cols="80" rows="5" id="text">
                        <xsl:text> </xsl:text>
                    </textarea>
                    <label for="submit">
                        <xsl:text> </xsl:text>
                    </label>
                    <input type="submit" id="submit">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$TEXTS/Post.new.message"/>
                        </xsl:attribute>
                    </input>
                </fieldset>
            </form>
        </div>
        <xsl:apply-templates select="messages/message" />
    </xsl:template>
    <xsl:template match="messages/message">
        <xsl:variable name="msg" select="."/>
        <div class="message">
            <xsl:attribute name="id">
                <xsl:text>msg</xsl:text>
                <xsl:value-of select="$msg/number"/>
            </xsl:attribute>
            <div class="left">
                <img class="photo">
                    <xsl:choose>
                        <xsl:when test="/page/bout/friends/friend[$msg/author=alias]">
                            <xsl:attribute name="src">
                                <xsl:value-of select="/page/bout/friends/friend[$msg/author=alias]/photo"/>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:value-of select="/page/bout/friends/friend[$msg/author=alias]/alias"/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="src">
                                <xsl:call-template name="cdn">
                                    <xsl:with-param name="name">
                                        <xsl:text>someone.png</xsl:text>
                                    </xsl:with-param>
                                </xsl:call-template>
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
                    <xsl:if test="position() &lt;= /page/bout/unread">
                        <xsl:attribute name="class">
                            <xsl:text>red</xsl:text>
                        </xsl:attribute>
                    </xsl:if>
                    <strong>
                        <xsl:choose>
                            <xsl:when test="$msg/author = /page/alias/name">
                                <xsl:value-of select="$TEXTS/you"/>
                            </xsl:when>
                            <xsl:when test="/page/bout/friends/friend[$msg/author=alias]">
                                <xsl:value-of select="/page/bout/friends/friend[$msg/author=alias]/alias"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$TEXTS/someone"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </strong>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$TEXTS/said"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="timeago"/>
                </div>
                <div class="text">
                    <xsl:value-of select="html" disable-output-escaping="yes" />
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="invite">
        <div class="invite">
            <form method="post" id="invite">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='invite']/@href"/>
                </xsl:attribute>
                <fieldset>
                    <input name="name" autocomplete="off">
                        <xsl:attribute name="placeholder">
                            <xsl:value-of select="$TEXTS/Invite"/>
                        </xsl:attribute>
                    </input>
                </fieldset>
            </form>
        </div>
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
    <xsl:template match="attachments">
        <nav class="attachments">
            <ul>
                <xsl:apply-templates select="attachment" />
            </ul>
        </nav>
    </xsl:template>
    <xsl:template match="attachment">
        <li>
            <xsl:attribute name="class">
                <xsl:if test="html">
                    <xsl:text>active</xsl:text>
                </xsl:if>
                <xsl:if test="unseen = 'TRUE'">
                    <xsl:text> unseen</xsl:text>
                </xsl:if>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="links/link[@rel='open']">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='open']/@href"/>
                        </xsl:attribute>
                        <xsl:value-of select="name" />
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="name" />
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>
    <xsl:template match="attachment" mode="page">
        <div class="page">
            <xsl:value-of select="html" disable-output-escaping="yes" />
        </div>
    </xsl:template>
</xsl:stylesheet>
