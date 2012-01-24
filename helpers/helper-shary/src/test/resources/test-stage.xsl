<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:variable name="stage-home-uri">
        <xsl:text>http://example.com/</xsl:text>
    </xsl:variable>

    <xsl:include href="../../../src/main/resources/com/netbout/shary/stage.xsl" />

    <xsl:output method="xhtml"/>

    <xsl:template match="/">
        <html style="padding: 20px;">
            <head>
                <link href="http://www.netbout.com/css/global.css" rel="stylesheet" type="text/css"/>
                <link href="http://www.netbout.com/css/layout.css" rel="stylesheet" type="text/css"/>
                <link href="http://www.netbout.com/css/bout.css" rel="stylesheet" type="text/css"/>
            </head>
            <body>
                <section id="content">
                    <div id="stage">
                        <xsl:apply-templates select="/page/stage"/>
                    </div>
                </section>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
