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
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:str="http://exslt.org/strings" exclude-result-prefixes="str">
    
    
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


   <!-- MATCH ROOT FEATURECOLLECTION -->
   <!-- ================================================================= -->
   <xsl:template match="wfs:FeatureCollection">
       
      <!-- <kml xmlns="http://www.opengis.net/kml/2.2"> -->
      <kml>
         <Document>
            <!-- STANDARD NAME AND DESCRIPTION FOR CONVERTED FILE -->
            <name>
               <xsl:text>GML Links to KML</xsl:text>
            </name>
            
            <description>
               <xsl:text>GeoSciML data converted to KML</xsl:text>
            </description>

            <xsl:apply-templates select="gml:featureMember/mo:Mine"/>
            <xsl:apply-templates select="gml:featureMember/mo:MiningActivity"/>
            <xsl:apply-templates select="gml:featureMember/mo:MiningFeatureOccurrence"/>
            <xsl:apply-templates select="gml:featureMember/mo:MineralOccurrence"/>
            <xsl:apply-templates select="gml:featureMember/geodesy:stations"/>
            <xsl:apply-templates select="gml:featureMember/gsml:GeologicUnit"/>
            <xsl:apply-templates select="gml:featureMember/gsml:MappedFeature"/>
            <xsl:apply-templates select="gml:featureMember/gsml:ShearDisplacementStructure"/>
            
            <xsl:apply-templates select="gml:featureMembers/gsml:Borehole"/>
            <xsl:apply-templates select="gml:featureMembers/sa:SamplingPoint"/>
         </Document>
      </kml>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING Mine -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/mo:Mine">
   
      <xsl:variable name="coordinates">
         <xsl:value-of select="./mo:occurrence/mo:MiningFeatureOccurrence/mo:location/gml:Point/gml:pos"/>
      </xsl:variable>
      <xsl:variable name="mineName">
         <xsl:value-of select="./mo:mineName/mo:MineName[./mo:isPreferred = true()]/mo:mineName/text()"/>
      </xsl:variable>
      
      <xsl:if test="$coordinates">  
         <Placemark>
            <name><xsl:value-of select="$mineName"/></name>
            <description>
               <![CDATA[</br><table border="1" cellspacing="1" width="100%">
               <tr><td>ID</td><td>]]><xsl:value-of select="./gml:name[starts-with(@codeSpace,'http://')]"/>
               <![CDATA[</td></tr><tr><td>Preferred Name</td><td>]]><xsl:value-of select="$mineName"/>
               <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="$coordinates"/>
               <![CDATA[</td></tr><tr><td>Status</td><td>]]><xsl:value-of select="./mo:status"/>
               <![CDATA[</td></tr></table>]]>
            </description>
            <Point>
               <Style>
                  <IconStyle>
                     <Icon><href>http://maps.google.com/mapfiles/kml/paddle/pink-blank.png</href></Icon>
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
      </xsl:if>
   
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING Maining Activity -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/mo:MiningActivity">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./mo:occurrence/mo:MiningFeatureOccurrence/mo:location/gml:Point/gml:pos"/>
      </xsl:variable>

      <xsl:if test="$coordinates">  
         <Placemark>
            <name><xsl:value-of select="@gml:id"/></name>
            <description>
               <![CDATA[</br><table border="1" cellspacing="1" width="100%">
               <tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
               <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="$coordinates"/>
               <![CDATA[</td></tr><tr><td>Acitivity Start Date</td><td>]]><xsl:value-of select="./mo:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition"/>
               <![CDATA[</td></tr><tr><td>Acitivity End Date</td><td>]]><xsl:value-of select="./mo:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition"/>
               <![CDATA[</td></tr><tr><td>Activity Type</td><td>]]><xsl:value-of select="./mo:activityType"/>            
               <![CDATA[</td></tr><tr><td>Associated Mine</td><td>]]><xsl:value-of select="./mo:associatedMine"/>
               <![CDATA[</td></tr><tr><td>Product</td><td>]]><xsl:value-of select="./mo:producedMaterial/mo:Product/mo:productName/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr></table>]]>
            </description>
            <Point>
               <Style>
                  <IconStyle>
                     <Icon><href>http://maps.google.com/mapfiles/kml/paddle/pink-blank.png</href></Icon>
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
      </xsl:if>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING Mining Feature Occurence -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/mo:MiningFeatureOccurrence">
   
      <xsl:variable name="coordinates">
         <xsl:value-of select="./mo:location/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./gml:name"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Description</td><td>]]><xsl:value-of select="./gml:description"/>
            <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="$coordinates"/>            
            <![CDATA[</td></tr></table>]]>            
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon>
               </IconStyle>
            </Style>
      
            <coordinates>
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
      </Placemark>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING Mineral Occurence -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/mo:MineralOccurrence">
   
      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:occurrence/gsml:MappedFeature/gsml:shape/gml:Point/gml:pos"/>
      </xsl:variable>
      <Placemark>
         <name><xsl:value-of select="./gml:name"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Type</td><td>]]><xsl:value-of select="./mo:type"/>
            <![CDATA[</td></tr><tr><td>Mineral Deposit Group</td><td>]]><xsl:value-of select="./mo:classification/mo:MineralDepositModel/mo:mineralDepositGroup"/>
            <![CDATA[</td></tr><tr><td>Ore Amount: Resource</td><td>]]><xsl:value-of select="./mo:oreAmount/mo:Resource/mo:measureDetails/mo:CommodityMeasure/mo:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue"/>
            <![CDATA[</td></tr><tr><td>Ore Amount: Reserve</td><td>]]><xsl:value-of select="./mo:oreAmount/mo:Reserve/mo:measureDetails/mo:CommodityMeasure/mo:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue"/>           
            <![CDATA[</td></tr></table>]]>            
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-blank.png</href></Icon>
               </IconStyle>
            </Style>
      
            <coordinates>
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
      </Placemark>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING GPS -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/geodesy:stations">
   
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
            <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="$coordinates"/>            
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
   
   
   <!-- TEMPLATE FOR TRANSLATING Geologic Unit -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:GeologicUnit">
   
         <!--
         <xsl:value-of select="local-name()"/>  
         <xsl:value-of select="namespace-uri()"/>  
         -->
      <xsl:if test="gsml:occurrence" >
         <Placemark>
            <name><xsl:value-of select="./gml:name[starts-with(@codeSpace,'http://')]"/></name>
            <description>
            <!--
               <![CDATA[</br><table border="1" cellspacing="1" width="100%">
               <tr><td>GeologicUnit</td><td>]]><xsl:value-of select="./gml:name[starts-with(@codeSpace,'http://')]"/>
               <![CDATA[</td></tr><tr><td>Name</td><td>]]><xsl:value-of select="./gml:description"/>
               <![CDATA[</td></tr><tr><td>Type</td><td>]]><xsl:value-of select="./gsml:geologicUnitType"/>
               --><!-- To DO: If no age then use preferedAge 
               <![CDATA[</td></tr><tr><td>Age</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventAge/gsml:CGI_TermRange/gsml:lower/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Environment</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventEnvironment/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Process</td><td>]]><xsl:value-of select="./gsml:geologicHistory/gsml:GeologicEvent/gsml:eventProcess/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr><tr><td>Lithology</td><td>]]><xsl:value-of select="./gsml:composition/gsml:CompositionPart/gsml:lithology"/>
               <![CDATA[</td></tr></table>]]>
               -->
            </description>

            <xsl:apply-templates select="./gsml:occurrence//gsml:shape"/>

         </Placemark>
      </xsl:if>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING MAPPED FEATURE -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:MappedFeature">

      <Placemark>
         <name><xsl:value-of select="@gml:id"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Description</td><td>]]><xsl:value-of select="./gml:description"/>            
            <![CDATA[</td></tr></table>]]>
         </description>

         <xsl:apply-templates select="gsml:shape"/>
         
      </Placemark>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING GU ShearDisplacementStructure -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMember/gsml:ShearDisplacementStructure">
   
      <xsl:variable name="coordinates">
         <xsl:value-of select="./mo:occurrence/mo:MiningFeatureOccurrence/mo:location/gml:Point/gml:pos"/>
      </xsl:variable>

         <Placemark>
            <name><xsl:value-of select="@gml:id"/></name>
            <description>
               <![CDATA[</br><table border="1" cellspacing="1" width="100%">
               <tr><td>Id</td><td>]]><xsl:value-of select="./gml:name[2]"/>
               <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="$coordinates"/>
               <![CDATA[</td></tr><tr><td>Acitivity Start Date</td><td>]]><xsl:value-of select="./mo:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition"/>
               <![CDATA[</td></tr><tr><td>Acitivity End Date</td><td>]]><xsl:value-of select="./mo:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition"/>
               <![CDATA[</td></tr><tr><td>Activity Type</td><td>]]><xsl:value-of select="./mo:activityType"/>            
               <![CDATA[</td></tr><tr><td>Associated Mine</td><td>]]><xsl:value-of select="./mo:associatedMine"/>
               <![CDATA[</td></tr><tr><td>Product</td><td>]]><xsl:value-of select="./mo:producedMaterial/mo:Product/mo:productName/gsml:CGI_TermValue/gsml:value"/>
               <![CDATA[</td></tr></table>]]>
            </description>
            <Point>
               <Style>
                  <IconStyle>
                     <Icon><href>http://maps.google.com/mapfiles/kml/paddle/pink-blank.png</href></Icon>
                  </IconStyle>
               </Style>
              
               <coordinates>
                  <xsl:call-template name="parseCoord">
                     <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                     <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
                  </xsl:call-template>
               </coordinates>
            </Point>
         </Placemark>
   </xsl:template>
   
   
   <!-- TEMPLATE FOR TRANSLATING NVCL -->
   <!-- ================================================================= -->
   <xsl:template match="gml:featureMembers/gsml:Borehole">

      <xsl:variable name="coordinates">
         <xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
      </xsl:variable>
      
      <Placemark>
         <name><xsl:value-of select="@gml:id"/></name>
         <description>
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Borehole Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location/gml:Point/gml:pos"/>
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
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
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
            <![CDATA[</br><table border="1" cellspacing="1" width="100%">
            <tr><td>Name</td><td>]]><xsl:value-of select="./gml:name"/>
            <![CDATA[</td></tr><tr><td>Lng Lat (deg)</td><td>]]><xsl:value-of select="./sa:position/gml:Point/gml:pos"/>            
            <![CDATA[</td></tr></table>]]>
         </description>
         <Point>
            <Style>
               <IconStyle>
                  <Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-blank.png</href></Icon>
               </IconStyle>
            </Style>

            <coordinates>
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </Point>
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
            <xsl:call-template name="parseCoord">
               <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
               <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
            </xsl:call-template>
         </coordinates>
      </Point>
   </xsl:template>
   
   
   <!-- ================================================================= -->
   <xsl:template match="gml:Polygon">
      <Polygon>
         <xsl:apply-templates select="gml:exterior"/>
         <xsl:apply-templates select="gml:interior"/>
      </Polygon>
   </xsl:template>
   
   
   <!-- ================================================================= -->
   <xsl:template match="gml:MultiCurve">
   
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
            <xsl:call-template name="parseCoord">
               <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
               <xsl:with-param name="coordinates" select="concat($coordinates,' ')"/>
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
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
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
               <xsl:call-template name="parseCoord">
                  <!-- ATTACH AN EXTRA SPACE TO THE END OF THE COORDINATES -->
                  <xsl:with-param name="coordinates" select="concat($int_coordinates,' ')"/>
               </xsl:call-template>
            </coordinates>
         </LinearRing>
      </innerBoundaryIs>
   </xsl:template>
   
   
   <!-- ================================================================= -->
   <!--    FUNCTION TO TRANSLATE X Y COORDS TO X,Y,0                      -->
   <!--    KML format: longitude,latitude,altitude (in that order)        -->
   <!-- ================================================================= -->
   <xsl:template name="parseCoord">
   
      <xsl:param name="coordinates"/>
      <!-- CHECK IF THERE IS CONTENT BEFORE THE FIRST SPACE YOU REACH -->
      <xsl:if test="substring-before($coordinates,' ')!=''">

         <!-- DIVIDE THE SET OF COORDINATES INTO TWO SECTIONS,
              BEFORE AND AFTER THE FIRST SPACE (CURRENT AND REST) -->
         <xsl:variable name="longitude">
            <xsl:value-of select="substring-before($coordinates,' ')"/>
         </xsl:variable>
         <xsl:variable name="rest">
            <xsl:value-of select="substring-after($coordinates,' ')"/>
         </xsl:variable>
         <xsl:variable name="latitude">
            <xsl:value-of select="substring-before($rest,' ')"/>
         </xsl:variable>
         <xsl:variable name="rest1">
            <xsl:value-of select="substring-after($rest,' ')"/>
         </xsl:variable>
         
         <!-- APPEND A "," to lon AND A ",0" (0 VALUE) TO LAT AND PRINT IT OUT -->
         <xsl:value-of select="concat($longitude,',')"/>
         <xsl:value-of select="concat($latitude,',0')"/>
         
         <!-- CHECK IF THERE IS STILL MORE CONTENT -->
         <xsl:if test="substring-before($rest1,' ')!=''">
            <!-- IF THERE IS, ADD A SPACE TO SEPERATE IT FROM THE NEXT COORDS -->
            <xsl:value-of select="' '"/>
         </xsl:if>
         
         <!-- SEND THE 'REST' BACK INTO THIS FUNCTION (RECURSION)
             TO BE PROCESSED THE SAME WAY -->
         <xsl:call-template name="parseCoord">
            <xsl:with-param name="coordinates" select="$rest1"/>
         </xsl:call-template>
        
      </xsl:if>
   </xsl:template>
   
   
   <!-- ================================================================= 
   <xsl:template name="parseCoordinates">
      <xsl:param name="coords"/>
      
      <xsl:variable name="tokens" select="tokenize($coords, ' ')"/>
      
      <xsl:for-each select="$tokens">
         <xsl:variable name="pos" select="position()"/>
         <xsl:if test="$pos mod 2 != 0">
            <xsl:value-of select="concat(., ',', $tokens[$pos + 1], ',0 ')" />
         </xsl:if>
      </xsl:for-each>
      
   </xsl:template>
   -->
   
   <!-- ================================================================= -->
</xsl:stylesheet>