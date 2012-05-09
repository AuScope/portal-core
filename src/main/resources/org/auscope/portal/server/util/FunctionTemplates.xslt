<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ogc="http://www.opengis.net/ogc">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<!-- ================================================================================================= -->
	<!-- COMMUNITY SCHEMA FUNCTION TEMPLATES ============================================================= -->
	<!-- ================================================================================================= -->
	<!-- -->
	<!-- ================================================================================================= -->
	<!-- codeSpaceFilter FUNCTION ======================================================================== -->
	<!-- ================================================================================================= -->
	<!-- Splits scoped name elements in community schema (such as GeoSciML) Filters into two elements for
         'private' Deegree schema:
            1) the main element; and
            2) the private schema _codespace element.-->
    <!-- Necessary because Deegree does not allow attributes to be defined and populated in its schema.-->
    <!-- The function assumes scoped name values have no child nodes, so it does not search for any
         predicated child nodes, also, any predicated parent nodes are not dealt with, in fact, they will
         probably not be mapped to the private schema by the PropertyName mapping templates. -->
    <!-- **IMPORTANT** Codepsace elements must be defined using this convention:
                      elementname_codeSpace-->
	<!-- Call using: <xsl:call-template name="codeSpaceFilter">
						<xsl:with-param name="propertyName" select="-Property Name-"/>
						<xsl:with-param name="operator" select="-Operator Tag-"/>
					 </xsl:call-template>-->
	<xsl:template name="filterCodeSpaceCheck">
		<xsl:param name="propertyName"/>
		<xsl:param name="operator"/>
		<!--Variable to escape apostophes-->
		<xsl:variable name="apos" select='"&apos;"'/>
		<xsl:choose>
			<xsl:when test="contains($propertyName,'@codeSpace')">
				<!--Working string 1: get everything after @codeSpace= from $propertyName-->
				<xsl:variable name="working1">
					<xsl:value-of select="substring-after($propertyName,'@codeSpace=')"/>
				</xsl:variable>
				<!--Working string 2: get everything before the ] closing off the attribute value-->
				<xsl:variable name="working2">
					<xsl:value-of select="substring-before($working1,']')"/>
				</xsl:variable>
				<!--Unpredicated PropertyName-->
				<xsl:variable name="propertyName2">
					<xsl:value-of select="substring-before($propertyName,'[@codeSpace')"/>
				</xsl:variable>
				<ogc:And>
					<xsl:choose>
						<xsl:when test="$operator='ogc:PropertyIsEqualTo'">
							<ogc:PropertyIsEqualTo>
								<ogc:PropertyName><xsl:value-of select="$propertyName2"/></ogc:PropertyName>
								<xsl:copy-of select="ogc:Literal"/>
							</ogc:PropertyIsEqualTo>
						</xsl:when>
						<xsl:when test="$operator='ogc:PropertyIsNotEqualTo'">
							<ogc:PropertyIsNotEqualTo>
								<ogc:PropertyName><xsl:value-of select="$propertyName2"/></ogc:PropertyName>
								<xsl:copy-of select="ogc:Literal"/>
							</ogc:PropertyIsNotEqualTo>
						</xsl:when>
						<xsl:when test="$operator='ogc:PropertyIsLike'">
							<ogc:PropertyIsLike>
								<ogc:PropertyName><xsl:value-of select="$propertyName2"/></ogc:PropertyName>
								<xsl:copy-of select="ogc:Literal"/>
							</ogc:PropertyIsLike>
						</xsl:when>
					</xsl:choose>
					<ogc:PropertyIsEqualTo>
						<ogc:PropertyName><xsl:value-of select="$propertyName2"/>_codeSpace</ogc:PropertyName>
						<ogc:Literal><xsl:value-of select="translate($working2,$apos,'')"/></ogc:Literal>
					</ogc:PropertyIsEqualTo>
				</ogc:And>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$operator='ogc:PropertyIsEqualTo'">
						<ogc:PropertyIsEqualTo>
							<ogc:PropertyName><xsl:value-of select="$propertyName"/></ogc:PropertyName>
							<xsl:copy-of select="ogc:Literal"/>
						</ogc:PropertyIsEqualTo>
					</xsl:when>
					<xsl:when test="$operator='ogc:PropertyIsNotEqualTo'">
						<ogc:PropertyIsNotEqualTo>
							<ogc:PropertyName><xsl:value-of select="$propertyName"/></ogc:PropertyName>
							<xsl:copy-of select="ogc:Literal"/>
						</ogc:PropertyIsNotEqualTo>
					</xsl:when>
					<xsl:when test="$operator='ogc:PropertyIsLike'">
						<ogc:PropertyIsLike>
							<ogc:PropertyName><xsl:value-of select="$propertyName"/></ogc:PropertyName>
							<xsl:copy-of select="ogc:Literal"/>
						</ogc:PropertyIsLike>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>
	<!-- ================================================================================================= -->
	<!-- leadingReplace FUNCTION ========================================================================== -->
	<!-- ================================================================================================= -->
	<!-- leadingReplace template to removing a leading character or string from a string. -->
	<!-- Call using: <xsl:call-template name="leadingReplace">
						<xsl:with-param name="outputString" select="."/>
						<xsl:with-param name="target" select="'XXXX'"/>
					</xsl:call-template>-->
	<xsl:template name="leadingReplace">
		<xsl:param name="outputString"/>
		<xsl:param name="target"/>
		<xsl:choose>
			<xsl:when test="starts-with($outputString,$target)">
				<xsl:variable name="outputString2">
					<xsl:value-of select="substring-after($outputString,$target)"/>
				</xsl:variable>
				<xsl:variable name="outputString3">
					<xsl:call-template name="leadingReplace">
						<xsl:with-param name="outputString" select="$outputString2"/>
						<xsl:with-param name="target" select="$target"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$outputString3"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$outputString"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================================================= -->
	<!-- globalReplace FUNCTION ========================================================================== -->
	<!-- ================================================================================================= -->
	<!-- globalReplace template to handle the fact that XSLT 1.0 (used by Deegree) has no REPLACE function.-->
	<!-- Call using: <xsl:call-template name="globalReplace">
						<xsl:with-param name="outputString" select="."/>
						<xsl:with-param name="target" select="'XXXX'"/>
						<xsl:with-param name="replacement" select="'YYYY'"/>
					</xsl:call-template>-->
	<xsl:template name="globalReplace">
		<xsl:param name="outputString"/>
		<xsl:param name="target"/>
		<xsl:param name="replacement"/>
		<xsl:choose>
			<xsl:when test="contains($outputString,$target)">
				<xsl:value-of select="concat(substring-before($outputString,$target),
               $replacement)"/>
				<xsl:call-template name="globalReplace">
					<xsl:with-param name="outputString" select="substring-after($outputString,$target)"/>
					<xsl:with-param name="target" select="$target"/>
					<xsl:with-param name="replacement" select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$outputString"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>