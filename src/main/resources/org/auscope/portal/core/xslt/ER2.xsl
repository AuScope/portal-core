<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" 
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
                xmlns:xlink="http://www.w3.org/1999/xlink"   
                xmlns:gml="http://www.opengis.net/gml/3.2"   
                xmlns:gsml="http://xmlns.geosciml.org/GeoSciML-Core/3.2"
                xmlns:gsmlu="http://xmlns.geosciml.org/Utilities/3.2"
                xmlns:er="http://xmlns.earthresourceml.org/EarthResource/2.0"
                xmlns:swe="http://www.opengis.net/swe/2.0"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:wfs_2="http://www.opengis.net/wfs/2.0"
                exclude-result-prefixes="er gml wfs wfs_2 gsml gsmlu xlink wfs swe">
                
    <xsl:output method="html" indent="yes"/>
                
    <xsl:template name="ERBody">
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:MineralOccurrence |
                        wfs:FeatureCollection/gml:featureMember/er:MineralOccurrence | er:MineralOccurrence"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:Mine |
                        wfs:FeatureCollection/gml:featureMember/er:Mine | er:Mine"/>                
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:Commodity |
                        wfs:FeatureCollection/gml:featureMember/er:Commodity | er:Commodity"/>
                <xsl:apply-templates select="wfs:FeatureCollection/gml:featureMembers/er:MiningFeatureOccurrence |
                        wfs:FeatureCollection/gml:featureMember/er:MiningFeatureOccurrence | er:MiningFeatureOccurrence"/>
                <!-- WFS 2.0 -->
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/er:Mine"/>                
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/er:MineralOccurrence"/>
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/er:Commodity"/>
                <xsl:apply-templates select="wfs_2:FeatureCollection/wfs_2:member/er:MiningFeatureOccurrence"/>   
    </xsl:template>
    <xsl:template match="er:Mine">
        <xsl:variable name="mineID" select="./gml:identifier"/>
        <xsl:variable name="minePrefName" select="./er:mineName/er:MineName/er:mineName/text()" />
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
                <tr>
                    <td class="our_row header">Mine Name:</td>
                    <td class="our_row"><xsl:value-of select="$minePrefName"/></td>
                </tr>
                <tr>
                    <td class="our_row header">Mine Id:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$mineID"/>
                            <xsl:with-param name="real-url" select="$mineID"/>
                        </xsl:call-template></td>
                </tr>
                <tr>
                    <td class="our_row header">Status:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./er:status/@xlink:title"/>
                            <xsl:with-param name="real-url" select="./er:status/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Source Reference:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./er:sourceReference/@xlink:title"/>
                            <xsl:with-param name="real-url" select="./er:sourceReference/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
        </table>&#160;
        <xsl:for-each select="./er:occurrence/er:MiningFeatureOccurrence">
            <xsl:apply-templates select=". | er:MiningFeatureOccurrence"/>&#160;
        </xsl:for-each>
     </xsl:template>
     <xsl:template match="er:MiningFeatureOccurrence">
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - Mining Feature Occurrence</td>
                    <td colspan="2" ALIGN="right"/>
                </tr>
                
                <xsl:apply-templates select="./er:observationMethod/swe:Category"/>
                <xsl:apply-templates select="./er:positionalAccuracy/swe:Quantity"/>
                <tr>
                    <td class="our_row header">Shape:</td>
                    <td class="our_row"><xsl:value-of select="./er:shape"/></td>
                </tr>
             </tbody>
         </table>
     </xsl:template>
     <xsl:template match="swe:Category">
         <tr>
             <td class="our_row header">Observation Method:</td>  
             <td class="our_row" colspan="2">
                 <xsl:call-template name="make-popup-url">
                     <xsl:with-param name="friendly-name" select="./swe:label"/>
                     <xsl:with-param name="real-url" select="./@definition"/>
                 </xsl:call-template>
             </td>
         </tr>
     </xsl:template>
     <xsl:template match="swe:Quantity">
         <tr>
             <td class="our_row header">Positional Accuracy:</td>
             <td class="our_row"><xsl:value-of 
                    select="concat(./swe:value, ' (', ./swe:uom/@code, ')')"/>
             </td>
         </tr>
     </xsl:template>
     <xsl:template match="er:CommodityMeasure">
         <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - Commodity Measure</td>
                    <td colspan="2" ALIGN="right"/>
                </tr>
                <tr>
                    <td class="our_row header">Grade (lower value):</td>  
                    <td class="our_row"><xsl:value-of 
                        select="concat(./er:grade/gsmlu:GSML_QuantityRange/gsmlu:lowerValue, ./er:grade/gsmlu:GSML_QuantityRange/swe:uom/@code)"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Grade (upper value):</td>  
                    <td class="our_row"><xsl:value-of 
                        select="concat(./er:grade/gsmlu:GSML_QuantityRange/gsmlu:upperValue, ./er:grade/gsmlu:GSML_QuantityRange/swe:uom/@code)"/>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Commodity of Interest:</td>  
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./er:commodityOfInterest/@xlink:title"/>
                            <xsl:with-param name="real-url" select="./er:commodityOfInterest/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
        </table>
     </xsl:template>
     <xsl:template match="er:Commodity">
        <xsl:variable name="minePrefName" select="./er:mineName/er:MineName/er:mineName/text()" />
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - Commodity</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML'"/>
                            <xsl:with-param name="real-url" select="./gml:identifier"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Identifier:</td>  
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./gml:identifier"/>
                            <xsl:with-param name="real-url" select="./gml:identifier"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Commodity:</td>  
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./er:commodity/@xlink:title"/>
                            <xsl:with-param name="real-url" select="./er:commodity/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Commodity Importance:</td>
                    <td class="our_row"><xsl:value-of select="./er:commodityImportance"/></td>
                </tr>
                <tr>
                    <td class="our_row header">Commodity Rank:</td>
                    <td class="our_row"><xsl:value-of select="./er:commodityRank"/></td>
                </tr>
                <tr>
                    <td class="our_row header">Source:</td>  
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./er:source/@xlink:href"/>
                            <xsl:with-param name="real-url" select="./er:source/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
         </table>
     </xsl:template>
     <xsl:template match="er:MineralOccurrence">
        <xsl:variable name="id" select="./gml:identifier"/>
        <table>
            <colgroup span="1" width="15%"/>
            <colgroup span="1" width="25%"/>
            <colgroup span="2" width="15%"/>
            <colgroup span="1" width="35%"/>
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">EarthResourceML - Mineral Occurrence</td>
                    <td colspan="2" ALIGN="right"><b>View As: </b>
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="'EarthResourceML'"/>
                            <xsl:with-param name="real-url" select="$id"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Identifier:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="$id"/>
                            <xsl:with-param name="real-url" select="$id"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Name:</td>
                    <td class="our_row"><xsl:value-of select="./gml:name"/></td>
                </tr>
                <tr>
                    <td class="our_row header">Purpose:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:purpose"/></td>
                </tr>
                <tr>
                    <td class="our_row header">Parent:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param name="friendly-name" select="./er:parent/@xlink:href"/>
                            <xsl:with-param name="real-url" select="./er:parent/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Type:</td>
                    <td class="our_row" colspan="2"><xsl:call-template name="make-popup-url">
                            <xsl:with-param name="friendly-name" select="./er:type/@xlink:title"/>
                            <xsl:with-param name="real-url" select="./er:type/@xlink:href"/>
                        </xsl:call-template>
                    </td>
                </tr>                
                <xsl:apply-templates select="./gsml:observationMethod/swe:Category"/>
            </tbody>
        </table>&#160;
        <xsl:apply-templates select="./gsml:occurrence/gsml:MappedFeature" />&#160;
        <xsl:for-each select="./er:commodityDescription/er:Commodity">
            <xsl:apply-templates select=". | er:Commodity" />&#160;
        </xsl:for-each>

        <xsl:for-each select="./er:child/er:MineralOccurrence">
            <p class="child_table"><xsl:value-of select="concat('Child ', position(), ':')" /></p>
            <xsl:apply-templates select=". | er:MineralOccurrence" />&#160;
        </xsl:for-each>

        <xsl:for-each select="./er:oreAmount/er:Resource/er:measureDetails/er:CommodityMeasure">
            <xsl:apply-templates select=". | er:CommodityMeasure" />&#160;
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="gsml:MappedFeature">
        <table>
            <colgroup span="1" width="15%" />
            <colgroup span="1" width="25%" />
            <colgroup span="2" width="15%" />
            <colgroup span="1" width="35%" />
            <tbody>
                <tr>
                    <td class="caption" colspan="3" rowspan="1">GeoSciML - Mapped Feature</td>
                    <td colspan="2" ALIGN="right"/>
                </tr>
                <xsl:apply-templates
                    select="./gsml:observationMethod/swe:Category" />
                <xsl:apply-templates
                    select="./gsml:positionalAccuracy/swe:Quantity" />
                <tr>
                    <td class="our_row header">Sampling Frame:</td>
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-popup-url">
                            <xsl:with-param
                                name="friendly-name"
                                select="./gsml:samplingFrame/@xlink:href" />
                            <xsl:with-param
                                name="real-url"
                                select="./gsml:samplingFrame/@xlink:href" />
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <td class="our_row header">Shape:</td>
                    <td class="our_row"><xsl:value-of select="./gsml:shape" /></td>
                </tr>
                <tr>
                    <td class="our_row header">Specification:</td>
                    <td class="our_row" colspan="2">
                        <xsl:call-template name="make-wfspopup-url">
                            <xsl:with-param
                                name="friendly-name"
                                select="./gsml:specification/@xlink:href" />
                            <xsl:with-param
                                name="real-url"
                                select="./gsml:specification/@xlink:href" />
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
        </table>            
    </xsl:template>
</xsl:stylesheet>