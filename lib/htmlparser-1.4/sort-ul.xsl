<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:h="http://www.w3.org/1999/xhtml">

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="h:ul">
  <h:ul>
    <xsl:apply-templates>
      <xsl:sort/>
    </xsl:apply-templates>
  </h:ul>
</xsl:template>

</xsl:transform>
