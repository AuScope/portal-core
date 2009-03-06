<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
    xmlns:wfs="http://www.opengis.net/wfs"
    xmlns:kml="http://www.opengis.net/kml/2.2"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"
    xmlns:sa="http://www.opengis.net/sampling/1.0"
    xmlns:geodesy="http://auscope.org.au/geodesy"
    xmlns:mo="urn:cgi:xmlns:GGIC:MineralOccurrence:1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    >

<!--  
    xmlns="http://earth.google.com/kml/2.0" 
    xmlns:set="http://exslt.org/sets" 
    xmlns:gpx="http://www.topografix.com/GPX/1/1" 
    xmlns:gpx10="http://www.topografix.com/GPX/1/0"  
    exclude-result-prefixes="gml gpx gpx10 gsml wfs kml" 
    extension-element-prefixes="set">
    http://maps.google.com/mapfiles/kml/paddle/wht-blank.png
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

   <!-- MATCH ROOT FEATURECOLLECTION -->
   <xsl:template match="wfs:FeatureCollection">
       
      <!-- PRINT KML HEADER
      <kml xmlns="http://www.opengis.net/kml/2.2">
      -->
      <kml>
         <Document>
               
            <!-- STANDARD NAME AND DESCRIPTION FOR CONVERTED FILE -->
            <name>
               <xsl:text>GML Links to KML</xsl:text>
            </name>
            
            <description>
               <xsl:text>GML data converted to KML</xsl:text>
            </description>
                
            <xsl:apply-templates select="gml:featureMembers/gsml:Borehole"/>
            <xsl:apply-templates select="gml:featureMembers/sa:SamplingPoint"/>
            <xsl:apply-templates select="gml:featureMember/geodesy:stations"/>
            <xsl:apply-templates select="gml:featureMember/mo:MiningFeatureOccurrence"/>
            <xsl:apply-templates select="gml:featureMember/gsml:MappedFeature"/>
         </Document>
      </kml>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING NVCL -->
   <!-- ====================================================== -->
   <xsl:template match="gml:featureMembers/gsml:Borehole">

      <!-- VARIABLE To STORE THE GML COORDINATES -->
      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
      <!--
         <name id="{@gml:id}"><xsl:value-of select="./gml:name"/></name>
         -->
         <name><xsl:value-of select="@gml:id"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Borehole Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Lat Lng (deg)</td><td>]]><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
            <![CDATA[</td></tr><tr><td>Project</td><td>]]><xsl:value-of select="./gml:metaDataProperty/@xlink:title"/>
            <![CDATA[</td></tr><tr><td>Core Custodian</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:href"/>
            <![CDATA[</td></tr><tr><td>Operator</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:operator/@xlink:href"/>
            <![CDATA[</td></tr><tr><td>Date of Drilling</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling"/>
            <![CDATA[</td></tr><tr><td>Drilling Method</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod"/>
            <![CDATA[</td></tr><tr><td>Inclination Type</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType"/>
            <![CDATA[</td></tr><tr><td>Start Point</td><td>]]><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:startPoint"/>
            <![CDATA[</td></tr></table>]]>
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/blu-blank.png</href></Icon>
               </IconStyle>
            </Style>
      
            <coordinates>
               <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
      </Placemark>
   </xsl:template>


   <!-- TEMPLATE FOR TRANSLATING GNSS -->
   <!-- ====================================================== -->
   <xsl:template match="gml:featureMembers/sa:SamplingPoint">
    
      <!-- VARIABLE To STORE THE GML COORDINATES -->
      <xsl:variable name="coordinates">
         <xsl:value-of select="./sa:position/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./gml:name"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Lat Lng (deg)</td><td>]]><xsl:value-of select="./sa:position/gml:Point/gml:pos"/>            
            <![CDATA[</td></tr></table>]]>
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-blank.png</href></Icon>
               </IconStyle>
            </Style>

            <coordinates>
               <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
      </Placemark>
   </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING GPS -->
    <!-- ====================================================== -->
    <xsl:template match="gml:featureMember/geodesy:stations">
    
      <!-- VARIABLE To STORE THE GML COORDINATES -->
      <xsl:variable name="coordinates">
         <xsl:value-of select="./geodesy:location/gml:Point/gml:coordinates"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="@fid"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Station Id</td><td>]]><xsl:value-of select="./geodesy:station_id"/>
            <![CDATA[</td></tr><tr><td>Name</td><td>]]><xsl:value-of select="./geodesy:name"/>
            <![CDATA[</td></tr><tr><td>Url</td><td>]]><xsl:value-of select="./geodesy:url"/>
            <![CDATA[</td></tr><tr><td>Lat Lng (deg)</td><td>]]><xsl:value-of select="$coordinates"/>            
            <![CDATA[</td></tr></table>]]>            
         </description>
         <Point>
                <Style>
                  <IconStyle>
                     <Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-blank.png</href></Icon>
                  </IconStyle>
                </Style>

            <coordinates>
               <xsl:value-of select="./geodesy:location/gml:Point/gml:coordinates"/>
            </coordinates>
         </Point>
      </Placemark>
    </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING MINERAL OCCURRENCES -->
   <!-- ====================================================== -->
   <xsl:template match="gml:featureMember/mo:MiningFeatureOccurrence">

      <!-- VARIABLE To STORE THE GML COORDINATES -->
      <xsl:variable name="coordinates">
         <xsl:value-of select="./mo:location/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./gml:name"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Description</td><td>]]><xsl:value-of select="./gml:description"/>
            <![CDATA[</td></tr><tr><td>Lat Lng (deg)</td><td>]]><xsl:value-of select="$coordinates"/>            
            <![CDATA[</td></tr></table>]]>            
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon>
               </IconStyle>
            </Style>
      
            <coordinates>
               <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
      </Placemark>
   </xsl:template>
    
    
   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE -->
   <!-- ====================================================== -->
   <xsl:template match="gml:featureMember/gsml:MappedFeature">

      <Placemark>
         <name><xsl:value-of select="@gml:id"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Description</td><td>]]><xsl:value-of select="./gml:description"/>            
            <![CDATA[</td></tr></table>]]>
         </description>

         <xsl:apply-templates select="gsml:shape/gml:Point"/>
         <xsl:apply-templates select="gsml:shape/gml:Polygon"/>
         <xsl:apply-templates select="gsml:shape/gml:MultiCurve"/>
         <!--
         <xsl:apply-templates select="gsml:shape/gml:MultiSurface"/>
         -->
      </Placemark>

   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE Points -->
   <!-- ====================================================== -->
   <xsl:template match="gsml:shape/gml:Point">
   
      <!-- VARIABLE To STORE THE GML COORDINATES -->
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
            <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
            <xsl:call-template name="parseCoord">
               <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
               <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
            </xsl:call-template>
         </coordinates>
      </Point>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE Polygons -->
   <!-- ====================================================== -->
   <xsl:template match="gsml:shape/gml:Polygon">
   
      <!-- Variable to store exterior coordinates -->
      <xsl:variable name="ext_coordinates">
         <xsl:value-of select="./gml:exterior/gml:LinearRing/gml:posList"/>
      </xsl:variable>
      
      <!-- Variable to store interior coordinates -->
      <xsl:variable name="int_coordinates">
         <xsl:value-of select="./gml:interior/gml:LinearRing/gml:posList"/>
      </xsl:variable>

      <Polygon>
         <outerBoundaryIs> 
            <LinearRing> 
               <coordinates>
                  <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
                  <xsl:call-template name="parseCoord">
                     <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                     <xsl:with-param name="coordinates" select="concat($ext_coordinates,' ')"/>
                  </xsl:call-template>
               </coordinates>
            </LinearRing>
         </outerBoundaryIs>
         <innerBoundaryIs>
            <LinearRing>
               <coordinates>
                  <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
                  <xsl:call-template name="parseCoord">
                     <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                     <xsl:with-param name="coordinates" select="concat($int_coordinates,' ')"/>
                  </xsl:call-template>
               </coordinates>
            </LinearRing>
         </innerBoundaryIs>
      </Polygon>

   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE MultiCurve -->
   <!-- ====================================================== -->
   <xsl:template match="gsml:shape/gml:MultiCurve">
   
      <!-- Variable to store exterior coordinates -->
      <xsl:variable name="coordinates">
         <xsl:value-of select="./gml:curveMembers/gml:Curve/gml:segments/gml:LineStringSegment/gml:posList"/>
      </xsl:variable>

      <Style>
         <LineStyle>
			   <color>ff004080</color>
			</LineStyle>
		</Style>
		<LineString>
         <coordinates>
            <!-- CALL THE PARSECOORDS FUNCTION TO PROCESS THE COORDINATES -->
            <xsl:call-template name="parseCoord">
               <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
               <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
            </xsl:call-template>
			</coordinates>
		</LineString>

   </xsl:template>
   
   
   <!-- ================================================================= -->
   <!-- FUNCTION TO TRANSLATE X Y COORDS TO X,Y,0 -->
   <!-- ================================================================= -->
   <xsl:template name="parseCoord">
      <xsl:param name="coordinates"/>
      <!-- CHECK IF THERE IS CONTENT BEFORE THE FIRST SPACE YOU REACH -->
      <xsl:if test="substring-before($coordinates,' ')!=''">

         <!-- DIVIDE THE SET OF COORDINATES INTO TWO SECTIONS,
             BEFORE AND AFTER THE FIRST SPACE (CURRENT AND REST) -->
         <xsl:variable name="alt">
            <xsl:value-of select="substring-before($coordinates,' ')"/>
         </xsl:variable>
         <xsl:variable name="rest">
            <xsl:value-of select="substring-after($coordinates,' ')"/>
         </xsl:variable>
         <xsl:variable name="long">
            <xsl:value-of select="substring-before($rest,' ')"/>
         </xsl:variable>
         <xsl:variable name="rest">
            <xsl:value-of select="substring-after($rest,' ')"/>
         </xsl:variable>
         
         <!-- APPEND A "," to ALT AND A ",0" (0 Z VALUE) TO LONG AND PRINT IT OUT -->
         <xsl:value-of select="concat($alt,',')"/>
         <xsl:value-of select="concat($long,',0')"/>
         
         <!-- CHECK IF THERE IS STILL MORE CONTENT -->
         <xsl:if test="substring-before($rest,' ')!=''">
            <!-- IF THERE IS, ADD A SPACE TO SEPERATE IT FROM THE NEXT COORDS -->
            <xsl:value-of select="' '"/>
         </xsl:if>
         
         <!-- SEND THE 'REST' BACK INTO THIS FUNCTION (RECURSION)
             TO BE PROCESSED THE SAME WAY -->
         <xsl:call-template name="parseCoord">
            <xsl:with-param name="coordinates" select="$rest"/>
         </xsl:call-template>
        
      </xsl:if>
   </xsl:template>
   
   
   <!-- ====================================================== -->
</xsl:stylesheet>