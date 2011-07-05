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
 * incident to the author by email: privacy@netbout.com.
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
    xmlns:nb="http://www.netbout.com"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:template match="/">
        <html>
            <head>
                <xsl:call-template name="head" />
                <link href="/css/screen.css" rel="stylesheet" type="text/css"></link>
            </head>
            <body>
                <div style="font-size: 6em; position: fixed; color: #ddd; line-height: 1em">
                    <!--
                    This text is for "production in development" stage only
                    -->
                    product<br/>
                    in development
                </div>
                <div id="header">
                    <div class="wrapper">
                        <table cellpadding="0" cellspacing="0">
                            <tr><td class="logo">
                                <a id="logo" href="/"><img src="/images/logo.png"/></a>
                                <ul>
                                    <li><a href="/john13"
                                        style="font-weight: bolder;">john13</a></li>
                                    <li><a href="/new">start</a></li>
                                    <li><a href="/out">log out</a></li>
                                </ul>
                            </td>
                            <td align="right">
                                <form action="" method="post">
                                    <dd><input type="text" value="PHP Germany" size="36" autocomplete="off" /></dd>
                                    <dd><input type="submit" value="?" /></dd>
                                </form>
                            </td></tr>
                        </table>
                    </div>
                </div>
                <div id="content">
                    <div class="wrapper">
                        <xsl:call-template name="content" />
                    </div>
                </div>
                <div id="footer">
                    <div class="wrapper">
                        <div style="position: absolute; right: 0; top: 0;">
                            USPTO patent app. no. 12/943,022
                        </div>
                        <ul id="menu">
                            <li><a href="">how it works</a></li>
                            <li><a href="">your privacy</a></li>
                            <li><a href="">feedback</a></li>
                        </ul>
                        <p>
                            (c) 2011, netBout.com,
                            rev.123,
                            0.3sec.
                            All Rights Reserved.
                        </p>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
