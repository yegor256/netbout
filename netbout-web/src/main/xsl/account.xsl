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
    <xsl:template match="page" mode="head">
        <title>
            <xsl:value-of select="$TEXTS/account"/>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <form method="post" action="{links/link[@rel='save-email']/@href}">
            <fieldset>
                <p>
                    <xsl:value-of select="$TEXTS/email.hello"/>
                </p>
                <p>
                    <input name="email" autocomplete="off"
                        value="{alias/email}" placeholder="{$TEXTS/your.email}"/>
                    <input type="submit" value="{$TEXTS/save.email}"/>
                </p>
            </fieldset>
        </form>
    </xsl:template>
</xsl:stylesheet>
