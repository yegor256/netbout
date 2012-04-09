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
        select="document(concat('/xml/lang/', /page/identity/locale, '.xml?', /page/version/revision))/texts"/>

    <xsl:include href="/xsl/layout.xsl" />

    <xsl:template name="head">
        <title>
            <xsl:value-of select="$TEXTS/profile"/>
        </title>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:text>/css/profile.css?</xsl:text>
                <xsl:value-of select="/page/version/revision"/>
            </xsl:attribute>
        </link>
    </xsl:template>

    <xsl:template name="content">
        <div>
            <h1>
                <span class="title"><xsl:value-of select="$TEXTS/Profile.settings"/></span>
            </h1>
        </div>
        <p>
            <img class="photo">
                <xsl:attribute name="src">
                    <xsl:value-of select="/page/identity/photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="/page/identity/alias"/>
                </xsl:attribute>
            </img>
        </p>
        <p>
            <xsl:value-of select="$TEXTS/Identity"/>
            <xsl:text>: </xsl:text>
            <span class="tt"><xsl:value-of select="/page/identity/name"/></span>
        </p>
        <xsl:if test="/page/identity/aliases[count(alias) &gt; 1]">
            <p>
                <xsl:value-of select="$TEXTS/AKA"/>
                <xsl:text>: </xsl:text>
                <xsl:for-each select="/page/identity/aliases/alias">
                    <xsl:if test="position() &gt; 1">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="/page/identity/alias = .">
                            <b><xsl:value-of select="."/></b>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </p>
        </xsl:if>
        <p>
            <xsl:value-of select="$TEXTS/Language"/>
            <xsl:text>: </xsl:text>
            <img class="flag">
                <xsl:attribute name="src">
                    <xsl:text>http://cdn.netbout.com/lang/</xsl:text>
                    <xsl:value-of select="/page/identity/locale"/>
                    <xsl:text>.png?</xsl:text>
                    <xsl:value-of select="/page/version/revision"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="/page/identity/locale"/>
                </xsl:attribute>
            </img>
            <xsl:text> </xsl:text>
            <xsl:value-of select="$TEXTS/switch.to"/>
            <xsl:text>: </xsl:text>
            <xsl:for-each select="/page/profile/locales/link">
                <xsl:if test="code != /page/identity/locale">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="@href"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="name" />
                        </xsl:attribute>
                        <img class="flag">
                            <xsl:attribute name="src">
                                <xsl:text>http://cdn.netbout.com/lang/</xsl:text>
                                <xsl:value-of select="code"/>
                                <xsl:text>.png?</xsl:text>
                                <xsl:value-of select="/page/version/revision"/>
                            </xsl:attribute>
                            <xsl:attribute name="alt">
                                <xsl:value-of select="name"/>
                            </xsl:attribute>
                        </img>
                    </a>
                    <xsl:text> </xsl:text>
                </xsl:if>
            </xsl:for-each>
        </p>
        <xsl:call-template name="helper"/>
    </xsl:template>

    <xsl:template name="helper">
        <xsl:if test="/page/identity/@helper">
            <p>
                <xsl:text>You're a helper already with this URL: </xsl:text>
                <span class="tt"><xsl:value-of select="/page/identity/location"/></span>
                <xsl:text>.</xsl:text>
            </p>
            <p>
                <xsl:value-of select="count(/page/identity/supports/operation)"/>
                <xsl:text> operation(s) are supported: </xsl:text>
                <xsl:for-each select="/page/identity/supports/operation">
                    <xsl:if test="position() &gt; 1">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <span class="tt"><xsl:value-of select="."/></span>
                </xsl:for-each>
                <xsl:text>.</xsl:text>
            </p>
        </xsl:if>
        <xsl:if test="/page/identity[starts-with(name, 'urn:netbout:')]
            or /page/identity[starts-with(name, 'urn:woquo:')]
            or /page/identity[starts-with(name, 'urn:test:')]
            or /page/identity[name='urn:facebook:1531296526']">
            <xsl:if test="/page/identity[not(@helper)]">
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
                        <label for="url">URL:</label>
                        <input name="url" type="url" size="50" autocomplete="off" id="url">
                            <xsl:attribute name="value">
                                <xsl:value-of select="/page/identity/location"/>
                            </xsl:attribute>
                        </input>
                        <label for="promote"><xsl:text> </xsl:text></label>
                        <input value="Promote" type="submit" id="promote"/>
                    </p>
                </form>
            </xsl:if>
            <form method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="/page/links/link[@rel='namespaces']/@href"/>
                </xsl:attribute>
                <p>
                    <label for="text">
                        <xsl:text>Namespaces registered for you (</xsl:text>
                        <span class="tt"><xsl:text>&lt;namespace&gt; "=" &lt;URL template&gt;</xsl:text></span>
                        <xsl:text> per line):</xsl:text>
                    </label>
                    <textarea name="text" style="width: 50em; height: 6em;" id="text">
                        <xsl:for-each select="/page/namespaces/namespace">
                            <xsl:value-of select="name"/>
                            <xsl:text>=</xsl:text>
                            <xsl:value-of select="template"/>
                            <xsl:text>&#x0d;</xsl:text>
                        </xsl:for-each>
                        <xsl:text>&#x0d;</xsl:text>
                    </textarea>
                    <label for="register"><xsl:text> </xsl:text></label>
                    <input value="Register" type="submit" id="register"/>
                </p>
            </form>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
