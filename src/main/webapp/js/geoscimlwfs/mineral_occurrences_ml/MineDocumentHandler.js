/**
 * Handles the mo:Mine GetFeature response from updateCSWRecords WFS
 *
 * Example mo:Mine response
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 <wfs:FeatureCollection xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:mo="urn:cgi:xmlns:GGIC:MineralOccurrence:1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:wfs="http://www.opengis.net/wfs" xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" numberOfFeatures="2" xsi:schemaLocation="urn:cgi:xmlns:GGIC:MineralOccurrence:1.0 https://www.seegrid.csiro.au/subversion/xmml/GGIC/trunk/schema/MineralOccurrence/1.0.1/mineralOccurrence.xsd http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd">
 <gml:boundedBy>
 <gml:Envelope srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.10619 -37.63652</gml:pos>
 <gml:pos srsDimension="2">147.52321 -37.33473</gml:pos>
 </gml:Envelope>
 </gml:boundedBy>
 <gml:featureMember>
 <mo:Mine gml:id="mo.mine.361068">
 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">Good Hope</gml:name>
 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">Mitchell</gml:name>
 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">New Good Hope</gml:name>

 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">New Good Hope Tribute</gml:name>
 <gml:name codeSpace="http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services">urn:cgi:feature:GSV:Mine:361068</gml:name>
 <gml:boundedBy>
 <gml:Envelope srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.10619 -37.33473</gml:pos>
 <gml:pos srsDimension="2">147.10619 -37.33473</gml:pos>
 </gml:Envelope>
 </gml:boundedBy>
 <mo:occurrence>
 <mo:MiningFeatureOccurrence gml:id="mo.miningfeatureoccurrence.361068">
 <gml:name codeSpace="http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services">urn:cgi:feature:GSV:MiningFeatureOccurrence:361068</gml:name>
 <gml:boundedBy>

 <gml:Envelope srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.10619 -37.33473</gml:pos>
 <gml:pos srsDimension="2">147.10619 -37.33473</gml:pos>
 </gml:Envelope>
 </gml:boundedBy>
 <mo:observationMethod>
 <gsml:CGI_TermValue>
 <gsml:value codeSpace="http://urn.opengis.net/">urn:ogc:def:nil:OGC:missing</gsml:value>
 </gsml:CGI_TermValue>
 </mo:observationMethod>
 <mo:positionalAccuracy>
 <gsml:CGI_NumericValue>
 <gsml:principalValue uom="urn:ogc:def:uom:UCUM:metre">5</gsml:principalValue>

 </gsml:CGI_NumericValue>
 </mo:positionalAccuracy>
 <mo:specification xlink:href="urn:cgi:feature:GSV:Mine:361068"/>
 <mo:location>
 <gml:Point srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.10619 -37.33473</gml:pos>
 </gml:Point>
 </mo:location>
 </mo:MiningFeatureOccurrence>
 </mo:occurrence>
 <mo:startDate>
 <gml:TimeInstant>
 <gml:timePosition>1865-01-01T00:00:00</gml:timePosition>
 </gml:TimeInstant>
 </mo:startDate>

 <mo:endDate>
 <gml:TimeInstant>
 <gml:timePosition>1914-12-31T00:00:00</gml:timePosition>
 </gml:TimeInstant>
 </mo:endDate>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>true</mo:isPreferred>
 <mo:mineName>Good Hope</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>

 <mo:mineName>Mitchell</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>
 <mo:mineName>New Good Hope</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>
 <mo:mineName>New Good Hope Tribute</mo:mineName>

 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>
 <mo:mineName>New Good Hope Tribute</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:sourceReference>
 <gmd:CI_Citation id="gmd.ci.citation.19900">
 <gmd:title>
 <gco:CharacterString>Notes on the geological structure of north Gippsland</gco:CharacterString>
 </gmd:title>
 <gmd:date>

 <gmd:CI_Date>
 <gmd:date>
 <gco:Date>1877</gco:Date>
 </gmd:date>
 <gmd:dateType>
 <gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/>
 </gmd:dateType>
 </gmd:CI_Date>
 </gmd:date>
 </gmd:CI_Citation>
 </mo:sourceReference>
 <mo:sourceReference>
 <gmd:CI_Citation id="gmd.ci.citation.20414">
 <gmd:title>
 <gco:CharacterString>Wonnangatta</gco:CharacterString>

 </gmd:title>
 <gmd:date>
 <gmd:CI_Date>
 <gmd:date>
 <gco:Date>1970</gco:Date>
 </gmd:date>
 <gmd:dateType>
 <gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/>
 </gmd:dateType>
 </gmd:CI_Date>
 </gmd:date>
 </gmd:CI_Citation>
 </mo:sourceReference>
 <mo:status>unknown</mo:status>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:83"/>

 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:90"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:78"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27891"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:85"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27885"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27886"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:89"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:93"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:94"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:95"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:76"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27916"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:80"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:84"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27890"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:91"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:92"/>

 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:98"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:100"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:81"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:82"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:75"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27887"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:27915"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:86"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:97"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:99"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361068:77"/>
 </mo:Mine>
 </gml:featureMember>
 <gml:featureMember>
 <mo:Mine gml:id="mo.mine.361102">
 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">New Sons of Freedom</gml:name>

 <gml:name codeSpace="urn:cgi:classifierScheme:GSV:VicMineName">Sons of Freedom Reef</gml:name>
 <gml:name codeSpace="http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services">urn:cgi:feature:GSV:Mine:361102</gml:name>
 <gml:boundedBy>
 <gml:Envelope srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.52321 -37.63652</gml:pos>
 <gml:pos srsDimension="2">147.52321 -37.63652</gml:pos>
 </gml:Envelope>
 </gml:boundedBy>
 <mo:occurrence>
 <mo:MiningFeatureOccurrence gml:id="mo.miningfeatureoccurrence.361102">
 <gml:name codeSpace="http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services">urn:cgi:feature:GSV:MiningFeatureOccurrence:361102</gml:name>
 <gml:boundedBy>

 <gml:Envelope srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.52321 -37.63652</gml:pos>
 <gml:pos srsDimension="2">147.52321 -37.63652</gml:pos>
 </gml:Envelope>
 </gml:boundedBy>
 <mo:observationMethod>
 <gsml:CGI_TermValue>
 <gsml:value codeSpace="http://urn.opengis.net/">urn:ogc:def:nil:OGC:missing</gsml:value>
 </gsml:CGI_TermValue>
 </mo:observationMethod>
 <mo:positionalAccuracy>
 <gsml:CGI_NumericValue>
 <gsml:principalValue uom="urn:ogc:def:uom:UCUM:metre">100</gsml:principalValue>

 </gsml:CGI_NumericValue>
 </mo:positionalAccuracy>
 <mo:specification xlink:href="urn:cgi:feature:GSV:Mine:361102"/>
 <mo:location>
 <gml:Point srsName="EPSG:4283">
 <gml:pos srsDimension="2">147.52321 -37.63652</gml:pos>
 </gml:Point>
 </mo:location>
 </mo:MiningFeatureOccurrence>
 </mo:occurrence>
 <mo:startDate>
 <gml:TimeInstant>
 <gml:timePosition>1869-01-01T00:00:00</gml:timePosition>
 </gml:TimeInstant>
 </mo:startDate>

 <mo:endDate>
 <gml:TimeInstant>
 <gml:timePosition>1903-12-31T00:00:00</gml:timePosition>
 </gml:TimeInstant>
 </mo:endDate>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>
 <mo:mineName>Sons of Freedom Reef</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>false</mo:isPreferred>

 <mo:mineName>New Sons of Freedom</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:mineName>
 <mo:MineName>
 <mo:isPreferred>true</mo:isPreferred>
 <mo:mineName>Sons of Freedom Reef</mo:mineName>
 </mo:MineName>
 </mo:mineName>
 <mo:sourceReference>
 <gmd:CI_Citation id="gmd.ci.citation.1592">
 <gmd:title>
 <gco:CharacterString>EL 243, County of Dargo, geological exploration. Report No 1</gco:CharacterString>

 </gmd:title>
 <gmd:date>
 <gmd:CI_Date>
 <gmd:date>
 <gco:Date>1971</gco:Date>
 </gmd:date>
 <gmd:dateType>
 <gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/>
 </gmd:dateType>
 </gmd:CI_Date>
 </gmd:date>
 </gmd:CI_Citation>
 </mo:sourceReference>
 <mo:sourceReference>
 <gmd:CI_Citation id="gmd.ci.citation.13140">
 <gmd:title>

 <gco:CharacterString>Parish of Bullumwaal</gco:CharacterString>
 </gmd:title>
 <gmd:date>
 <gmd:CI_Date>
 <gmd:date>
 <gco:Date>1895</gco:Date>
 </gmd:date>
 <gmd:dateType>
 <gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/>
 </gmd:dateType>
 </gmd:CI_Date>
 </gmd:date>
 </gmd:CI_Citation>
 </mo:sourceReference>
 <mo:status>unknown</mo:status>

 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:142"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:151"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:154"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:143"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:144"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:145"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:150"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:152"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:146"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:155"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:148"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:147"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:149"/>
 <mo:relatedActivity xlink:href="urn:cgi:feature:GSV:MiningActivity:361102:153"/>
 </mo:Mine>
 </gml:featureMember>
 </wfs:FeatureCollection>

 *
 * @param xmlDocMineGetFeatureResponse
 */

function MineDocumentHandler(xmlDocMineGetFeatureResponse) {
    /**
     * Returns updateCSWRecords collection Array of Mines - Mine.js
     */
    this.getMines = function() {
        //get all the mine features from the document
        var mineNodes = xmlDocMineGetFeatureResponse.getElementsByTagName("er:Mine");

        //placeholder for the mines
        var mines = new Array();

        //create Mine objects - Mine.js
        for(i=0; i<mineNodes.length; i++) {
            mines[i] = new Mine(mineNodes[i]);
        }

        return mines;        
    };
}