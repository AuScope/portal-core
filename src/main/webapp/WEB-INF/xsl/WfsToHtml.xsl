<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:er="urn:cgi:xmlns:GGIC:EarthResource:1.1"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:wfs="http://www.opengis.net/wfs"
                exclude-result-prefixes="er gml wfs fo gsml xlink">
    <xsl:output method="html" indent="yes"/>
    
    <!-- External Parameters -->
    <xsl:param name="serviceURL"/>

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
                        color: #FF6600;
                    }
                    td.header {
                        font-weight: bold;
                        color: #15428B;
                    }
                    td.row {
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
                </style>
                <title>AuScope Portal Project</title>
                <!-- 
                <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1" />
                -->
                <meta http-equiv="content-type" content="text/html; charset=utf-8" />
                

                <meta content="Jarek Sanders" name="author"/>
            </head>
            <body>
                <div>
                    <img alt="" src="img/img-auscope-banner.gif" style="border: 0px solid;" />
                </div>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMember/er:MineralOccurrence | er:MineralOccurrence"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMember/er:Mine | er:Mine"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMember/er:MiningActivity | er:MiningActivity"/>
                <xsl:apply-templates select="er:Commodity"/>
            </body>
        </html>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Mining Activity -->
    <!-- =============================================================== -->
    <xsl:template match="er:MiningActivity">
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - MiningActivity</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b><a href="#" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>                        
                    </td>
                </tr>
                <!-- Mining Activity Type -->
                <tr>
                    <td class="row header">Mining Activity Type</td>
                    <td class="row"><xsl:value-of select="./er:activityType"/></td>
                    <td class="row header">MiningActivity Id:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2141']"/></td>
                </tr>
                <!-- Start Date -->
                <tr>
                    <td class="row header">Start Date:</td>
                    <td class="row"><xsl:value-of select="./er:activityDuration/gml:TimePeriod/gml:begin"/></td>
                    <td class="row header">End Date:</td>
                    <td class="row"><xsl:value-of select="./er:activityDuration/gml:TimePeriod/gml:end"/></td>
                    <td class="row">&#160;</td>
                </tr>
                <!-- Associated Mine Name -->
                <tr>
                    <td class="row header">Associated Mine Name</td>
                    <td class="row"><xsl:value-of select="./er:associatedMine/er:mineName/er:MineName[./er:isPreferred = 'true']/er:mineName/text()"/></td>
                    <td class="row header">Associated Mine Id:</td>
                    <td class="row">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:associatedMine/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:associatedMine/@xlink:href,'=')"/></a>
                    </td>
                    <td class="row">&#160;</td>
                </tr>
                <!-- Amount of Ore Processed -->
                <tr>
                    <td class="row header">Amount of Ore Processed</td>
                    <td class="row"><xsl:value-of select="./er:oreProcessed"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:oreProcessed/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td class="row" colspan="3">&#160;</td>
                </tr>
                <!-- Commodity -->
                <tr>
                    <td class="row header">Commodity</td>
                    <td class="row"><xsl:value-of select="./er:producedMaterial/er:Product/er:sourceCommodity/er:commodityName"/></td>
                    <td class="row header">Commodity Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:producedMaterial/er:Product/er:sourceCommodity/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:producedMaterial/er:Product/er:sourceCommodity/@xlink:href,'=')"/></a>
                    </td>
                </tr>
                <!-- Product Name -->
        <xsl:for-each select="./er:producedMaterial">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row">&#160;</td>
                    <td class="row">&#160;</td>
                    <td class="row col_header">Production Amount</td>
                    <td class="row col_header">Recovery %</td>
                    <td class="row col_header">Grade</td>
                </tr>
                <tr>
                    <td class="header">Product Name</td>
                    <td><xsl:value-of select="./er:Product/er:productName"/></td>
                    <td><xsl:value-of select="./er:Product/er:production"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:Product/er:production/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:Product/er:recovery"/></td>
                    <td><xsl:value-of select="./er:Product/er:grade"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:Product/er:grade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>                          
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td class="row"><xsl:value-of select="./er:Product/er:productName"/></td>
                    <td class="row"><xsl:value-of select="./er:Product/er:production"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:Product/er:production/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td class="row"><xsl:value-of select="./er:Product/er:recovery"/></td>
                    <td class="row"><xsl:value-of select="./er:Product/er:grade"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:Product/er:grade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
                <!-- Raw Material -->
        <xsl:if test="./er:composition">
                <tr>
                    <td class="row header">Raw Material</td>
                    <td class="row col_header">Lithology</td>
                    <td class="row col_header">Role</td>
                    <td class="row col_header">Proportion</td>
                    <td class="row col_header">&#160;</td>
                </tr>
                <tr>
                    <td>&#160;</td>
                    <td ><xsl:value-of select="./er:composition/er:rawMaterial/er:material/gsml:RockMaterial/gsml:lithology"/></td>
                    <td ><xsl:value-of select="./er:composition/er:RawMaterial/er:rawMaterialRole"/></td>
                    <td ><xsl:value-of select="./er:composition/er:RawMaterial/er:proportion"/></td>
                    <td>&#160;</td>
                </tr>
        </xsl:if>
                <!-- Associated Earth Resources -->
                <tr>
                    <td class="row header">Associated Earth Resources</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Earth Resource Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:deposit/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:deposit/@xlink:href,'=')"/></a>
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Mine -->
    <!-- =============================================================== -->
    <xsl:template match="er:Mine">
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - Mine</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <a href="#" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>                        
                    </td>
                </tr>
                <!-- Mine Name -->
                <tr>
                    <td class="row header">Mine Name</td>
                    <td class="row"><xsl:value-of select="./er:mineName/er:MineName[./er:isPreferred = true()]/er:mineName/text()"/></td>
                    <td class="row header">Mine Id:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2141']"/></td>
                </tr>
                <!-- Alternative Mine Name -->
        <xsl:for-each select="./er:mineName/er:MineName[./er:isPreferred = 'false']">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Alternative Mine Name</td>
                    <td class="row"><xsl:value-of select="./er:mineName/text()"/></td>
                    <td class="row" colspan="3">&#160;</td>
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:mineName/text()"/></td>
                    <td colspan="3">&#160;</td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
                <!-- Status -->
                <tr>
                    <td class="row header">Status</td>
                    <td class="row" colspan="4"><xsl:value-of select="./er:status"/></td>
                </tr>
                <!-- Start Date -->
                <tr>
                    <td class="row header">Start Date</td>
                    <td class="row"><xsl:value-of select="er:startDate"/></td>
                    <td class="row header">End Date:</td>
                    <td class="row"><xsl:value-of select="er:endDate"/></td>
                    <td class="row">&#160;</td>
                </tr>
                <!-- Location -->
                <tr>
                    <td class="row header">Location</td>
                    <td class="row"><xsl:value-of select="substring-before(er:occurrence/er:MiningFeatureOccurrence/er:location/gml:Point/gml:pos,' ')"/></td>
                    <td class="row"><xsl:value-of select="substring-after(er:occurrence/er:MiningFeatureOccurrence/er:location/gml:Point/gml:pos,' ')"/></td>
                    <td class="row header">Datum:</td>
                    <td class="row"><xsl:value-of select="er:occurrence/er:MiningFeatureOccurrence/er:location/gml:Point/@srsName"/></td>
                </tr>
                <!-- Related Mining Activity -->
        <xsl:for-each select="./er:relatedActivity">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Related Mining Activity</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Mining Activity Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>                                        
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td class="row header">Mining Activity Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
                <!-- Related Mine -->
        <xsl:for-each select="./er:relatedMine">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Related Mine</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Mine Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>
                </tr>
                </xsl:when> 
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td class="row header">Mine Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Commodity -->
    <!-- =============================================================== -->
    <xsl:template match="er:Commodity">
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - Commodity</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <a href="#" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>                        
                    </td>
                </tr>
                <!-- Commodity -->
                <tr>
                    <td class="row header">Commodity</td>
                    <td class="row"><xsl:value-of select="er:commodityName"/></td>
                    <td class="row header">Commodity Id:</td>
                    <td class="row" colspan="1"><xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2141']"/></td>
                </tr>
                <tr>
                    <td class="row header">Importance</td>
                    <td class="row" ><xsl:value-of select="./er:commodityImportance"/></td>
                    <td class="row header">Rank:</td>
                    <td class="row" colspan="1"><xsl:value-of select="./er:commodityRank"/></td>
                </tr>
        <xsl:for-each select="./er:source">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Associated Earth Resources</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Earth Resource Id:</td>
                    <td class="row" colspan="1">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>
                </tr>
                </xsl:when> 
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td class="row header">Earth Resource Id:</td>
                    <td class="row" colspan="1">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>
                    </td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Mineral Occurrence -->
    <!-- =============================================================== -->
    <xsl:template match="er:MineralOccurrence">
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - MineralOccurrence</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <a href="#" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>                        
                    </td>
                </tr>
                <!-- Type -->
                <tr>
                    <td class="row header">Type</td>
                    <td class="row"><xsl:value-of select="er:type"/></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2141']"/></td>
                </tr>
                <!-- Mineral Occurrence Name -->
        <xsl:for-each select="./gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2141')]">
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Mineral Occurrence Name</td>
                    <td class="row"><xsl:value-of select="."/></td>
                    <td class="row">&#160;</td>
                    <td class="row">&#160;</td>
                    <td class="row">&#160;</td>
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td><xsl:value-of select="."/></td>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td>&#160;</td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
                <!-- Description -->
        <xsl:if test="gml:description">
                <tr>
                    <td class="row header" rowspan="1">Description</td>
                    <td class="row" rowspan="2" colspan="4"><xsl:value-of select="."/></td>
                </tr>
        </xsl:if>
                <!-- Commodity -->
        <xsl:for-each select="./er:commodityDescription">
            <xsl:variable name="comm_description" select="@xlink:href"></xsl:variable>
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Commodity</td>
                    <td class="row"><xsl:value-of select="./er:Commodity/er:commodityName"/></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>      
                    </td>
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td class="row"><xsl:value-of select="./er:Commodity/er:commodityName"/></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(@xlink:href,'=')"/></a>      
                    </td>
                </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
                <!-- Observation Method -->
                <tr>
                    <td class="row header">Observation Method</td>
                    <td class="row"><xsl:value-of select="./gsml:observationMethod/gsml:CGI_TermValue/gsml:value"/></td>
                    <td class="row header">Purpose:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./gsml:purpose"/></td>
                </tr>
                 <!-- Dimension -->
       <xsl:if test="./er:dimension">                                 
                <tr>
                    <td class="row header">Dimension</td>
                    <td class="row col_header">Area</td>
                    <td class="row col_header">Depth</td>
                    <td class="row col_header">Length</td>
                    <td class="row col_header">Width</td>
                </tr>
                <tr>
                    <td></td>                                                        
                    <td><xsl:value-of select="./er:dimension/er:EarthResourceDimension/er:area/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:dimension/er:EarthResourceDimension/er:area/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:dimension/er:EarthResourceDimension/er:depth/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:dimension/er:EarthResourceDimension/er:depth/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:dimension/er:EarthResourceDimension/er:length/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:dimension/er:EarthResourceDimension/er:length/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:dimension/er:EarthResourceDimension/er:width/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:dimension/er:EarthResourceDimension/er:width/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>                    
                </tr>
       </xsl:if>
                <!-- Appearance -->
       <xsl:if test="./er:form | ./er:expression | ./er:shape">
                <tr>
                    <td class="row header">Appearance</td>
                    <td class="row col_header">Form</td>
                    <td class="row col_header">Expression</td>
                    <td class="row col_header">Shape</td>
                    <td class="row col_header">&#160;</td>
                </tr>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:form/gsml:CGI_TermValue/gsml:value"/></td>
                    <td><xsl:value-of select="./er:expression/gsml:CGI_TermValue/gsml:value"/></td>
                    <td><xsl:value-of select="./er:shape/gsml:CGI_TermValue/gsml:value"/></td>
                    <td></td>
                </tr>
       </xsl:if>
                <!-- Linear Orientation -->
       <xsl:if test="./er:linearOrientation">
                <tr>
                    <td class="row header">Linear Orientation</td>
                    <td class="row col_header">Plunge</td>
                    <td class="row col_header">Trend</td>
                    <td class="row col_header">&#160;</td>
                    <td class="row col_header">&#160;</td>
                </tr>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:linearOrientation/gsml:CGI_LinearOrientation/gsml:plungeValue"/></td>
                    <td><xsl:value-of select="./er:linearOrientation/gsml:CGI_LinearOrientation/gsml:trend"/></td>
                    <td></td>
                    <td></td>
                </tr>
       </xsl:if>
                <!-- Planar Orientation -->
       <xsl:if test="./er:planarOrientation">
                <tr>
                    <td class="row header">Planar Orientation</td>
                    <td class="row col_header">Convention</td>
                    <td class="row col_header">Azimuth</td>
                    <td class="row col_header">Dip</td>
                    <td class="row col_header">&#160;</td>
                </tr>
                <tr>
                    <td></td>                                                        
                    <td><xsl:value-of select="./er:planarOrientation/gsml:CGI_PlanarOrientation/gsml:convention"/></td>
                    <td><xsl:value-of select="./er:planarOrientation/gsml:CGI_PlanarOrientation/gsml:azimuth/gsml:CGI_NumericValue/gsml:principalValue"/></td>
                    <td><xsl:value-of select="./er:planarOrientation/gsml:CGI_PlanarOrientation/gsml:dip/gsml:CGI_NumericValue/gsml:principalValue"/></td>
                    <td></td>
                </tr>
       </xsl:if>
                <!-- Classification -->
       <xsl:if test="./er:classification">
                <tr>
                    <td class="row header">Classification</td>
                    <td class="row col_header">Deposit Group</td>
                    <td class="row col_header">&#160;</td>
                    <td class="row col_header">Deposit Type</td>
                    <td class="row col_header">&#160;</td>
                </tr>
                <tr>
                    <td></td>                                            
                    <td><xsl:value-of select="./er:classification/er:MineralDepositModel/er:mineralDepositGroup"/></td>
                    <td></td>
                    <td><xsl:value-of select="./er:classification/er:MineralDepositModel/er:mineralDepositType"/></td>
                    <td></td>
                </tr>
       </xsl:if>
                <!-- Supergene Modification -->
       <xsl:if test="./er:supergeneModification">
                <tr>
                    <td class="row header">Supergene Modification</td>
                    <td class="col_header">Lithology</td>
                    <td class="col_header">Supergene Type</td>
                    <td class="col_header">Depth</td>
                    <td class="col_header"></td>
                </tr>
                <tr>
                    <td></td>
                    <td></td>                                          
                    <!-- <td><xsl:value-of select="./er:supergeneModification/er:SupergeneProcesses/er:material/gsml:lithology"/></td> -->
                    <td><xsl:value-of select="./er:supergeneModification/er:SupergeneProcesses/er:type/gsml:CGI_TermValue/gsml:value"/></td>
                    <td><xsl:value-of select="./er:supergeneModification/er:SupergeneProcesses/er:depth/gsml:CGI_Numeric/gsml:CGI_NumericValue/gsml:principalValue"/></td>                                        
                    <td></td>
                </tr>
       </xsl:if>
                <!-- Composition -->
       <xsl:if test="./er:composition">
                <tr>
                    <td class="row header">Composition</td>
                    <td class="row col_header">Lithology</td>
                    <td class="row col_header">Role</td>
                    <td class="row col_header">Proportion</td>
                    <td class="row col_header"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:composition/er:EarthResourceMaterial/er:material/gsml:RockMaterial/gsml:lithology"/></td>
                    <td><xsl:value-of select="./er:composition/er:EarthResourceMaterial/er:earthResourceMaterialRole"/></td>
                    <td><xsl:value-of select="./er:composition/er:EarthResourceMaterial/er:proportion/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:composition/er:EarthResourceMaterial/er:proportion/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td></td>
                </tr>
       </xsl:if>                
                <!-- Geological History -->
       <xsl:if test="./gsml:preferredAge/gsml:GeologicEvent">
                <tr>
                    <td class="row header">Geological History</td>
                    <td class="row col_header">Age</td>
                    <td class="row col_header">Process</td>
                    <td class="row col_header">Environment</td>
                    <td class="row col_header"></td>
                </tr>
            <xsl:for-each select="./gsml:preferredAge/gsml:GeologicEvent">
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./gsml:eventAge/gsml:CGI_NumericValue/gsml:principalValue"/></td>
                    <td><xsl:value-of select="./gsml:eventProcess/gsml:CGI_TermValue/gsml:value"/></td>
                    <td><xsl:value-of select="./gsml:eventEnvironment/gsml:CGI_TermValue/gsml:value"/></td>
                    <td></td>
                </tr>
            </xsl:for-each>
       </xsl:if>
                <!-- Reserves -->
        <xsl:for-each select="./er:oreAmount/er:Reserve">
                <tr>
                    <td class="row header">Reserves</td>
                    <td class="row"><xsl:value-of select="./er:ore"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>                    
                    <td class="row"><xsl:value-of select="./er:category"/></td>
                    <td class="row header">Date:</td>
                    <td class="row"><xsl:value-of select="./er:date"/></td>
                </tr>
            <xsl:for-each select="./er:measureDetails">
                <xsl:variable name="comm_description" select="/er:CommodityMeasure/er:commodityOfInterest/@xlink:href"></xsl:variable>
                    <tr>
                        <td class="col_header">Commodity</td>
                        <td class="row">
                            <xsl:choose>
                                <xsl:when test="./er:CommodityMeasure/er:commodityOfInterest/er:commodity/er:commodityName">
                                    <xsl:value-of select="./er:CommodityMeasure/er:commodityOfInterest/er:commodity/er:commodityName"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="substring-after-last">
                                        <xsl:with-param name="input" select="./er:CommodityMeasure/er:commodityOfInterest/@xlink:href" />
                                        <xsl:with-param name="substr" select="':'" />
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td class="row header">Commodity Id:</td>
                        <td class="row" colspan="2">
                            <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:CommodityMeasure/er:commodityOfInterest/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(.//er:CommodityMeasure/er:commodityOfInterest/@xlink:href,'=')"/></a>
                        </td>
                    </tr>
                <tr>
                    <td></td>
                    <td class="row col_header">Commodity Amount</td>
                    <td class="row col_header">Cut-off Grade</td>
                    <td class="row col_header">Grade</td>
                    <td class="row col_header">Importance</td>
                </tr>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:grade/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:CommodityMeasure/er:grade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:commodityOfInterest/er:commodityImportance"/></td>
                </tr>
            </xsl:for-each>
        </xsl:for-each>
                <!-- Resources -->
        <xsl:for-each select="./er:oreAmount/er:Resource">
                <tr>
                    <td class="row header">Resources</td>
                    <td class="row"><xsl:value-of select="./er:ore"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:ore/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td class="row"><xsl:value-of select="./er:category"/></td>
                    <td class="row header">Date:</td>
                    <td class="row"><xsl:value-of select="./er:date"/></td>
                </tr>
            <xsl:for-each select="./er:measureDetails">
                <xsl:variable name="comm_description" select="/er:CommodityMeasure/er:commodityOfInterest/@xlink:href"></xsl:variable>
                    <tr>
                        <td class="col_header" >Commodity:</td>
                        <td class="row">
                            <xsl:choose>
                                <xsl:when test="./er:CommodityMeasure/er:commodityOfInterest/er:commodity/er:commodityName">
                                    <xsl:value-of select="./er:CommodityMeasure/er:commodityOfInterest/er:commodity/er:commodityName"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="substring-after-last">
                                        <xsl:with-param name="input" select="./er:CommodityMeasure/er:commodityOfInterest/@xlink:href" />
                                        <xsl:with-param name="substr" select="':'" />
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td class="row header">Id:</td>
                        <td class="row" colspan="2">
                            <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:CommodityMeasure/er:commodityOfInterest/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(.//er:CommodityMeasure/er:commodityOfInterest/@xlink:href,'=')"/></a>
                        </td>
                    </tr>
                <tr>
                    <td></td>
                    <td class="row col_header">Commodity Amount</td>
                    <td class="row col_header">Cut-off Grade</td>
                    <td class="row col_header">Grade</td>
                    <td class="row col_header">Importance</td>
                </tr>
                <tr>
                    <td></td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:CommodityMeasure/er:commodityAmount/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:CommodityMeasure/er:cutOffGrade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:grade/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:CommodityMeasure/er:grade/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td><xsl:value-of select="./er:CommodityMeasure/er:commodityOfInterest/er:commodityImportance"/></td>
                </tr>
            </xsl:for-each>
        </xsl:for-each>
                <!-- Associated Mining Activity -->
       <xsl:if test="./er:resourceExtraction">
                <tr>
                    <td class="row header">Associated Mining Activity</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:resourceExtraction/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:resourceExtraction/@xlink:href,'=')"/></a>
                    </td>
                </tr>
       </xsl:if>
                <!-- Parent Earth Resources -->
       <xsl:if test="./er:parent">
                <tr>
                    <td class="row header">Parent Earth Resources</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:parent/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:parent/@xlink:href,'=')"/></a>
                    </td>
                </tr>
       </xsl:if>
                <!-- Child Earth Resources -->
       <xsl:if test="./er:child">
                <tr>
                    <td class="row header">Child Earth Resources</td>
                    <td class="row">&#160;</td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <a href="#" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:child/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:child/@xlink:href,'=')"/></a>
                    </td>
                </tr>
       </xsl:if>
            </tbody>
        </table>
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
   
</xsl:stylesheet>
