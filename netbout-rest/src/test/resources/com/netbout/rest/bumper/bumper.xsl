<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs">

    <xsl:template match="stage" mode="head">
        <!-- nothing -->
    </xsl:template>

    <xsl:template match="stage">
        <form method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="$stage-home-uri"/>
            </xsl:attribute>
            <textarea name="data" cols="25" rows="4"></textarea>
            <input value="post it" type="submit"/>
        </form>
    </xsl:template>

</xsl:stylesheet>
