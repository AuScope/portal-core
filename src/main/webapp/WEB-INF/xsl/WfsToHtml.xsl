<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
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
    <xsl:param name="vocabserviceURL"/>
    <xsl:param name="vocabservice-reponame"/>
    <xsl:variable name="vocab-hard-coded-lookup" select="concat('http://services-test.auscope.org/SISSVoc/getConceptByURI?commodity_vocab/', '')"/>
    <xsl:variable name="vocab-hard-coded-lookupCGI" select="concat('http://services-test.auscope.org/SISSVoc/getConceptByURI?CGI/', '')"/>
    
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
                <meta http-equiv="content-type" content="text/html; charset=utf-8" />
            </head>
            <body>
                <div>
                    <img alt="" src="img/img-auscope-banner.gif" style="border: 0px solid;" />
                </div>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:MineralOccurrence | er:MineralOccurrence"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:Mine | er:Mine"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:MiningActivity | er:MiningActivity"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:Commodity | er:Commodity"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:MiningFeatureOccurrence | er:MiningFeatureOccurrence"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/gsml:GeologicUnit | gsml:GeologicUnit"/>
            </body>
        </html>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Mining Activity -->
    <!-- =============================================================== -->
    <xsl:template match="er:MiningActivity">
        <xsl:variable name="miningActivityID" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
        <xsl:variable name="substring" select="substring(./er:producedMaterial/er:Product/er:sourceCommodity/@xlink:href, 2)"/>
        <xsl:variable name="commodity" select="//*[@gml:id=$substring]"/>



        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - MiningActivity</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML'"/>
                            <xsl:with-param name="real-url" select="$miningActivityID"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <!-- Mining Activity Type -->
                <tr>
                    <td class="row header">Mining Activity Type</td>
                    <td class="row"><xsl:value-of select="./er:activityType"/></td>
                    <td class="row header">MiningActivity Id:</td>
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                        <xsl:with-param name="friendly-name" select="$miningActivityID"/>
                        <xsl:with-param name="real-url" select="$miningActivityID"/>
                    </xsl:call-template></td>
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
                    <td class="row header">Associated Mine</td>
                    <td class="row"></td>
                    <td class="row header">Associated Mine Id:</td>
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                        <xsl:with-param name="friendly-name" select="./er:associatedMine/@xlink:href"/>
                        <xsl:with-param name="real-url" select="./er:associatedMine/@xlink:href"/>
                    </xsl:call-template></td>
                </tr>
                <!-- Amount of Ore Processed -->
                <tr>
                    <td class="row header">Amount of Ore Processed</td>
                    <td class="row"><xsl:value-of select="./er:oreProcessed"/><xsl:value-of select="' '"/><xsl:value-of select="substring-after(./er:oreProcessed/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/></td>
                    <td class="row" colspan="3">&#160;</td>
                </tr>
                <!-- Commodity -->
                <xsl:for-each select="./er:producedMaterial/er:Product/er:sourceCommodity">
                
                    <xsl:variable name="commodityName">
			            <xsl:choose>
			                <xsl:when test="exists(./er:Commodity/er:commodityName)">
			                    <xsl:value-of select="./er:Commodity/er:commodityName" />
			                </xsl:when>
			                <xsl:when test="starts-with(@xlink:href, '#')">
			                    <xsl:value-of select="$commodity/er:commodityName" />
			                </xsl:when>
			                <xsl:otherwise>
			                    <xsl:value-of select="''" />
			                </xsl:otherwise>
			            </xsl:choose>
			        </xsl:variable>
			
			        <xsl:variable name="commodityID">
			            <xsl:choose>
			                <xsl:when test="exists(./er:Commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616'])">
			                    <xsl:value-of select="./er:Commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']" />
			                </xsl:when>
			                <xsl:when test="starts-with(@xlink:href, '#')">
			                    <xsl:value-of select="$commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']" />
			                </xsl:when>
			                <xsl:otherwise>
			                    <xsl:value-of select="@xlink:href" />
			                </xsl:otherwise>
			            </xsl:choose>
			        </xsl:variable>
                          
	                <tr>
	                    <td class="row header">Commodity</td>
	                    <td class="row"><xsl:value-of select="$commodityName"/></td>
	                    <td class="row header">Commodity Id:</td>
	                    <td class="row" colspan="2">
	                        <xsl:choose>
	                            <xsl:when test="starts-with($commodityID, 'http://')">
	                                <a href="wfsFeaturePopup.do?url={$commodityID}" onclick="var w=window.open('wfsFeaturePopup.do?url={$commodityID}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="$commodityID"/></a>
	                            </xsl:when>
	                            <xsl:otherwise>
	                                <xsl:value-of select="$commodityID"/>
	                            </xsl:otherwise>
	                        </xsl:choose>
	                    </td>
	                </tr>
                
                </xsl:for-each>
                
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
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                        <xsl:with-param name="friendly-name" select="./er:deposit/@xlink:href"/>
                        <xsl:with-param name="real-url" select="./er:deposit/@xlink:href"/>
                    </xsl:call-template></td>
                </tr>
            </tbody>
        </table>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Mine -->
    <!-- =============================================================== -->
    <xsl:template match="er:Mine">
        <xsl:variable name="mineID" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
        <xsl:variable name="minePrefName" select="./er:mineName/er:MineName[./er:isPreferred = true()]/er:mineName/text()" />
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - Mine</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML'"/>
                            <xsl:with-param name="real-url" select="$mineID"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <!-- Mine Name -->
                <tr>
                    <td class="row header">Mine Name</td>
                    <td class="row"><xsl:value-of select="$minePrefName"/></td>
                    <td class="row header">Mine Id:</td>
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$mineID"/>
                            <xsl:with-param name="real-url" select="$mineID"/>
                        </xsl:call-template></td>
                </tr>
                <!-- Alternative Mine Name -->
                <xsl:for-each select="./er:mineName/er:MineName[./er:isPreferred = 'false']">
                    <xsl:variable name="currentOtherName" select="./er:mineName/text()"/>
                    <xsl:choose>
                        <xsl:when test="position()=1">
                        <tr>
                            <td class="row header">Alternative Mine Name</td>
                            <td class="row"><xsl:value-of select="$currentOtherName"/></td>
                            <td class="row" colspan="3">&#160;</td>
                        </tr>
                        </xsl:when>
                        <xsl:otherwise>
                        <tr>
                            <td></td>
                            <td><xsl:value-of select="$currentOtherName"/></td>
                            <td colspan="3">&#160;</td>
                        </tr>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <!-- Status -->
                <tr>
                    <td class="row header">Status</td>
                    <td class="row"><xsl:value-of select="./er:status"/></td>
                    <td class="row header">Occurence</td>
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                        <xsl:with-param name="friendly-name" select="./er:occurrence/@xlink:href"/>
                        <xsl:with-param name="real-url" select="./er:occurrence/@xlink:href"/>
                    </xsl:call-template></td>


                </tr>
                <!-- Start Date -->
                <tr>
                    <td class="row header">Start Date</td>
                    <td class="row"><xsl:value-of select="./er:relatedActivity/er:MiningActivity/er:activityDuration/gml:TimePeriod/gml:begin"/></td>
                    <td class="row header">End Date:</td>
                    <td class="row"><xsl:value-of select="./er:relatedActivity/er:MiningActivity/er:activityDuration/gml:TimePeriod/gml:end"/></td>
                    <td class="row">&#160;</td>
                </tr>
                <xsl:for-each select="./er:relatedActivity/er:MiningActivity">
	                <!-- Related Mining Activity -->
                    <xsl:variable name="rel-mine-id" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
                    <xsl:choose>
                        <xsl:when test="position()=1">
                        <tr>
                            <td class="row header">Related Mining Activity</td>
                            <td class="row">&#160;</td>
                            <td class="row header">Mining Activity Id:</td>
                            <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                                    <xsl:with-param name="friendly-name" select="$rel-mine-id"/>
                                    <xsl:with-param name="real-url" select="$rel-mine-id"/>
                                </xsl:call-template></td>
                        </tr>
                        </xsl:when>
                        <xsl:otherwise>
                        <tr>
                            <td>&#160;</td>
                            <td>&#160;</td>
                            <td class="row header">Mining Activity Id:</td>
                            <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                                    <xsl:with-param name="friendly-name" select="$rel-mine-id"/>
                                    <xsl:with-param name="real-url" select="$rel-mine-id"/>
                                </xsl:call-template></td>
                        </tr>
                        </xsl:otherwise>
                    </xsl:choose>
	                <!-- Related Mine -->
                    <xsl:for-each select="./er:associatedMine">
	                    <xsl:variable name="rel-mine-id" select="@xlink:href"/>
	                    <xsl:choose>
	                        <xsl:when test="position()=1">
	                        <tr>
	                            <td class="row header">Related Mine</td>
	                            <td class="row">&#160;</td>
	                            <td class="row header">Mine Id:</td>
	                            <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
	                                    <xsl:with-param name="friendly-name" select="$rel-mine-id"/>
	                                    <xsl:with-param name="real-url" select="$rel-mine-id"/>
	                                </xsl:call-template></td>
	                        </tr>
	                        </xsl:when>
	                        <xsl:otherwise>
	                        <tr>
	                            <td>&#160;</td>
	                            <td>&#160;</td>
	                            <td class="row header">Mine Id:</td>
	                            <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
	                                    <xsl:with-param name="friendly-name" select="$rel-mine-id"/>
	                                    <xsl:with-param name="real-url" select="$rel-mine-id"/>
	                                </xsl:call-template></td>
	                        </tr>
	                        </xsl:otherwise>
	                    </xsl:choose>
	                </xsl:for-each>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>


    <!-- TEMPLATE FOR TRANSLATING Commodity -->
    <!-- =============================================================== -->
    <xsl:template match="er:Commodity">
        <xsl:variable name="commodity_id" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
        <xsl:variable name="commodity_name" select="./er:commodityName"/>
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - Commodity</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <a href="{$serviceURL}" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>
                    </td>
                </tr>
                <!-- Commodity -->
                <tr>
                    <td class="row header">Commodity</td>
                    <td class="row">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="$commodity_name"/>
                            <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookup,$commodity_name)"/>
                        </xsl:call-template>
                    </td>
                    <td class="row header">Commodity Id:</td>
                    <td class="row" colspan="1">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$commodity_id"/>
                            <xsl:with-param name="real-url" select="$commodity_id"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="row header">Local Name*</td>
                    <td class="row"><xsl:value-of select="./gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]"/></td>
                    <td class="row header">Local Group*</td>
                    <td class="row" colspan="1"><xsl:value-of select="./er:commodityGroup[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]"/></td>
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
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="@xlink:href"/>
                            <xsl:with-param name="real-url" select="@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td class="row header">Earth Resource Id:</td>
                    <td class="row" colspan="1">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="@xlink:href"/>
                            <xsl:with-param name="real-url" select="@xlink:href"/>
                        </xsl:call-template>
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
        <xsl:variable name="minocc_id">
            <xsl:value-of select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
        </xsl:variable>
        <table>
            <tbody>
                <tr>
                    <td class="caption" colspan="2" rowspan="1">EarthResourceML - MineralOccurrence</td>
                    <td>&#160;</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <a href="{$serviceURL}" onclick="var w=window.open('{$serviceURL}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;">EarthResourceML</a>
                    </td>
                </tr>
                <!-- Type -->
                <tr>
                    <td class="row header">Type</td>
                    <td class="row"><xsl:value-of select="er:type"/></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$minocc_id"/>
                            <xsl:with-param name="real-url" select="$minocc_id"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <!-- Mineral Occurrence Name -->
        <xsl:for-each select="./gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]">
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
            <xsl:variable name="comm_description">
                <xsl:value-of select="@xlink:href"/>
            </xsl:variable>
            <xsl:variable name="comm_name">
                <xsl:value-of select="./er:Commodity/er:commodityName"/>
            </xsl:variable>
            <xsl:variable name="comm_url">
                <xsl:value-of select="./er:Commodity/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="position()=1">
                <tr>
                    <td class="row header">Commodity</td>
                    <td class="row"><xsl:call-template name="make-popup-url">
                        <xsl:with-param name="friendly-name" select="$comm_name"/>
                        <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookup,$comm_name)"/>
                    </xsl:call-template></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$comm_url"/>
                            <xsl:with-param name="real-url" select="$comm_url"/>
                        </xsl:call-template>
                    </td>
                </tr>
                </xsl:when>
                <xsl:otherwise>
                <tr>
                    <td>&#160;</td>
                    <td class="row"><xsl:call-template name="make-popup-url">
                        <xsl:with-param name="friendly-name" select="$comm_name"/>
                        <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookup,$comm_name)"/>
                    </xsl:call-template></td>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$comm_url"/>
                            <xsl:with-param name="real-url" select="$comm_url"/>
                        </xsl:call-template>
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
                <!-- Composition
                Note: there are more than one type of thing in here. -->
       <xsl:if test="./er:composition">
           <xsl:for-each select="./er:composition">
                <tr>
                    <td class="row header">Composition</td>
	 			<xsl:if test="./er:EarthResourceMaterial/er:material/gsml:RockMaterial/gsml:lithology/@xlink:title">
                    <td class="row col_header">Lithology</td>
                </xsl:if>
	 			<xsl:if test="./er:EarthResourceMaterial/er:material/gsml:Mineral/gsml:mineralName/@xlink:title">
                    <td class="row col_header">Mineral Name</td>
                </xsl:if>

                    <td class="row col_header">Role</td>
                    <td class="row col_header">Proportion</td>
                    <td class="row col_header"></td>
                </tr>
                <tr>
                    <td></td>
                    <xsl:if test="./er:EarthResourceMaterial/er:material/gsml:RockMaterial/gsml:lithology/@xlink:title">
                        <td><xsl:value-of select="./er:EarthResourceMaterial/er:material/gsml:RockMaterial/gsml:lithology/@xlink:title"/></td>
                	</xsl:if>
	 				<xsl:if test="./er:EarthResourceMaterial/er:material/gsml:Mineral/gsml:mineralName/@xlink:title">
                    	<td><xsl:value-of select="./er:EarthResourceMaterial/er:material/gsml:Mineral/gsml:mineralName/@xlink:title"/></td>
                	</xsl:if>
                    <td><xsl:value-of select="./er:EarthResourceMaterial/er:earthResourceMaterialRole"/></td>
                    <td><xsl:value-of select="./er:EarthResourceMaterial/er:proportion/gsml:CGI_NumericValue/gsml:principalValue"/><xsl:value-of select="' '"/>
                        <xsl:call-template name="convert-escaped-percentage">
                            <xsl:with-param name="value" select="substring-after(./er:composition/er:EarthResourceMaterial/er:proportion/gsml:CGI_NumericValue/gsml:principalValue/@uom,'::')"/>
                        </xsl:call-template>
                    </td>
                    <td></td>
                </tr>
           </xsl:for-each>
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
                <xsl:variable name="comm_description" select="substring-after(./er:CommodityMeasure/er:commodityOfInterest/@xlink:href,'#')"></xsl:variable>
                <xsl:variable name="comm_org_name" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]"></xsl:variable>
                <xsl:variable name="comm_std_name" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/er:commodityName[@codeSpace='urn:cgi:classifierScheme:GA:commodity']"></xsl:variable>
                <xsl:variable name="comm_url" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"></xsl:variable>
                <tr>
                    <td class="col_header" >Commodity:</td>
                    <td class="row">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="$comm_std_name"/>
                            <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookup,$comm_std_name)"/>
                        </xsl:call-template>
                    </td>
                    <td class="row header">Commodity Id:</td>
                        <td class="row" colspan="2">
                            <xsl:call-template name="make-wfspopup-url">
                                <xsl:with-param name="friendly-name" select="$comm_description"/>
                                <xsl:with-param name="real-url" select="$comm_description"/>
                            </xsl:call-template>
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
                <xsl:variable name="comm_description" select="substring-after(./er:CommodityMeasure/er:commodityOfInterest/@xlink:href,'#')"></xsl:variable>
                <xsl:variable name="comm_org_name" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc2616')]"></xsl:variable>
                <xsl:variable name="comm_std_name" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/er:commodityName[@codeSpace='urn:cgi:classifierScheme:GA:commodity']"></xsl:variable>
                <xsl:variable name="comm_url" select="../../../er:commodityDescription/er:Commodity[@gml:id=concat($comm_description,'')]/gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"></xsl:variable>
                    <tr>
                        <td class="col_header" >Commodity:</td>
                        <td class="row">
                            <xsl:call-template name="make-popup-url">
                                <xsl:with-param name="friendly-name" select="$comm_std_name"/>
                                <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookup,$comm_std_name)"/>
                            </xsl:call-template>
                        </td>
                        <td class="row header">Id:</td>
                        <td class="row" colspan="2">
                            <xsl:call-template name="make-wfspopup-url">
                                <xsl:with-param name="friendly-name" select="$comm_url"/>
                                <xsl:with-param name="real-url" select="$comm_url"/>
                            </xsl:call-template>
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
                        <a href="wfsFeaturePopup.do?url={./er:resourceExtraction/@xlink:href}" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:resourceExtraction/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:resourceExtraction/@xlink:href,'=')"/></a>
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
                        <a href="wfsFeaturePopup.do?url={./er:parent/@xlink:href}" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:parent/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:parent/@xlink:href,'=')"/></a>
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
                        <a href="wfsFeaturePopup.do?url={./er:child/@xlink:href}" onclick="var w=window.open('wfsFeaturePopup.do?url={./er:child/@xlink:href}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=850');w.focus();return false;"><xsl:value-of select="substring-after(./er:child/@xlink:href,'=')"/></a>
                    </td>
                </tr>
       </xsl:if>
            </tbody>
        </table>
    </xsl:template>



    <!-- TEMPLATE FOR TRANSLATING MiningFeatureOccurrence -->
    <!-- =============================================================== -->
    <xsl:template match="er:MiningFeatureOccurrence">
        <xsl:variable name="mfoID" select="./gml:name[@codeSpace='http://www.ietf.org/rfc/rfc2616']"/>

        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - MiningFeatureOccurrence</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML'"/>
                            <xsl:with-param name="real-url" select="$serviceURL"/>
                        </xsl:call-template>
                    </td>
                </tr>

                <!-- MiningFeatureOccurrence Type -->
                <tr>
                    <td class="row header">Mining Feature Occurrence Description:</td>
                    <td class="row"><xsl:value-of select="./gml:description"/></td>
                    <td class="row header">Mining Feature Occurrence Id:</td>
                    <td class="row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$mfoID"/>
                            <xsl:with-param name="real-url" select="$mfoID"/>
                        </xsl:call-template></td>
                </tr>
                <!-- Observation Method -->
                <tr>
                    <td class="row header" colspan="2">Observation Method:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./er:observationMethod/gsml:CGI_TermValue/gsml:value"/></td>
                    <td class="row" colspan="1">&#160;</td>
                </tr>
                <!-- Positional Accuracy -->
                <tr>
                    <td class="row header" colspan="2">Positional Accuracy:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./er:positionalAccuracy/gsml:CGI_TermValue/gsml:value"/></td>
                    <td class="row" colspan="1">&#160;</td>
                </tr>
            </tbody>
        </table>&#160;

        <xsl:for-each select="./er:specification/er:Mine">
            <p></p>
            <table>
                <tbody>
                    <xsl:apply-templates select=". | er:Mine"/>
                </tbody>
            </table>&#160;
        </xsl:for-each>

        <xsl:for-each select="./er:specification/er:Mine/er:relatedActivity/er:MiningActivity">
		<p></p>
            <table>
                <tbody>
                    <xsl:apply-templates select=". | er:MiningActivity"/>
            	</tbody>
            </table>&#160;
        </xsl:for-each>
    </xsl:template>
    
    <!-- TEMPLATE FOR TRANSLATING Yilgarn Geochemistry -->
    <!-- =============================================================== -->
    <xsl:template match="gsml:GeologicUnit">
        <xsl:variable name="guID" select="@gml:id"/>
		<xsl:variable name="mappedFeatureObsMethod" select="./gsml:occurrence/gsml:MappedFeature/gsml:observationMethod/gsml:CGI_TermValue/gsml:value[@codeSpace='www.ietf.org/rfc/rfc1738']"/>
		<xsl:variable name="observationMethod" select="./gsml:observationMethod/gsml:CGI_TermValue/gsml:value[@codeSpace='www.ietf.org/rfc/rfc1738']"/>
		
        <table>
        	<colgroup span="1" width="15%"/>
            <colgroup span="1" width="35%"/>
            <colgroup span="2" width="20%"/>
            <colgroup span="1" width="20%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">GeoSciML - Yilgarn Laterite Geologic Unit</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'XML'"/>
                            <xsl:with-param name="real-url" select="$serviceURL"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="row header">Id:</td>
                    <td class="row" colspan="4"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$guID"/>
                            <xsl:with-param name="real-url" select="$serviceURL"/>           
                        </xsl:call-template></td>
                </tr>
                <xsl:for-each select="./gml:name[not(@codeSpace='http://www.ietf.org/rfc/rfc3406')]">
                    <xsl:choose>
                        <xsl:when test="position()=1">
                        <tr>
                            <td class="row header">Name:</td>
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
                <tr>
                    <td class="row header">Observation Method:</td>
                    <td class="row">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="$observationMethod"/>
                            <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookupCGI,$observationMethod)"/>
                        </xsl:call-template>
                    </td>                    
                    <td class="row header">Mapped Feature Id:</td>
                    <td class="row" colspan="2"><xsl:value-of select="./gsml:occurrence/gsml:MappedFeature/@gml:id"/></td>                    
                </tr>
                <tr>                    
                    <td class="row header">Mapped Feature Observation Method:</td>
                    <td class="row" colspan="4">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="$mappedFeatureObsMethod"/>
                            <xsl:with-param name="real-url" select="concat($vocab-hard-coded-lookupCGI,$mappedFeatureObsMethod)"/>
                        </xsl:call-template>
                    </td>                    
                </tr>
                
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
        <a href="wfsFeaturePopup.do?url={$real-url}" onclick="var w=window.open('wfsFeaturePopup.do?url={$real-url}','AboutWin','toolbar=no, menubar=no,location=no,resizable=yes,scrollbars=yes,statusbar=no,height=450,width=820');w.focus();return false;"><xsl:value-of select="$friendly-name"/></a>
    </xsl:template>

</xsl:stylesheet>
