<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns="http://www.opengis.net/kml/2.2"
    xmlns:er="urn:cgi:xmlns:GGIC:EarthResource:1.1"
    xmlns:geodesy="http://www.auscope.org/geodesy"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"
    xmlns:ngcp="http://www.auscope.org/ngcp"
    xmlns:sa="http://www.opengis.net/sampling/1.0"
    xmlns:wfs="http://www.opengis.net/wfs"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="er geodesy gml gsml ngcp sa wfs xsl xlink">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"
      cdata-section-elements="description Snippet text"
      media-type="application/vnd.google-earth.kml+xml"/>

   <!--  Examples of available markers - not easy to find on the net
    xmlns:str="http://exslt.org/strings" exclude-result-prefixes="str"
    xmlns="http://earth.google.com/kml/2.0"
    xmlns:set="http://exslt.org/sets"
    xmlns:gpx="http://www.topografix.com/GPX/1/1"
    xmlns:gpx10="http://www.topografix.com/GPX/1/0"
    exclude-result-prefixes="gml gpx gpx10 gsml wfs kml"
    extension-element-prefixes="set">

    http://maps.google.com/mapfiles/kml/paddle/pink-blank.png
    http://maps.google.com/mapfiles/kml/paddle/ltblu-blank.png

    http://maps.google.com/mapfiles/kml/paddle/ylw-blank-lv.png
    http://maps.google.com/mapfiles/kml/paddle/pink-blank-lv.png
    http://maps.google.com/mapfiles/kml/paddle/grn-blank-lv.png
    http://maps.google.com/mapfiles/kml/paddle/blu-blank-lv.png
    http://maps.google.com/mapfiles/kml/paddle/ltblu-blank-lv.png
    http://maps.google.com/mapfiles/kml/paddle/wht-blank-lv.png

    http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png

    http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/ylw-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/pink-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/red-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/ltblu-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/blu-diamond.png
    http://maps.google.com/mapfiles/kml/paddle/grn-diamond.png

    http://maps.google.com/mapfiles/kml/shapes/target.png
   -->


   <!-- External parameter -->
   <xsl:param name="serviceURL"/>
   <xsl:variable name="vocab-hard-coded-lookupCGI" select="concat('http://services-test.auscope.org/SISSVoc/getConceptByURI?CGI/', '')"/>

   <!-- Replace the above parameter with the one below for stand-alone testing
   <xsl:variable name="serviceURL" select="'http://gsv-ws.dpi.vic.gov.au/EarthResourceML/1.1/wfs?'"/>
   -->

   <xsl:variable name="mineServiceString"><![CDATA[service=WFS&version=1.1.0&request=GetFeature&typename=er:Mine&featureid=]]></xsl:variable>
   <xsl:variable name="minOccServiceString"><![CDATA[service=WFS&version=1.1.0&request=GetFeature&typename=er:MineralOccurrence&featureid=]]></xsl:variable>
   <xsl:variable name="gsmlGeoUnitString"><![CDATA[?service=WFS&version=1.1.0&request=GetFeature&typename=gsml:GeologicUnit&featureId=]]></xsl:variable>
   <!-- MATCH ROOT FEATURECOLLECTION -->
   <!-- ================================================================= -->
   <xsl:template match="wfs:FeatureCollection">

      <!--<kml xmlns="http://www.opengis.net/kml/2.2">-->
      <kml>
         <Document>
            <!-- STANDARD NAME AND DESCRIPTION FOR CONVERTED FILE -->
            <name>
               <xsl:text>GML Links to KML</xsl:text>
            </name>

            <description>
               <xsl:text>GeoSciML data converted to KML</xsl:text>
            </description>

            <xsl:apply-templates select="gml:featureMembers/* | gml:featureMember/*"/>
         </Document>
      </kml>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING er:Mine -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/er:Mine | gml:featureMembers/er:Mine" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./er:occurrence/er:MiningFeatureOccurrence/er:location/gml:Point/gml:pos"/>
      </xsl:variable>

      <xsl:variable name="mineName">
         <xsl:value-of select="./er:mineName/er:MineName[./er:isPreferred = true()]/er:mineName/text()"/>
      </xsl:variable>

      <xsl:variable name="mineNameHrefLink">
         <xsl:call-template name="createHrefLink">
            <xsl:with-param name="thisGmlName" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
            <xsl:with-param name="specification" select="./er:occurrence/er:MiningFeatureOccurrence/er:specification/@xlink:href"/>
            <xsl:with-param name="candidate1" select="''"/>
            <xsl:with-param name="candidate2">
               <xsl:value-of select="$serviceURL"/><xsl:value-of select="$mineServiceString"/><xsl:value-of select="@gml:id"/></xsl:with-param>
         </xsl:call-template>
      </xsl:variable>

      <xsl:if test="$coordinates">
         <Placemark>
            <name><xsl:value-of select="$mineName"/></name>
            <description>
               <xsl:call-template name="start-table"></xsl:call-template>
               <![CDATA[<tr><td>Id</td><td>]]>
                   <xsl:call-template name="make-wfspopup-url">
                      <xsl:with-param name="friendly-name" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                      <xsl:with-param name="real-url" select="$mineNameHrefLink"/>
                   </xsl:call-template>
               <![CDATA[</td></tr><tr><td>Preferred Name</td><td>]]><xsl:value-of select="$mineName"/>
               <![CDATA[</td></tr><tr><td>Location</td><td>]]><xsl:value-of select="$coordinates"/>
               <![CDATA[</td></tr><tr><td>Status</td><td>]]><xsl:value-of select="./er:status"/>
               <![CDATA[</td></tr></table></div>]]>
            </description>

            <xsl:apply-templates select="er:occurrence/er:MiningFeatureOccurrence/er:location/gml:Point"/>
         </Placemark>
      </xsl:if>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING Mining Activity -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/er:MiningFeatureOccurrence | gml:featureMembers/er:MiningFeatureOccurrence" priority="100">

        <xsl:variable name="activity">
         <xsl:value-of select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/@gml:id"/>
      </xsl:variable>

      <xsl:if test="$activity">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./er:location/gml:Point/gml:pos"/>
      </xsl:variable>

      <xsl:if test="$coordinates">

            <xsl:variable name="mineNameHrefLink">
             <xsl:call-template name="createHrefLink">
                <xsl:with-param name="thisGmlName" select="gml:id"/>
                <xsl:with-param name="specification" select="gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                <xsl:with-param name="candidate1" select="''"/>
                <xsl:with-param name="candidate2" select="''"/>
             </xsl:call-template>
          </xsl:variable>

          <xsl:variable name="count">
             <xsl:value-of select="count(./er:specification/er:Mine/er:relatedActivity/er:MiningActivity)"/>
          </xsl:variable>

          <xsl:variable name="minStartDate">
            <xsl:for-each select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition">
            <xsl:sort select="." data-type="text" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
            </xsl:for-each>
          </xsl:variable>

          <xsl:variable name="maxEndDate">
            <xsl:for-each select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition">
            <xsl:sort select="." data-type="text" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
            </xsl:for-each>
          </xsl:variable>

         <Placemark>
            <name><xsl:value-of select="@gml:id"/></name>
            <description>
               <xsl:call-template name="start-table"></xsl:call-template>
               <![CDATA[<tr><td>Id</td><td>]]>
                   <xsl:call-template name="make-wfspopup-url">
                      <xsl:with-param name="friendly-name" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                      <xsl:with-param name="real-url" select="$mineNameHrefLink"/>
                   </xsl:call-template>
               <![CDATA[</td>]]>
               <![CDATA[</tr><tr><td>Location</td><td>]]><xsl:value-of select="$coordinates"/><![CDATA[</td>]]>
               <![CDATA[</tr><tr><td>Mining Activity Count</td><td>]]><xsl:value-of select="$count"/><![CDATA[</td>]]>
               <![CDATA[</tr><tr><td>Earliest Start Date</td><td>]]><xsl:value-of select="$minStartDate"/><![CDATA[</td>]]>
               <![CDATA[</tr><tr><td>Latest End Date</td><td>]]><xsl:value-of select="$maxEndDate"/><![CDATA[</td>]]>
               <![CDATA[<tr><td>Product</td><td>]]>
               <xsl:for-each-group select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:productName/gsml:CGI_TermValue/gsml:value" group-by=".">
                     <xsl:value-of select="."/><xsl:if test="position() != last()"><![CDATA[, ]]></xsl:if>
               </xsl:for-each-group>
               <![CDATA[</td>]]>
               <![CDATA[</tr><tr><td>Activity Type</td><td>]]>
               <xsl:for-each-group select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:activityType" group-by=".">
                     <xsl:value-of select="."/><xsl:if test="position() != last()"><![CDATA[, ]]></xsl:if>
               </xsl:for-each-group>
               <![CDATA[</td></tr>]]>
               <![CDATA[<tr><td>Commodity(s)</td><td>]]>

               <xsl:choose>
               <xsl:when test="exists(./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:sourceCommodity/er:Commodity)">
                   <xsl:for-each-group select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:sourceCommodity/er:Commodity" group-by=".">
                       <xsl:call-template name="make-wfspopup-url">
                           <xsl:with-param name="friendly-name" select="gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                           <xsl:with-param name="real-url" select="$mineNameHrefLink"/>
                       </xsl:call-template><xsl:if test="position() != last()"><![CDATA[, ]]></xsl:if>
                   </xsl:for-each-group>
               </xsl:when>
               <xsl:when test="exists(./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:sourceCommodity/@xlink:href)">
               <xsl:for-each-group select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity/er:producedMaterial/er:Product/er:sourceCommodity/@xlink:href" group-by=".">
                   <xsl:choose>
                       <xsl:when test="starts-with(., 'http')">
                           <xsl:call-template name="make-wfspopup-url">
                               <xsl:with-param name="friendly-name" select="."/>
                               <xsl:with-param name="real-url" select="."/>
                           </xsl:call-template><xsl:if test="position() != last()"><![CDATA[, ]]></xsl:if>
                          </xsl:when>
                          <xsl:otherwise>
                              <xsl:value-of select="."/><xsl:if test="position() != last()"><![CDATA[, ]]></xsl:if>
                          </xsl:otherwise>
                       </xsl:choose>
               </xsl:for-each-group>
               </xsl:when>
               </xsl:choose>

               <![CDATA[</td></tr>]]>
               <![CDATA[</table></div>]]>
            </description>
            <xsl:apply-templates select="./descendant::gml:Point"/>
         </Placemark>

      </xsl:if>
      </xsl:if>
   </xsl:template>



   <!-- TEMPLATE FOR TRANSLATING Mineral Occurrences -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/er:MineralOccurrence | gml:featureMembers/er:MineralOccurrence" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:occurrence/gsml:MappedFeature/gsml:shape/gml:Point/gml:pos"/>
      </xsl:variable>
      <xsl:variable name="resource_id">
         <xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
      </xsl:variable>

      <xsl:variable name="idHrefLink">
         <xsl:call-template name="createHrefLink">
         <xsl:with-param name="thisGmlName" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
         <xsl:with-param name="specification" select="./gsml:occurrence/gsml:MappedFeature/gsml:specification/@xlink:href"/>
         <xsl:with-param name="candidate1" select="''"/>
         <xsl:with-param name="candidate2">
            <xsl:value-of select="$serviceURL"/><xsl:value-of select="minOccServiceString"/><xsl:value-of select="@gml:id"/></xsl:with-param>
         </xsl:call-template>
      </xsl:variable>

      <Placemark>
         <name><xsl:value-of select="$resource_id"/></name>
         <description>
            <xsl:call-template name="start-table"></xsl:call-template>
            <![CDATA[<tr><td>Id</td><td>]]>
            <xsl:call-template name="make-wfspopup-url">
                <xsl:with-param name="friendly-name" select="$resource_id"/>
                <xsl:with-param name="real-url" select="$idHrefLink"/>
            </xsl:call-template>
            <![CDATA[</td>]]>
            <![CDATA[</tr><tr><td>Type</td><td>]]><xsl:value-of select="./er:type"/>
            <![CDATA[</td></tr><tr><td>Mineral Deposit Group</td><td>]]><xsl:value-of select="./er:classification/er:MineralDepositModel/er:mineralDepositGroup"/><![CDATA[</td></tr>]]>
            <xsl:apply-templates select="./er:commodityDescription"/>
            <![CDATA[</table></div>]]>
         </description>

         <xsl:apply-templates select="./gsml:occurrence/gsml:MappedFeature/gsml:shape/gml:Point"/>
      </Placemark>
   </xsl:template>
   <!-- TEMPLATE FOR Commodity Description -->
   <!-- ================================================================= -->
   <xsl:template match="er:sourceCommodity">
      <![CDATA[<tr><td>]]>Commodity Description <![CDATA[</td>]]>
      <![CDATA[<td>]]><xsl:call-template name="make-wfspopup-url">
         <xsl:with-param name="friendly-name" select="@xlink:href"/>
         <xsl:with-param name="real-url" select="@xlink:href"/>
      </xsl:call-template><![CDATA[</td></tr>]]>
   </xsl:template>


   <!-- TEMPLATE FOR Commodity Description -->
   <!-- ================================================================= -->
   <xsl:template match="er:commodityDescription">
      <![CDATA[<tr><td>]]>Commodity Description <![CDATA[</td>]]>
      <![CDATA[<td>]]><xsl:call-template name="make-wfspopup-url">
         <xsl:with-param name="friendly-name" select="./er:Commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']/text()"/>
         <xsl:with-param name="real-url" select="./er:Commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']/text()"/>
      </xsl:call-template><![CDATA[</td></tr>]]>
   </xsl:template>

   <!-- TEMPLATE FOR TRANSLATING Geologic Unit -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:GeologicUnit" priority="100">

         <!--
         <xsl:value-of select="local-name()"/>
         <xsl:value-of select="namespace-uri()"/>
         -->
      <xsl:if test="gsml:occurrence" >
         <Placemark>
            <name><xsl:value-of select="./gml:name[starts-with(@codeSpace,'http://')]"/></name>
            <description>
            <!--
               <![CDATA[<table border="1" cellspacing="1" width="100%">
               <tr><td>GeologicUnit</td><td>]]><xsl:value-of select="./gml:name[starts-with(@codeSpace,'http://')]"/>
               <![CDATA[</td></tr><tr><td>Name</td><td>]]><xsl:value-of select="./gml:description"/>
               <![CDATA[</td></tr><tr><td>Type</td><td>]]><xsl:value-of select="./gsml:geologicUnitType"/>
               --><!-- To DO: If no age then use preferedAge
               <![CDATA[</td></tr><tr><td>Age</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventAge/gsml:CGI_TermRange/gsml:lower/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Environment</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventEnvironment/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Process</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventProcess/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Lithology</td><td>]]><xsl:value-of select="./gsml:composition/gsml:CompositionPart/gsml:lithology"/>
               <![CDATA[</td></tr></table></div>]]>
               -->
            </description>

            <xsl:apply-templates select="./gsml:occurrence//gsml:shape"/>

         </Placemark>
      </xsl:if>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE -
   Mineral Occurence is requested via Mapped Feature (so the geometry can be extracted) -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:MappedFeature | gml:featureMembers/gsml:MappedFeature" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:shape/gml:Point"/>
      </xsl:variable>
      <xsl:variable name="resource_id">
         <xsl:value-of select="./gsml:specification/er:MineralOccurrence/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
      </xsl:variable>

      <xsl:variable name="idHrefLink">
         <xsl:call-template name="createHrefLink">
         <xsl:with-param name="thisGmlName" select="./gsml:specification/er:MineralOccurrence/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
         <xsl:with-param name="specification" select="./gsml:specification/er:MineralOccurrence/er:commodityDescription[1]/er:Commodity/er:source/@xlink:href"/>
         <xsl:with-param name="candidate1" select="''"/>
         <xsl:with-param name="candidate2">
            <xsl:value-of select="$serviceURL"/><xsl:value-of select="minOccServiceString"/><xsl:value-of select="./gsml:specification/er:MineralOccurrence/@gml:id"/>
         </xsl:with-param>
         </xsl:call-template>
      </xsl:variable>

      <Placemark>
         <name><xsl:value-of select="$resource_id"/></name>
         <description>
            <xsl:call-template name="start-table"></xsl:call-template>
            <![CDATA[<tr><td>Id</td><td>]]>
            <xsl:call-template name="make-wfspopup-url">
                <xsl:with-param name="friendly-name" select="$resource_id"/>
                <xsl:with-param name="real-url" select="$resource_id"/>
            </xsl:call-template>
            <![CDATA[</td>]]>
            <![CDATA[</tr><tr><td>Type</td><td>]]><xsl:value-of select="./gsml:specification/er:MineralOccurrence/er:type"/>
            <![CDATA[</td></tr><tr><td>Mineral Deposit Group</td><td>]]><xsl:value-of select="./gsml:specification/er:MineralOccurrence/er:classification/er:MineralDepositModel/er:mineralDepositGroup"/><![CDATA[</td></tr>]]>
            <xsl:apply-templates select="./gsml:specification/er:MineralOccurrence/er:commodityDescription"/>
            <![CDATA[</table></div>]]>
         </description>

         <xsl:apply-templates select="./gsml:shape/gml:Point"/>
      </Placemark>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING GU ShearDisplacementStructure -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:ShearDisplacementStructure" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:occurrence/gsml:MappedFeature/gsml:shape/gml:MultiCurve/gml:curveMember/gml:LineString/gml:posList"/>
      </xsl:variable>

      <xsl:if test="gsml:occurrence" >
         <Placemark>
            <name><xsl:value-of select="@gml:id"/></name>
            <description>
               <xsl:call-template name="start-table"></xsl:call-template>
               <![CDATA[<tr><td>Id</td><td>]]><xsl:value-of select="./gml:name[2]"/>
               <![CDATA[</td></tr></table></div>]]>
            </description>

            <xsl:apply-templates select="./gsml:occurrence//gsml:shape"/>
         </Placemark>
      </xsl:if>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING GEODESY -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/ngcp:GnssStation | gml:featureMembers/ngcp:GnssStation" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./ngcp:GEOM/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./ngcp:GPSSITEID"/></name>
         <description>
            <xsl:call-template name="start-table"></xsl:call-template>
            <![CDATA[<tr><td>Station Id</td><td>]]><xsl:value-of select="./ngcp:GPSSITEID"/>
            <![CDATA[</td></tr><tr><td>Name</td><td>]]><xsl:value-of select="./ngcp:STATIONNAME"/>
            <![CDATA[</td></tr><tr><td>Type</td><td>]]><xsl:value-of select="./ngcp:STATIONTYPE"/>
            <![CDATA[</td></tr><tr><td>Country Id</td><td>]]><xsl:value-of select="./ngcp:COUNTRYID"/>
            <![CDATA[</td></tr><tr><td>State</td><td>]]><xsl:value-of select="./ngcp:STATEID"/>
            <![CDATA[</td></tr><tr><td>Location</td><td>]]><xsl:value-of select="$coordinates"/>
            <![CDATA[</td></tr><tr><td>CoordinateNO</td><td>]]><xsl:value-of select="./ngcp:COORDINO"/>
            <![CDATA[</td></tr><tr><td>DATUM</td><td>]]><xsl:value-of select="./ngcp:DATUM"/>
            <![CDATA[</td></tr><tr><td>Equipment</td><td>]]><xsl:value-of select="./ngcp:EQUIPMENT"/>
            <![CDATA[</td></tr></table></div>]]>
         </description>

         <xsl:apply-templates select="./ngcp:GEOM/gml:Point"/>
      </Placemark>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING NVCL -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMembers/gsml:Borehole" priority="100">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
      </xsl:variable>

      <xsl:variable name="rawId">
         <xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
      </xsl:variable>

      <xsl:variable name="idTokens" select="tokenize($rawId, '/')"/>

      <xsl:variable name="clickyName">
            <xsl:call-template name="make-popup-url">
                <xsl:with-param name="friendly-name" select="./gml:name[@codeSpace!='http://www.ietf.org/rfc/rfc2616']"/>
                <xsl:with-param name="real-url" select="$rawId"/>
            </xsl:call-template>
      </xsl:variable>




      <Placemark>
         <name><xsl:value-of select="$idTokens[last()]"/></name>
         <description>
            <xsl:call-template name="start-table"></xsl:call-template>
            <![CDATA[<tr><td>Borehole Name</td><td>]]><xsl:value-of select="$clickyName"/>
            <![CDATA[</td></tr><tr><td>Location</td><td>]]><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
            <![CDATA[</td></tr><tr><td>Elevation (]]><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation/@uomLabels"/><![CDATA[)</td><td>]]><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation"/>
            <![CDATA[</td></tr><tr><td>Start Depth (m)</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:lowerCorner"/>
            <![CDATA[</td></tr><tr><td>End Depth (m)</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:upperCorner"/>
            <![CDATA[</td></tr><tr><td>Core Custodian</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title"/>
            <![CDATA[</td></tr><tr><td>Drilling Co.</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:driller/@xlink:title"/>
            <![CDATA[</td></tr><tr><td>Drilling Date</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling"/>
            <![CDATA[</td></tr><tr><td>Drilling Method</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod"/>
            <![CDATA[</td></tr><tr><td>Inclination Type</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType"/>
            <![CDATA[</td></tr><tr><td>Start Point</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:startPoint"/>
            <![CDATA[</td></tr></table></div>]]>
         </description>

         <xsl:apply-templates select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point"/>
      </Placemark>
   </xsl:template>

    <!--TEMPLATE FOR TRANSLATING A GENERIC INPUT-->
    <!-- ================================================================= -->
    <xsl:template match="gml:featureMembers/* | gml:featureMember/*" priority="1">
        <Placemark>
            <name>
                <xsl:value-of select="@gml:id"/>
            </name>

            <!-- This is just a simple description of the node -->
            <description>
               <!--  This is to tell the JS to specifically search for this -->
               <xsl:text>GENERIC_PARSER:</xsl:text><xsl:value-of select="@gml:id"/>
            </description>

            <!-- This is so we can pickup where the node should be drawn -->
            <MultiGeometry>
               <xsl:apply-templates select="./descendant::gml:Polygon"/>
               <xsl:apply-templates select="./descendant::gml:Multicurve"/>
               <xsl:apply-templates select="./descendant::gml:Point"/>
               <xsl:apply-templates select="./descendant::gml:MultiLineString"/>
            </MultiGeometry>
        </Placemark>
    </xsl:template>

   <!-- TEMPLATE FOR TRANSLATING GNSS -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMembers/sa:SamplingPoint">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./sa:position/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./gml:name"/></name>
         <description>
            <xsl:call-template name="start-table"></xsl:call-template>
            <![CDATA[<tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="./sa:position/gml:Point/gml:pos"/>
            <![CDATA[</td></tr></table></div>]]>
         </description>

         <xsl:apply-templates select="./sa:position/gml:Point"/>
      </Placemark>
   </xsl:template>


   <!-- ================================================================= -->
   <xsl:template match="gsml:shape">
      <xsl:apply-templates select="gml:Point"/>
      <xsl:apply-templates select="gml:Polygon"/>
      <xsl:apply-templates select="gml:MultiCurve"/>
      <xsl:apply-templates select="gml:MultiSurface//gml:PolygonPatch"/>
   </xsl:template>


   <!-- ================================================================= -->
   <xsl:template match="gml:Point">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gml:pos"/>
      </xsl:variable>

      <Point>
         <Style>
            <IconStyle>
               <Icon><href>http://maps.google.com/mapfiles/kml/paddle/ylw-blank.png</href></Icon>
            </IconStyle>
         </Style>

         <coordinates>
            <xsl:choose>
                <xsl:when test="starts-with(@srsName,'http://www.opengis.net/gml/srs/epsg.xml#4283')">
                  <xsl:call-template name="parseLatLongCoord">
                     <xsl:with-param name="coordinates" select="$coordinates"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="starts-with(@srsName,'EPSG')">
                  <xsl:call-template name="parseLongLatCoord">
                     <xsl:with-param name="coordinates" select="$coordinates"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="starts-with(@srsName,'http://www.opengis.net/gml/srs/epsg.xml')">
                  <xsl:call-template name="parseLongLatCoord">
                     <xsl:with-param name="coordinates" select="$coordinates"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="starts-with(@srsName,'urn:x-ogc:def:crs:EPSG')">
                  <xsl:call-template name="parseLatLongCoord">
                     <xsl:with-param name="coordinates" select="$coordinates"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="parseLongLatCoord">
                     <xsl:with-param name="coordinates" select="$coordinates"/>
                  </xsl:call-template>
               </xsl:otherwise>
            </xsl:choose>
         </coordinates>
      </Point>
   </xsl:template>


   <!-- ================================================================= -->
   <xsl:template match="gml:Polygon">
      <Polygon>
          <altitudeMode><xsl:text>clampToGround</xsl:text></altitudeMode>
         <xsl:apply-templates select="gml:exterior"/>
         <xsl:apply-templates select="gml:interior"/>
      </Polygon>
   </xsl:template>


   <!-- ================================================================= -->
   <xsl:template match="gml:MultiCurve">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gml:curveMembers/gml:Curve/gml:segments/gml:LineStringSegment/gml:posList | ./gml:curveMember/gml:LineString/gml:posList"/>
      </xsl:variable>

      <Style>
         <LineStyle>
               <color>ff004080</color>
            </LineStyle>
        </Style>

        <LineString>
         <coordinates>
            <xsl:call-template name="parseLatLongCoord">
               <xsl:with-param name="coordinates" select="$coordinates"/>
            </xsl:call-template>
            </coordinates>
        </LineString>
   </xsl:template>

   <xsl:template match="gml:MultiLineString">
        <xsl:variable name="int_coordinates">
                 <xsl:value-of select="./gml:lineStringMember/gml:LineString/gml:posList"/>
        </xsl:variable>

        <LineString>
            <extrude><xsl:text>1</xsl:text></extrude>
            <tessellate><xsl:text>1</xsl:text></tessellate>
            <coordinates>
                <xsl:call-template name="parseLatLongCoord">
                    <xsl:with-param name="coordinates" select="$int_coordinates"/>
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
         <xsl:apply-templates select="gml:exterior"/>
         <xsl:apply-templates select="gml:interior"/>
      </Polygon>
   </xsl:template>


   <!-- TEMPLATE FOR Polygon : exterior -->
   <!-- ================================================================= -->
   <xsl:template match="gml:exterior">

      <xsl:variable name="ext_coordinates">
         <xsl:value-of select="./gml:LinearRing/gml:posList"/>
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
         <xsl:value-of select="./gml:LinearRing/gml:posList"/>
      </xsl:variable>

      <innerBoundaryIs>
         <LinearRing>
            <coordinates>
               <xsl:call-template name="parseLatLongCoord">
                  <xsl:with-param name="coordinates" select="$int_coordinates"/>
               </xsl:call-template>
            </coordinates>
         </LinearRing>
      </innerBoundaryIs>
   </xsl:template>


   <!-- ================================================================= -->
   <!--    FUNCTION TO DISPLAY URN RESOLVER LINK WITHIN HTML TABLE ROW    -->
   <!--    PARAM: tableRowLabel                                           -->
   <!--    PARAM: tableRowValue                                           -->
   <!-- ================================================================= -->
   <xsl:template name="displayUrnResolverLink">
      <xsl:param name="tableRowLabel"/>
      <xsl:param name="tableRowValue"/>
      <![CDATA[</td></tr><tr><td>]]><xsl:value-of select="$tableRowLabel"/><![CDATA[</td><td><a href="#" onclick="var w=window.open(']]><xsl:value-of select="$tableRowValue"/><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;">]]><xsl:value-of select="$tableRowValue"/><![CDATA[</a>]]>
   </xsl:template>


   <!-- ================================================================= -->
   <!--    THIS FUNCTION TAKES HTTP LINK TO URN RESOLVER AND CONSTRUCTS   -->
   <!--    HREF HTML LINK TO DISPLAY CONTENT IN A NEW WINDOW              -->
   <!--                                                                   -->
   <!--    IT STRIPS THE 'http://...=' PREFIX FROM THE tableRowValue      -->
   <!--    PARAMETER AND DISPLAYS JUST THE URN PART TO THE USER           -->
   <!--                                                                   -->
   <!--    PARAM: tableRowLabel                                           -->
   <!--    PARAM: tableRowValue - HTTP RESOLVER LINK                      -->
   <!-- ================================================================= -->
   <xsl:template name="displayUrnResolverLinkWithoutHTTP">
      <xsl:param name="tableRowLabel"/>
      <xsl:param name="tableRowValue"/>
      <![CDATA[<tr><td>]]><xsl:value-of select="$tableRowLabel"/>
      <![CDATA[</td><td><a href="#" onclick="var w=window.open(']]><xsl:value-of select="'wfsFeaturePopup.do?url='"/><xsl:value-of select="$tableRowValue"/><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;">]]><xsl:value-of select="substring-after($tableRowValue,'=')"/><![CDATA[</a></td></tr>]]>
   </xsl:template>


   <!-- ================================================================= -->
   <!--    THIS FUNCTION RESOLVES HTTP ADDRESS OF A RELATED FEATURE       -->
   <!--    THE er:specification ELEMENT MAY CONTAIN LINK TO URN RESOLVER  -->
   <!--    OR XPOINTER LOCAL REFERENCE TO THE DOCUMENT IT IS LEAVING IN   -->
   <!--    eg. #er.miningactivity.1                                       -->
   <!--                                                                   -->
   <!--    PARAM: specification - HTTP OF URN RESOLVER OR LOCAL XPOINTER  -->
   <!--    PARAM: candidate1 - AN ALTERNATIVE TO CHECK FOR RESOLVER'S HTTP-->
   <!--    PARAM: candidate2 - DEFAULT, GetFeature REQUEST URL            -->
   <!-- ================================================================= -->
   <xsl:template name="createHrefLink">
      <xsl:param name="thisGmlName"/>
      <xsl:param name="specification"/>
      <xsl:param name="candidate1"/>
      <xsl:param name="candidate2"/>

      <xsl:choose>
         <xsl:when test="starts-with($specification,'http')">
            <xsl:value-of select="$specification" />
         </xsl:when>
         <xsl:when test="starts-with($specification,'#')">
            <xsl:choose>
               <xsl:when test="starts-with($candidate1,'http')">
                  <xsl:value-of select="substring-before($candidate1,'urn:cgi')" /><xsl:value-of select="$thisGmlName" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="$candidate2"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
      </xsl:choose>

   </xsl:template>


   <!-- ================================================================= -->
   <!--    FUNCTION TO TRANSLATE  X Y  COORDS TO  X,Y,0                   -->
   <!--    KML format:LONGITUDE(X),LATITUDE(Y),ALTITUDE(0) (in that order)-->
   <!--    Note: Used with Saxon / XSLT 2.0                               -->
   <!-- ================================================================= -->
   <xsl:template name="parseLongLatCoord">
      <xsl:param name="coordinates"/>

      <xsl:variable name="tokens" select="tokenize($coordinates, '\s+')"/>
      <xsl:variable name="start" select="true()"/>

      <xsl:for-each select="$tokens">
         <xsl:variable name="pos" select="position()"/>
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
   <!--    FUNCTION TO TRANSLATE  Y X  COORDS TO  X,Y,0                   -->
   <!--    KML format:LONGITUDE(X),LATITUDE(Y),ALTITUDE(0) (in that order)-->
   <!--    Note: Used with Saxon / XSLT 2.0                               -->
   <!-- ================================================================= -->
   <xsl:template name="parseLatLongCoord">
      <xsl:param name="coordinates"/>

      <xsl:variable name="tokens" select="tokenize($coordinates, '\s+')"/>
      <xsl:variable name="start" select="true()"/>

      <xsl:for-each select="$tokens">
         <xsl:variable name="pos" select="position()"/>
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
   <!--    This function swaps the coordinates                             -->
   <!--    PARAM: coordinates                                              -->
   <!-- ================================================================= -->

   <xsl:template name="swapLatLongCoord">
       <xsl:param name="coordinates"/>

      <xsl:variable name="tokens" select="tokenize($coordinates, '\s+')"/>
      <xsl:variable name="start" select="true()"/>

      <xsl:for-each select="$tokens">
         <xsl:variable name="pos" select="position()"/>
         <xsl:if test="$pos != last()">

            <!-- Add space after each set of coordinates -->
            <xsl:if test="$pos != 1 and $pos != (last() - 1)">
               <xsl:value-of select="' '" />
            </xsl:if>

            <!-- Print set of coordinates -->
            <xsl:if test="$pos mod 2 != 0">
               <xsl:value-of select="concat($tokens[$pos + 1],'   ',.)" />
            </xsl:if>
         </xsl:if>
      </xsl:for-each>

   </xsl:template>

   <!-- ================================================================= -->
   <!--    This function creates a same window popup link                 -->
   <!--    PARAM: friendly-name                                           -->
   <!--    PARAM: real-url                                                -->
   <!--    Returns something like this
      <a href="#" onclick="var w=window.open('{real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">friendly-name</a>
   -->
   <!-- ================================================================= -->
   <xsl:template name="make-popup-url">
      <xsl:param name="friendly-name"/>
      <xsl:param name="real-url"/>
      <![CDATA[<a href="#" onclick="var w=window.open(']]><xsl:value-of select="$real-url"/><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">]]><xsl:value-of select="$friendly-name"/><![CDATA[</a>]]></xsl:template>



   <!-- ================================================================= -->
   <!--    This function creates a same window popup link                 -->
   <!--    PARAM: friendly-name                                           -->
   <!--    PARAM: real-url                                                -->
   <!--    Returns something like this
      <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">friendly-name</a>
   -->
   <!-- ================================================================= -->
   <xsl:template name="make-wfspopup-url">
      <xsl:param name="friendly-name"/>
      <xsl:param name="real-url"/>

      <!-- Highly simplified escaping -->
      <xsl:variable name="escaped-url">
        <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text">
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text" select="$real-url"/>
                    <xsl:with-param name="replace" select="'&amp;'"/>
                    <xsl:with-param name="by" select="'%26'"/>
                </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="replace" select="'?'"/>
            <xsl:with-param name="by" select="'%3F'"/>
        </xsl:call-template>
      </xsl:variable>

      <![CDATA[<a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url=]]><xsl:value-of select="$escaped-url"/><![CDATA[','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">]]><xsl:value-of select="$friendly-name"/><![CDATA[</a>]]></xsl:template>

   <!-- ================================================================= -->
   <!--    This function creates the generic table cdata header           -->
   <!--    Returns something like this
         <![CDATA[<table border="1" cellspacing="1" cellpadding="2" width="100%" bgcolor="#EAF0F8">
   -->
   <xsl:template name="start-table"><![CDATA[<div style='min-width: 40px; max-width:650px; min-height: 40px; max-height: 350px; overflow: auto;"'><table border="1" cellpadding="4" class="auscopeTable">]]>
   </xsl:template>

    <!-- Sourced from http://geekswithblogs.net/Erik/archive/2008/04/01/120915.aspx -->
    <xsl:template name="string-replace-all">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="by" />
        <xsl:choose>
            <xsl:when test="contains($text, $replace)">
                <xsl:value-of select="substring-before($text,$replace)" />
                <xsl:value-of select="$by" />
                <xsl:call-template name="string-replace-all">
                    <xsl:with-param name="text"
                        select="substring-after($text,$replace)" />
                    <xsl:with-param name="replace"
                        select="$replace" />
                    <xsl:with-param name="by" select="$by" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>