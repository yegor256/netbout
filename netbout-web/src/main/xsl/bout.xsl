<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2015, netbout.com
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
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes" />
    <xsl:param name="TEXTS"
        select="document(concat('/lang/', /page/alias/locale, '.xml?', /page/version/name))/texts"/>
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:include href="/xsl/friends.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="bout/number"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="bout/title"/>
        </title>
        <!-- @todo #606 The line below should be changed to avoid linking
             directly to the plugin author's website. Before doing that,
             we should add the functionality to download third party libraries
             from their website (like https://github.com/yuku-t/jquery-textcomplete/
             in this case) by Bower.
             Refer to #712 for suggestions on how to do it. -->
        <script src="http://yuku-t.com/jquery-textcomplete/media/javascripts/jquery.textcomplete.js">
            <xsl:text> </xsl:text>
        </script>
        <script src="/js/friends.js?{version/name}">
            <xsl:text> </xsl:text>
            <!-- this is for W3C compliance -->
        </script>
        <script src="/js/bout.js?{version/name}">
            <xsl:text> </xsl:text>
            <!-- this is for W3C compliance -->
        </script>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <xsl:apply-templates select="bout"/>
    </xsl:template>
    <xsl:template match="bout">
        <h1 class="bout">
            <a href="{/page/links/link[@rel='self']/@href}">
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
                    <xsl:with-param name="text" select="title"/>
                    <xsl:with-param name="length" select="50"/>
                </xsl:call-template>
            </span>
            <a href="{/page/links/link[@rel='subscribe']/@href}">
                <xsl:choose>
                    <xsl:when test="subscription='true'">
                        <i class="fa fa-bell-o"></i>
                    </xsl:when>
                    <xsl:otherwise>
                        <i class="fa fa-bell-slash-o"></i>
                    </xsl:otherwise>
                </xsl:choose>
            </a>
        </h1>
        <div class="top2">
            <xsl:apply-templates select="friends"/>
            <xsl:if test="not(/page/links/link[@rel='re-login'])">
                <xsl:call-template name="invite"/>
                <xsl:call-template name="rename"/>
            </xsl:if>
        </div>
        <xsl:apply-templates select="attachments"/>
        <xsl:apply-templates select="attachments/attachment[html]" mode="page"/>
        <div id="tabs" class="tabs previewable-content">
            <div class="tabnav">
                <nav class="tabnav-tabs">
                    <a id="write-link" href="#tab-post" class="active">Write</a>
                    <a id="preview-link" data-preview="{/page/links/link[@rel='preview']/@href}" href="#preview">Preview</a>
                </nav>
            </div>
            <form id="post-message" method="post" action="{/page/links/link[@rel='post']/@href}">
                <div id="tab-post" class="tab-panel active post">
                    <fieldset>
                        <label for="text">
                            <xsl:text> </xsl:text>
                        </label>
                        <textarea name="text" cols="80" rows="5" id="text">
                            <xsl:text> </xsl:text>
                        </textarea>
                    </fieldset>
                </div>
                <div id="tab-preview" class="tab-panel preview">
                    <div class="content">
                    </div>
                </div>
                <fieldset class="form-submit">
                    <label for="submit">
                        <xsl:text> </xsl:text>
                    </label>
                    <input type="submit" id="submit" value="{$TEXTS/Post.new.message}"/>
                </fieldset>
            </form>
        </div>
        <div id="messages" data-more="{messages/message[position()=last()]/links/link[@rel='more']/@href}">
            <xsl:apply-templates select="messages/message"/>
            <div id="tail"/>
        </div>
    </xsl:template>
    <xsl:template match="messages/message">
        <xsl:variable name="msg" select="."/>
        <div class="message" id="msg{$msg/number}">
            <div class="left">
                <img class="photo" src="{links/link[@rel='photo']/@href}"/>
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
                    <a href="{/page/links/link[@rel='self']/@href}#msg{$msg/number}">
                        <span title="{date}">
                            <xsl:value-of select="timeago"/>
                        </span>
                    </a>
                </div>
                <div class="text">
                    <xsl:value-of select="html" disable-output-escaping="yes"/>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="invite">
        <div class="invite">
            <form method="post" id="invite" action="{/page/links/link[@rel='invite']/@href}">
                <fieldset>
                    <input name="name" autocomplete="off" placeholder="{$TEXTS/Invite}"/>
                </fieldset>
            </form>
        </div>
    </xsl:template>
    <xsl:template name="rename">
        <xsl:if test="/page/links/link[@rel='rename']">
            <form id="rename" method="post" style="display: none;" action="{/page/links/link[@rel='rename']/@href}">
                <input name="title" size="50" autocomplete="off" value="{/page/bout/title}"/>
            </form>
        </xsl:if>
    </xsl:template>
    <xsl:template match="attachments">
        <nav class="attachments">
            <xsl:variable name="files" select="attachment[not(links/link[@rel='open'])]"/>
            <ul>
                <xsl:apply-templates select="attachment[links/link[@rel='open']]"/>
                <li onclick="$('#files').toggle();" class="toggle">
                    <xsl:variable name="count" select="count($files)"/>
                    <xsl:text>+</xsl:text>
                    <xsl:value-of select="$count"/>
                    <xsl:text> file</xsl:text>
                    <xsl:if test="$count != 1">
                        <xsl:text>s</xsl:text>
                    </xsl:if>
                </li>
                <xsl:text> </xsl:text>
            </ul>
            <div id="files" style="display:none">
                <ul>
                    <xsl:for-each select="$files">
                        <li>
                            <a href="{links/link[@rel='download']/@href}">
                                <xsl:value-of select="name"/>
                            </a>
                            <xsl:text> (</xsl:text>
                            <xsl:value-of select="ctype"/>
                            <xsl:text>)</xsl:text>
                        </li>
                    </xsl:for-each>
                </ul>
                <form method="post" id="upload" action="{/page/links/link[@rel='attach']/@href}"
                    enctype="multipart/form-data">
                    <fieldset>
                        <input id="file-name" name="name" autocomplete="off" placeholder="attachment name..." size="30" maxlength="50"/>
                        <input id="file-binary" name="file" type="file"/>
                        <label for="file-submit"/>
                        <input id="file-submit" type="submit" value="Upload"/>
                    </fieldset>
                </form>
            </div>
        </nav>
    </xsl:template>
    <xsl:template match="attachment">
        <li>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="html">
                        <xsl:text>active</xsl:text>
                    </xsl:when>
                    <xsl:when test="unseen = 'true'">
                        <xsl:text>unseen</xsl:text>
                    </xsl:when>
                </xsl:choose>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="links/link[@rel='open']">
                    <a href="{links/link[@rel='open']/@href}">
                        <xsl:value-of select="name"/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="name"/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>
    <xsl:template match="attachment" mode="page">
        <div class="page">
            <div class="controls">
                <xsl:if test="links/link[@rel='download']">
                    <a title="download {name} ({ctype})" href="{links/link[@rel='download']/@href}">
                        <i class="ico ico-download">
                            <xsl:comment>download</xsl:comment>
                        </i>
                    </a>
                </xsl:if>
            </div>
            <xsl:value-of select="html" disable-output-escaping="yes"/>
        </div>
    </xsl:template>
</xsl:stylesheet>
