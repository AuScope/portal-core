<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:wfs="http://www.opengis.net/wfs"      
        xmlns:wfs_2="http://www.opengis.net/wfs/2.0"  
        xmlns:mo="http://xmlns.geoscience.gov.au/minoccml/1.0" 
        xmlns:mt="http://xmlns.geoscience.gov.au/mineraltenementml/1.0"
        xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"
        xmlns:gsmlp="http://xmlns.geosciml.org/geosciml-portrayal/4.0"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:gml="http://www.opengis.net/gml"
        xmlns:sa="http://www.opengis.net/sampling/1.0"
        exclude-result-prefixes="gml wfs wfs_2 gsml xlink mo mt gsmlp sa">
    <!-- ERML Namespace URI -->
    <xsl:param name="er" required="yes" static="yes"/>  
    <xsl:param name="portalBaseURL"/>  
    
    <xsl:variable name="erl" select="'http://xmlns.earthresourceml.org/earthresourceml-lite'"/>
    
    <xsl:include href="ER1.xsl" use-when="$er='urn:cgi:xmlns:GGIC:EarthResource:1.1'"/>
    <xsl:include href="ER2.xsl" use-when="$er='http://xmlns.earthresourceml.org/EarthResource/2.0'"/>
    
    
    <!-- Global Variables -->
    <xsl:template match="/">
    <!--
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    -->

        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
                <style type="text/css">
                    body {
                        font-family: Arial;
                        font-size:x-small;
                    }
                    table {
                        border-collapse:collapse;
                        font-family: Arial;
                        font-size:small;
                        width:100%;
                    }
                    th {
                        /*border-top: 1px solid #C1DAD7;*/
                        text-align:right;
                        padding-right:10px;
                        color: #15428B;
                    }
                    tr.border {
                        /*border-top: 1px solid #ddecfe;*/
                        /*border-top: 1px solid #4682B4;*/
                        /*border-top: 1px solid #1E90FF;*/
                    }
                    td {
                        padding:1px 5px;
                        vertical-align:text-top;
                    }
                    td.caption {
                        font-weight: bold;
                        font-size:medium;
                        color: #4f94cd;
                    }
                    td.our_row a {
                        text-decoration: none;
                        color: #15428B;
                    }
                    td.header {
                        font-weight: bold;
                        color: #15428B;
                    }
                    /* rename row to our_row so bootstrap row styles doesn't interfere!*/
                    td.our_row {
                        border-top: 1px solid #4682B4;
                    }
                    td.col {
                    }
                    td.col_header {
                        color: Black;
                        font-weight: bold;
                    }
                    td.no_border {
                        border-top: none;
                    }
                    p.child_table {
                        text-decoration-line: underline;
                        font-weight: bold;
                        font-size:medium;
                        color: Black;
                    }

                </style>
                <title>AuScope Portal Project</title>
                <meta http-equiv="content-type" content="text/html; charset=utf-8" />
            </head>
            <body>
                <!-- Specific for ER1.xsl and ER2.xsl -->
                <xsl:call-template name="ERBody" />
                <!-- Everything else -->
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/mo:MinOccView |
                        wfs:FeatureCollection/gml:featureMember/mo:MinOccView | mo:MinOccView"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/mt:MineralTenement |
                        wfs:FeatureCollection/gml:featureMember/mt:MineralTenement | mt:MineralTenement"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/gsmlp:BoreholeView |
                        wfs:FeatureCollection/gml:featureMember/gsmlp:BoreholeView | gsmlp:BoreholeView"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/gsml:Borehole |
                        wfs:FeatureCollection/gml:featureMember/gsml:Borehole | gsml:Borehole"/>       
                <!-- EarthResourceML Lite. Unfortunately there's a mix of ERL1 and ERL2 in the data, so we can't specify the namespace -->
                <!-- ERL with WFS 1.1.0 -->
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/*:MineralOccurrenceView |
                        wfs:FeatureCollection/gml:featureMember/*:MineralOccurrenceView | *:MineralOccurrenceView"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/*:MineView |
                        wfs:FeatureCollection/gml:featureMember/*:MineView | *:MineView "/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/*:CommodityResourceView |
                        wfs:FeatureCollection/gml:featureMember/*:CommodityResourceView | *:CommodityResourceView"/>
                <!-- ERL with WFS 2.0 -->                   
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/*:MineralOccurrenceView"/>
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/*:CommodityResourceView"/>
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/*:MineView"/>
                
            </body>
        </html>
    </xsl:template>
        <!-- ================================================================= -->
    <!--    FUNCTION TO GET STRING AFTER LAST OCCURRENCE OF A SUBSTR       -->
    <!--    PARAM: input - string to search within                         -->
    <!--    PARAM: substr - last occurrence of str that we searching for   -->
    <!-- ================================================================= -->
    <xsl:template name="substring-after-last">
        <xsl:param name="input"/>
        <xsl:param name="substr"/>

        <!-- Extract the string which comes after the first occurence -->
        <xsl:variable name="temp" select="substring-after($input,$substr)"/>

        <xsl:choose>
            <xsl:when test="$substr and contains($temp,$substr)">
                <xsl:call-template name="substring-after-last">
                    <xsl:with-param name="input" select="$temp"/>
                    <xsl:with-param name="substr" select="$substr"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$temp"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- ================================================================= -->
    <!--    THIS FUNCTION CHECKS IF GIVEN STRING CONTAINS %25 ESCAPE       -->
    <!--    SEQUENCE. IF IT DOES, IT RETURNS UN-ESCAPED % CHARACTER        -->
    <!--    OTHERWISE RETURNS BACK UNMODIFIED INPUT STRING                 -->
    <!--    PARAM: value                                                   -->
    <!-- ================================================================= -->
    <xsl:template name="convert-escaped-percentage">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="$value and contains($value,'%25')">
                <xsl:value-of select="'%'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$value"/>
            </xsl:otherwise>
        </xsl:choose>
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
        <a href="{$real-url}" onclick="var w=window.open('{$real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;"><xsl:value-of select="$friendly-name"/></a>
    </xsl:template>

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
        <a href="wfsFeaturePopup.do?url={$real-url}" onclick="var w=window.open('{$portalBaseURL}/wfsFeaturePopup.do?url={$real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;"><xsl:value-of select="$friendly-name"/></a>
    </xsl:template>
    
    <!-- Adapted from "functx:camel-case-to-words" -->
    <xsl:template name="formatLabel">
        <xsl:param name="arg"/>
         <!-- separate camel case into words, capitalise first letter of the words -->
        <xsl:value-of select="concat(upper-case(substring($arg,1,1)),
             replace(substring($arg,2),'(\p{Lu})',
                        concat(' ', '$1')))"/>
    </xsl:template>
    <!-- Adapted from functx:substring-before-if-contains and camel-case-to-words -->
    <xsl:template name="formatURILabel">
        <xsl:param name="arg"/>
        <xsl:variable name="label" select="concat(upper-case(substring($arg,1,1)),
             replace(substring($arg,2),'(\p{Lu})',
                        concat(' ', '$1')))"/>
        <!-- Strip the URI suffix -->
        <xsl:value-of select="substring-before($label, '_uri')"/>
    </xsl:template>    
    <xsl:template name="formatSimpleFeatures">
        <xsl:param name="wfsUriList"/>
        <xsl:for-each select="./*">
            <xsl:choose>
                 <xsl:when test="contains($wfsUriList, local-name(.))">
                     <xsl:choose>
                         <xsl:when test="contains(name(.), '_uri')">
                             <tr>
                                 <td class="our_row header">
                                     <xsl:call-template name="formatURILabel">
                                         <xsl:with-param name="arg" select="local-name(.)"/>
                                     </xsl:call-template>
                                 </td>
                                 <td class="our_row" colspan="2">
                                     <xsl:call-template name="make-wfspopup-url">
                                         <xsl:with-param name="friendly-name" select="."/>
                                         <xsl:with-param name="real-url" select="."/>
                                     </xsl:call-template>
                                 </td>
                             </tr>
                         </xsl:when>
                         <xsl:otherwise>
                             <tr>
                                 <td class="our_row header">
                                     <xsl:call-template name="formatLabel">
                                         <xsl:with-param name="arg" select="local-name(.)"/>
                                     </xsl:call-template>
                                 </td>
                                 <td class="our_row" colspan="2">
                                     <xsl:call-template name="make-wfspopup-url">
                                         <xsl:with-param name="friendly-name" select="."/>
                                         <xsl:with-param name="real-url" select="."/>
                                    </xsl:call-template>
                                </td>
                             </tr>
                         </xsl:otherwise>
                     </xsl:choose>                            
                 </xsl:when>
                 <xsl:otherwise> 
                     <xsl:choose>
                         <xsl:when test="contains(name(.), '_uri')">
                             <tr>
                                 <td class="our_row header">
                                     <xsl:call-template name="formatURILabel">
                                         <xsl:with-param name="arg" select="local-name(.)"/>
                                     </xsl:call-template>
                                 </td>
                                 <td class="our_row" colspan="2">
                                     <xsl:call-template name="make-popup-url">
                                         <xsl:with-param name="friendly-name" select="."/>
                                         <xsl:with-param name="real-url" select="."/>
                                     </xsl:call-template>
                                 </td>
                             </tr>
                         </xsl:when>
                         <xsl:otherwise>                                
                             <tr>
                                 <td class="our_row header">
                                     <xsl:call-template name="formatLabel">
                                         <xsl:with-param name="arg" select="local-name(.)"/>
                                     </xsl:call-template>
                                 </td>
                                 <td class="our_row"><xsl:value-of select="."/></td>
                             </tr>
                         </xsl:otherwise>
                     </xsl:choose>
                 </xsl:otherwise>
             </xsl:choose>
         </xsl:for-each>
     </xsl:template>
        <!-- Mineral Occurrence View -->
    <!-- =============================================================== -->
    <xsl:template match="mo:MinOccView">        
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">Mineral Occurrence Portrayal View</td>
                </tr>
                <xsl:call-template name="formatSimpleFeatures">
                    <xsl:with-param name="wfsUriList" select="concat('earthResourceSpecification_uri ', 'identifier')" />
                </xsl:call-template>                
            </tbody>
        </table>
    </xsl:template>
    
    <!-- Mineral Tenement -->
    <!-- =============================================================== -->
    <xsl:template match="mt:MineralTenement">        
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">Mineral Tenement</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'MineralTenementML'"/>
                            <xsl:with-param name="real-url" select="./*:identifier"/>
                        </xsl:call-template>
                    </td> 
                </tr>
                <xsl:call-template name="formatSimpleFeatures">
                    <xsl:with-param name="wfsUriList" select="'identifier'" />
                </xsl:call-template>                
            </tbody>
        </table>
    </xsl:template>
    
    <!-- Borehole and BoreholeView -->
    <!-- =============================================================== -->
    <xsl:template match="gsmlp:BoreholeView">        
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">Borehole Portrayal View</td>
                </tr>
                <tr>
                    <td class="our_row header">Identifier:</td>
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./gsmlp:identifier"/>
                            <xsl:with-param name="real-url" select="./gsmlp:identifier"/>
                        </xsl:call-template>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Name:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:name"/>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Drilling Method:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:drillingMethod"/>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Operator:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:operator"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Driller:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:driller"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Drill Start Date:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:drillStartDate"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Drill End Date:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:drillEndDate"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Start Point:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:startPoint"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Inclination Type:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:inclinationType"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Borehole Material Custodian:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:boreholeCustodianMaterial"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Borehole Length:</td>
                    <td class="our_row">
                        <xsl:if test="./gsmlp:boreholeLength_m">                            
                            <xsl:value-of select="concat(./gsmlp:boreholeLength_m, ' (m)')"/>
                        </xsl:if>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Elevation SRS:</td>
                    <td class="our_row">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./gsmlp:elevation_srs"/>
                            <xsl:with-param name="real-url" select="./gsmlp:elevation_srs"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Metadata:</td>
                    <td class="our_row">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./gsmlp:metadata_uri"/>
                            <xsl:with-param name="real-url" select="./gsmlp:metadata_uri"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Shape:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:shape"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">NVCL Collection:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:nvclCollection"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Project:</td>
                    <td class="our_row">
                        <xsl:value-of select="./gsmlp:project"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="gsml:Borehole">        
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">Borehole</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'GeoSciML'"/>
                            <xsl:with-param name="real-url" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                        </xsl:call-template>
                    </td> 
                </tr>
                <tr>
                    <td class="our_row header">Name:</td>
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                            <xsl:with-param name="real-url" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                        </xsl:call-template>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Local Name:</td>
                    <td class="our_row"><xsl:value-of select="./gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]"/></td>
                 </tr>
                 <tr>
                    <td class="our_row header">Sampled Feature:</td>
                    <td class="our_row">
                        <xsl:value-of select="./sa:sampledFeature/@xlink:title"/>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Shape:</td>
                    <td class="our_row">
                        <xsl:value-of select="./sa:shape"/>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Collar Location:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:collarLocation/gsml:BoreholeCollar/gsml:location"/></td>
                 </tr>
                 <tr>
                    <td class="our_row header">Collar Elevation:</td>
                    <td class="our_row">
                        <xsl:if test="./gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation">                            
                            <xsl:value-of select="concat(./gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation, ' ',
                                 ./gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation/@uomLabels)"/>
                        </xsl:if>
                    </td>
                 </tr>
                 <tr>
                    <td class="our_row header">Driller:</td>  
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:driller/@xlink:title"/></td>
                 </tr>
                 <tr>          
                    <td class="our_row header">Date of Drilling:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling"/></td>
                 </tr>
                 <tr>
                    <td class="our_row header">Drilling Method:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod"/></td>
                 </tr>
                 <tr>
                    <td class="our_row header">Start Point:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:startPoint"/></td>
                 </tr>
                 <tr>
                    <td class="our_row header">Inclination Type:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType"/></td>                    
                 </tr>
                 <tr>
                     <td class="our_row header">Cored Interval (Upper Corner):</td>
                     <td class="our_row">
                        <xsl:if test="./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:upperCorner">                            
                            <xsl:value-of select="concat(./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:upperCorner, 
                                ' ', ./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/@uomLabels)"/>
                        </xsl:if>
                     </td>
                 </tr>
                 <tr>
                     <td class="our_row header">Cored Interval (Lower Corner):</td>
                     <td class="our_row">
                        <xsl:if test="./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:lowerCorner">                            
                            <xsl:value-of select="concat(./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:lowerCorner,
                                ' ', ./gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/@uomLabels)"/>
                        </xsl:if>
                     </td>
                 </tr>
                 <tr>   
                    <td class="our_row header">Core Custodian:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title"/></td>
                 </tr>  
            </tbody>
        </table>
    </xsl:template>
         
    <!--  Earth ResourceML Lite -->
    <!-- =============================================================== -->
    <xsl:template match="*:MineralOccurrenceView[starts-with(namespace-uri(), $erl)]">
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML Lite - MineralOccurrenceView</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML Lite'"/>
                            <xsl:with-param name="real-url" select="./*:identifier"/>
                        </xsl:call-template>
                    </td> 
                </tr>                  
                <xsl:call-template name="formatSimpleFeatures">
                    <xsl:with-param name="wfsUriList" select="concat('mine_uri ', 'specification_uri ', 'identifier')" />
                </xsl:call-template>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="*:MineView[starts-with(namespace-uri(), $erl)]">
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML Lite - MineralOccurrenceView</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML Lite'"/>
                            <xsl:with-param name="real-url" select="./*:identifier"/>
                        </xsl:call-template>
                    </td> 
                </tr>                  
                <xsl:call-template name="formatSimpleFeatures">
                    <xsl:with-param name="wfsUriList" select="concat('specification_uri ', 'identifier')" />
                </xsl:call-template>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="*:CommodityResourceView[starts-with(namespace-uri(), $erl)]">
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML Lite - MineralOccurrenceView</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML Lite'"/>
                            <xsl:with-param name="real-url" select="./*:identifier"/>
                        </xsl:call-template>
                    </td> 
                </tr>                  
                <xsl:call-template name="formatSimpleFeatures">
                    <xsl:with-param name="wfsUriList" select="concat('mineralOccurrence_uri ', 'mine_uri ', 'specification_uri ', 'identifier')" />
                </xsl:call-template>
            </tbody>
        </table>
    </xsl:template>
 </xsl:stylesheet>