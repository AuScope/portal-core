<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns="http://www.opengis.net/kml/2.2" xmlns:er="urn:cgi:xmlns:GGIC:EarthResource:1.1" xmlns:geodesy="http://www.auscope.org/geodesy" xmlns:gml="http://www.opengis.net/gml"
	xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0" xmlns:ngcp="http://www.auscope.org/ngcp" xmlns:sa="http://www.opengis.net/sampling/1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="er geodesy gml gsml ngcp sa wfs xsl xlink"
>

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" cdata-section-elements="description Snippet text" media-type="application/vnd.google-earth.kml+xml" />

	<xsl:param name="serviceURL" />
	<xsl:variable name="mineServiceString"><![CDATA[service=WFS&version=1.1.0&request=GetFeature&typename=er:Mine&featureid=]]></xsl:variable>


	<!-- MATCH ROOT FEATURECOLLECTION -->
	<!-- ================================================================= -->
	<xsl:template match="wfs:FeatureCollection">

		<!--<kml xmlns="http://www.opengis.net/kml/2.2"> -->
		<kml>
			<Document>
				<!-- STANDARD NAME AND DESCRIPTION FOR CONVERTED FILE -->
				<name>
					<xsl:text>GML Links to KML</xsl:text>
				</name>
				<description>
					<xsl:text>GeoSciML data converted to KML</xsl:text>
				</description>
				<xsl:apply-templates select="gml:featureMembers/* | gml:featureMember/*" />
			</Document>
		</kml>
	</xsl:template>

	<xsl:template match="gml:featureMember/er:MiningFeatureOccurrence | gml:featureMembers/er:MiningFeatureOccurrence">
		<xsl:variable name="coordinates">
			<xsl:value-of select="./er:location/gml:Point/gml:pos" />
		</xsl:variable>

		<xsl:variable name="mineName">
			<xsl:value-of select="./er:specification/er:Mine/er:mineName/er:MineName[./er:isPreferred = true()]/er:mineName/text()" />
		</xsl:variable>

		<xsl:variable name="mineNameHrefLink">
			<xsl:call-template name="createHrefLink">
				<xsl:with-param name="thisGmlName" select="./er:specification/er:Mine/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']" />
				<xsl:with-param name="specification" select="./er:specification/er:Mine/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']" />
				<xsl:with-param name="candidate1" select="''" />
				<xsl:with-param name="candidate2">
					<xsl:value-of select="$serviceURL" /><![CDATA[service=WFS&version=1.1.0&request=GetFeature&typename=er:Mine&featureid=]]><xsl:value-of select="./er:specification./er:Mine/@gml:id" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="$coordinates">
			<Placemark>
				<name>
					<xsl:value-of select="$mineName" />
				</name>
				<description>
					<xsl:call-template name="start-table"></xsl:call-template>
                    <![CDATA[<tr><td>Name</td><td>]]><xsl:call-template name="make-wfspopup-url">
						<xsl:with-param name="friendly-name" select="./er:specification/er:Mine/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']" />
						<xsl:with-param name="real-url" select="$mineNameHrefLink" />
					</xsl:call-template><![CDATA[</td></tr>]]>
					<![CDATA[<tr><td>Preferred Name</td><td>]]><xsl:value-of select="$mineName" /><![CDATA[</td></tr>]]>
					<![CDATA[<tr><td>Location</td><td>]]><xsl:value-of select="$coordinates" /><![CDATA[</td></tr>]]>
					<![CDATA[<tr><td>Status</td><td>]]><xsl:value-of select="./er:specification/er:Mine/er:status" /><![CDATA[</td></tr>]]>
					<![CDATA[</table></div>]]></description>

				<xsl:apply-templates select="./descendant::gml:Point" />
			</Placemark>
		</xsl:if>
	</xsl:template>

	<!-- ================================================================= -->
	<xsl:template match="gsml:shape">

		<xsl:apply-templates select="gml:Point" />
		<xsl:apply-templates select="gml:Polygon" />
		<xsl:apply-templates select="gml:MultiCurve" />
		<xsl:apply-templates select="gml:MultiSurface//gml:PolygonPatch" />
	</xsl:template>


	<!-- ================================================================= -->
	<xsl:template match="gml:Point">

		<xsl:variable name="coordinates">
			<xsl:value-of select="./gml:pos" />
		</xsl:variable>

		<Point>
			<Style>
				<IconStyle>
					<Icon>
						<href>http://maps.google.com/mapfiles/kml/paddle/ylw-blank.png
						</href>
					</Icon>
				</IconStyle>
			</Style>

			<coordinates>
				<xsl:choose>
					<xsl:when test="starts-with(@srsName,'http://www.opengis.net/gml/srs/epsg.xml#4283')">
						<xsl:call-template name="parseLatLongCoord">
							<xsl:with-param name="coordinates" select="$coordinates" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="starts-with(@srsName,'EPSG')">
						<xsl:call-template name="parseLongLatCoord">
							<xsl:with-param name="coordinates" select="$coordinates" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="starts-with(@srsName,'http://www.opengis.net/gml/srs/epsg.xml')">
						<xsl:call-template name="parseLongLatCoord">
							<xsl:with-param name="coordinates" select="$coordinates" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="starts-with(@srsName,'urn:x-ogc:def:crs:EPSG')">
						<xsl:call-template name="parseLatLongCoord">
							<xsl:with-param name="coordinates" select="$coordinates" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="parseLongLatCoord">
							<xsl:with-param name="coordinates" select="$coordinates" />
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</coordinates>
		</Point>
	</xsl:template>


	<!-- ================================================================= -->
	<xsl:template match="gml:Polygon">
		<Polygon>
			<altitudeMode>
				<xsl:text>clampToGround</xsl:text>
			</altitudeMode>
			<xsl:apply-templates select="gml:exterior" />
			<xsl:apply-templates select="gml:interior" />
		</Polygon>
	</xsl:template>


	<!-- ================================================================= -->
	<xsl:template match="gml:MultiCurve">

		<xsl:variable name="coordinates">
			<xsl:value-of select="./gml:curveMembers/gml:Curve/gml:segments/gml:LineStringSegment/gml:posList | ./gml:curveMember/gml:LineString/gml:posList" />
		</xsl:variable>

		<Style>
			<LineStyle>
				<color>ff004080</color>
			</LineStyle>
		</Style>

		<LineString>
			<coordinates>
				<xsl:call-template name="parseLatLongCoord">
					<xsl:with-param name="coordinates" select="$coordinates" />
				</xsl:call-template>
			</coordinates>
		</LineString>
	</xsl:template>

	<xsl:template match="gml:MultiLineString">
		<xsl:variable name="int_coordinates">
			<xsl:value-of select="./gml:lineStringMember/gml:LineString/gml:posList" />
		</xsl:variable>

		<LineString>
			<extrude>
				<xsl:text>1</xsl:text>
			</extrude>
			<tessellate>
				<xsl:text>1</xsl:text>
			</tessellate>
			<coordinates>
				<xsl:call-template name="parseLatLongCoord">
					<xsl:with-param name="coordinates" select="$int_coordinates" />
				</xsl:call-template>
			</coordinates>

		</LineString>
	</xsl:template>

	<!-- ================================================================= -->
	<xsl:template match="gml:MultiSurface//gml:PolygonPatch">

		<Style>
			<LineStyle>
				<color>ff0000ff</color>
			</LineStyle>
		</Style>

		<Polygon>
			<xsl:apply-templates select="gml:exterior" />
			<xsl:apply-templates select="gml:interior" />
		</Polygon>
	</xsl:template>


	<!-- TEMPLATE FOR Polygon : exterior -->
	<!-- ================================================================= -->
	<xsl:template match="gml:exterior">

		<xsl:variable name="ext_coordinates">
			<xsl:value-of select="./gml:LinearRing/gml:posList" />
		</xsl:variable>

		<outerBoundaryIs>
			<LinearRing>
				<coordinates>
					<xsl:call-template name="parseLatLongCoord">
						<xsl:with-param name="coordinates" select="$ext_coordinates" />
					</xsl:call-template>
				</coordinates>
			</LinearRing>
		</outerBoundaryIs>

	</xsl:template>


	<!-- TEMPLATE FOR Polygon : interior -->
	<!-- ================================================================= -->
	<xsl:template match="gml:interior">

		<xsl:variable name="int_coordinates">
			<xsl:value-of select="./gml:LinearRing/gml:posList" />
		</xsl:variable>

		<innerBoundaryIs>
			<LinearRing>
				<coordinates>
					<xsl:call-template name="parseLatLongCoord">
						<xsl:with-param name="coordinates" select="$int_coordinates" />
					</xsl:call-template>
				</coordinates>
			</LinearRing>
		</innerBoundaryIs>
	</xsl:template>


	<!-- ================================================================= -->
	<!-- FUNCTION TO DISPLAY URN RESOLVER LINK WITHIN HTML TABLE ROW -->
	<!-- PARAM: tableRowLabel -->
	<!-- PARAM: tableRowValue -->
	<!-- ================================================================= -->
	<xsl:template name="displayUrnResolverLink">
		<xsl:param name="tableRowLabel" />
		<xsl:param name="tableRowValue" />
		<!-- <![CDATA[</td></tr><tr><td>]]><xsl:value-of select="$tableRowLabel"/><![CDATA[</td><td><a href="#" onclick="var w=window.open(']]><xsl:value-of select="$uriResolverURL"/><xsl:value-of select="$tableRowValue"/><![CDATA[','AboutWin','toolbar=no,
			menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=800');w.focus();return false;">]]><xsl:value-of select="$tableRowValue"/><![CDATA[</a>]]> -->
		<![CDATA[</td></tr><tr><td>]]><xsl:value-of select="$tableRowLabel" /><![CDATA[</td><td><a href="#" onclick="var w=window.open(']]><xsl:value-of select="$tableRowValue" /><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;">]]><xsl:value-of
		select="$tableRowValue" /><![CDATA[</a>]]>
	</xsl:template>


	<!-- ================================================================= -->
	<!-- THIS FUNCTION TAKES HTTP LINK TO URN RESOLVER AND CONSTRUCTS -->
	<!-- HREF HTML LINK TO DISPLAY CONTENT IN A NEW WINDOW -->
	<!-- -->
	<!-- IT STRIPS THE 'http://...=' PREFIX FROM THE tableRowValue -->
	<!-- PARAMETER AND DISPLAYS JUST THE URN PART TO THE USER -->
	<!-- -->
	<!-- PARAM: tableRowLabel -->
	<!-- PARAM: tableRowValue - HTTP RESOLVER LINK -->
	<!-- ================================================================= -->
	<xsl:template name="displayUrnResolverLinkWithoutHTTP">
		<xsl:param name="tableRowLabel" />
		<xsl:param name="tableRowValue" />
		<![CDATA[<tr><td>]]><xsl:value-of select="$tableRowLabel" />
		<![CDATA[</td><td><a href="#" onclick="var w=window.open(']]><xsl:value-of select="'wfsFeaturePopup.do?url='" />
		<xsl:value-of select="$tableRowValue" /><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;">]]><xsl:value-of
		select="substring-after($tableRowValue,'=')" /><![CDATA[</a></td></tr>]]>
	</xsl:template>


	<!-- ================================================================= -->
	<!-- THIS FUNCTION RESOLVES HTTP ADDRESS OF A RELATED FEATURE -->
	<!-- THE er:specification ELEMENT MAY CONTAIN LINK TO URN RESOLVER -->
	<!-- OR XPOINTER LOCAL REFERENCE TO THE DOCUMENT IT IS LEAVING IN -->
	<!-- eg. #er.miningactivity.1 -->
	<!-- -->
	<!-- PARAM: specification - HTTP OF URN RESOLVER OR LOCAL XPOINTER -->
	<!-- PARAM: candidate1 - AN ALTERNATIVE TO CHECK FOR RESOLVER'S HTTP -->
	<!-- PARAM: candidate2 - DEFAULT, GetFeature REQUEST URL -->
	<!-- ================================================================= -->
	<xsl:template name="createHrefLink">
		<xsl:param name="thisGmlName" />
		<xsl:param name="specification" />
		<xsl:param name="candidate1" />
		<xsl:param name="candidate2" />

		<xsl:choose>
			<xsl:when test="starts-with($specification,'http')">
				<xsl:value-of select="$specification" />
			</xsl:when>
			<xsl:when test="starts-with($specification,'#')">
				<xsl:choose>
					<xsl:when test="starts-with($candidate1,'http')">
						<xsl:value-of select="substring-before($candidate1,'urn:cgi')" />
						<xsl:value-of select="$thisGmlName" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$candidate2" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>

	</xsl:template>


	<!-- ================================================================= -->
	<!-- FUNCTION TO TRANSLATE X Y COORDS TO X,Y,0 -->
	<!-- KML format:LONGITUDE(X),LATITUDE(Y),ALTITUDE(0) (in that order) -->
	<!-- Note: Used with Saxon / XSLT 2.0 -->
	<!-- ================================================================= -->
	<xsl:template name="parseLongLatCoord">
		<xsl:param name="coordinates" />

		<xsl:variable name="tokens" select="tokenize($coordinates, '\s+')" />
		<xsl:variable name="start" select="true()" />

		<xsl:for-each select="$tokens">
			<xsl:variable name="pos" select="position()" />
			<xsl:if test="$pos != last()">

				<!-- Add space after each set of coordinates -->
				<xsl:if test="$pos != 1 and $pos != (last() - 1)">
					<xsl:value-of select="' '" />
				</xsl:if>

				<!-- Print set of coordinates -->
				<xsl:if test="$pos mod 2 != 0">
					<xsl:value-of select="concat(., ',', $tokens[$pos + 1], ',0')" />
				</xsl:if>
			</xsl:if>
		</xsl:for-each>

	</xsl:template>


	<!-- ================================================================= -->
	<!-- FUNCTION TO TRANSLATE Y X COORDS TO X,Y,0 -->
	<!-- KML format:LONGITUDE(X),LATITUDE(Y),ALTITUDE(0) (in that order) -->
	<!-- Note: Used with Saxon / XSLT 2.0 -->
	<!-- ================================================================= -->
	<xsl:template name="parseLatLongCoord">
		<xsl:param name="coordinates" />

		<xsl:variable name="tokens" select="tokenize($coordinates, '\s+')" />
		<xsl:variable name="start" select="true()" />

		<xsl:for-each select="$tokens">
			<xsl:variable name="pos" select="position()" />
			<xsl:if test="$pos != last()">

				<!-- Add space after each set of coordinates -->
				<xsl:if test="$pos != 1 and $pos != (last() - 1)">
					<xsl:value-of select="' '" />
				</xsl:if>

				<!-- Print set of coordinates -->
				<xsl:if test="$pos mod 2 != 0">
					<xsl:value-of select="concat($tokens[$pos + 1],',',.,',0')" />
				</xsl:if>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- This function creates a same window popup link -->
	<!-- PARAM: friendly-name -->
	<!-- PARAM: real-url -->
	<!-- Returns something like this <a href="#" onclick="var w=window.open('{real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return
		false;">friendly-name</a> -->
	<!-- ================================================================= -->
	<xsl:template name="make-popup-url">
		<xsl:param name="friendly-name" />
		<xsl:param name="real-url" />
      <![CDATA[<a href="#" onclick="var w=window.open(']]><xsl:value-of select="$real-url" /><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">]]><xsl:value-of
		select="$friendly-name" /><![CDATA[</a>]]></xsl:template>



	<!-- ================================================================= -->
	<!-- This function creates a same window popup link -->
	<!-- PARAM: friendly-name -->
	<!-- PARAM: real-url -->
	<!-- Returns something like this <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return
		false;">friendly-name</a> -->
	<!-- ================================================================= -->
	<xsl:template name="make-wfspopup-url">
		<xsl:param name="friendly-name" />
		<xsl:param name="real-url" />
      <![CDATA[<a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url=]]><xsl:value-of select="$real-url" /><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">]]><xsl:value-of
		select="$friendly-name" /><![CDATA[</a>]]></xsl:template>

	<!-- ================================================================= -->
	<!-- This function creates the generic table cdata header -->
	<!-- Returns something like this <![CDATA[<table border="1" cellspacing="1" cellpadding="2" width="100%" bgcolor="#EAF0F8"> -->
	<xsl:template name="start-table"><![CDATA[<div style='min-width: 40px; max-width:650px; min-height: 40px; max-height: 350px; overflow: auto;"'><table border="1" cellpadding="4" class="auscopeTable">]]>
    </xsl:template>
</xsl:stylesheet>
