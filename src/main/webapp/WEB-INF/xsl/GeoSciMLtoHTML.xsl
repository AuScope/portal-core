<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gsml="urn:cgi:xmlns:CGI:GeoSciML:2.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:wfs="http://www.opengis.net/wfs">
	<xsl:import href="FunctionTemplates.xslt"/>
	<xsl:output method="html"/>
	<xsl:template match="wfs:FeatureCollection">
		<html>
			<head>
				<style type="text/css">TD{font-family: Arial; font-size: 10pt; color: #000066}</style>
				<meta content="text/html;charset=ISO-8859-1" http-equiv="Content-Type"></meta>
				<title>GeoScience Victoria - Seamless Geology Project</title>
				<meta content="Alistair Ritchie" name="author"></meta>
			</head>
			<body style="color: rgb(0, 0, 0); background-color: rgb(255, 255, 255);" alink="#000088" link="#0000ff" vlink="#ff0000">
				<xsl:if test="gml:featureMember/gsml:GeologicUnit"><xsl:apply-templates select="gml:featureMember/gsml:GeologicUnit"/></xsl:if>
				<xsl:if test="gml:featureMember/gsml:ShearDisplacementStructure"><xsl:apply-templates select="gml:featureMember/gsml:ShearDisplacementStructure"/></xsl:if>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="gml:featureMember/gsml:ShearDisplacementStructure">
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#000066">
			<tbody>
				<tr>
					<td colspan="2" rowspan="1" style="vertical-align: top;">
						<font color="#FFFFFF"><b>GeoScience Victoria - Seamless Geology Project - Fault</b></font></td>
				</tr>
				<tr>
					<xsl:for-each select="gml:name">
						<xsl:if test="contains(node(),'[previous')=false">
							<xsl:if test="contains(node(),'urn:')=false">
								<td colspan="1" rowspan="1" style="vertical-align: top;">
									<font color="#FFFFFF"><b><xsl:value-of select="."/></b></font>
								</td>
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
					<xsl:for-each select="gml:name">
						<xsl:if test="contains(node(),'urn:')">
							<td style="vertical-align: top;" align="right">
								<font color="#FFFFFF"><b><small><xsl:value-of select="."/></small></b></font>
							</td>
						</xsl:if>
					</xsl:for-each>
				</tr>
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#dddddd">
			<tbody>
				<xsl:for-each select="gml:name">
					<tr>
						<td style="vertical-align: top; width: 60px;"/>
						<xsl:if test="contains(node(),'[previous')">
							<td style="vertical-align: top;"><xsl:value-of select="."/></td>
						</xsl:if>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0">
			<tbody>
				<tr>
					<td style="vertical-align: top; width: 150px;"><b>Observation Method</b></td>
					<td style="vertical-align: top; width: 250px; text-transform: lowercase;">
					    <xsl:value-of select="translate(substring-after(gsml:observationMethod/gsml:CGI_TermValue/gsml:value,'OGC:'),'_',' ')"/></td>
					<td style="vertical-align: top; width: 150px;"><b>Purpose</b></td>
					<td style="vertical-align: top; width: 250px;"><xsl:value-of select="gsml:purpose"/></td>
				</tr>
				<xsl:if test="gsml:geologicHistory">
					<xsl:variable name="preferredAgeID" select="translate(gsml:preferredAge/@xlink:href,'#','')"/>
					<tr>
						<td style="vertical-align: top;"><b>Geological History</b></td>
						<td colspan="3" rowspan="1" style="vertical-align: top; border-top: 1px solid #dddddd;">
							<table style="text-align: left; width: 100%;" border="0" cellpadding="3" cellspacing="0">
								<tbody>
									<xsl:for-each select="gsml:geologicHistory/gsml:DisplacementEvent">
										<xsl:variable name="geologicEventID" select="@gml:id"/>
										<tr>
											<td style="vertical-align: top; width: 150px;"><b>Age</b></td>
											<td style="vertical-align: top; width: 400px;">
												<xsl:variable name="lowerAgeIn">
													<xsl:value-of select="gsml:eventAge/gsml:CGI_TermRange/gsml:lower/gsml:CGI_TermValue/gsml:value"/>
												</xsl:variable>
												<xsl:variable name="lowerAge">
													<xsl:choose>
														<xsl:when test="contains($lowerAgeIn,'Upper')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Upper'"/>
																<xsl:with-param name="replacement" select="'Upper_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:when test="contains($lowerAgeIn,'Middle')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Middle'"/>
																<xsl:with-param name="replacement" select="'Middle_'"/>
															</xsl:call-template>	
														</xsl:when>
														<xsl:when test="contains($lowerAgeIn,'Lower')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Lower'"/>
																<xsl:with-param name="replacement" select="'Lower_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise><xsl:value-of select="$lowerAgeIn"/></xsl:otherwise>
													</xsl:choose>
												</xsl:variable>
												<xsl:variable name="upperAgeIn">
													<xsl:value-of select="gsml:eventAge/gsml:CGI_TermRange/gsml:upper/gsml:CGI_TermValue/gsml:value"/>
												</xsl:variable>
												<xsl:variable name="upperAge">
													<xsl:choose>
														<xsl:when test="contains($upperAgeIn,'Upper')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Upper'"/>
																<xsl:with-param name="replacement" select="'Upper_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:when test="contains($upperAgeIn,'Middle')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Middle'"/>
																<xsl:with-param name="replacement" select="'Middle_'"/>
															</xsl:call-template>	
														</xsl:when>
														<xsl:when test="contains($upperAgeIn,'Lower')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Lower'"/>
																<xsl:with-param name="replacement" select="'Lower_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise><xsl:value-of select="$upperAgeIn"/></xsl:otherwise>
													</xsl:choose>
												</xsl:variable>
												<xsl:value-of select="translate(substring-after($lowerAge,'2008:'),'_',' ')"/> to <xsl:value-of select="translate(substring-after($upperAge,'2008:'),'_',' ')"/>
											</td>
											<td style="vertical-align: top; width: 100px" align="right">
												<xsl:if test="$preferredAgeID = $geologicEventID">
													<font color="green"><b>Preferred Age</b></font>
												</xsl:if>
											</td>
										</tr>
										<xsl:if test="gsml:eventProcess">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Process</b></td>
												<td colspan="2" style="vertical-align: top;">
													<table>
														<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">
															<xsl:for-each select="gsml:eventProcess">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="translate(substring-after(*/gsml:value,'OGC:'),'_',' ')"/>
																	</td>
																</tr>
															</xsl:for-each>
														</tbody>
													</table>
												</td>
											</tr>
										</xsl:if>
										<xsl:if test="gsml:incrementalDisplacement/*/gsml:movementSense">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Movement Sense</b></td>
												<td colspan="2" style="vertical-align: top;">
													<table>
														<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">
															<xsl:for-each select="gsml:incrementalDisplacement/*/gsml:movementSense">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="*/gsml:value"/>
																	</td>
																</tr>
															</xsl:for-each>
														</tbody>
													</table>
												</td>
											</tr>
										</xsl:if>
										<xsl:if test="gsml:incrementalDisplacement/*/gsml:movementType">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Movement Type</b></td>
												<td colspan="2" style="vertical-align: top;">
													<table>
														<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">
															<xsl:for-each select="gsml:incrementalDisplacement/*/gsml:movementType">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="*/gsml:value"/>
																	</td>
																</tr>
															</xsl:for-each>
														</tbody>
													</table>
												</td>
											</tr>
										</xsl:if>
										<xsl:if test="gsml:incrementalDisplacement/*/gsml:hangingWallDirection">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Hanging Wall Dir.</b></td>
												<td colspan="2" style="vertical-align: top;">
													<table>
														<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">
															<xsl:for-each select="gsml:incrementalDisplacement/*/gsml:hangingWallDirection">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="gsml:CGI_LinearOrientation/gsml:descriptiveOrientation/gsml:CGI_TermValue/gsml:value"/>
																	</td>
																</tr>
															</xsl:for-each>
														</tbody>
													</table>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</tbody>
							</table>
						</td>
					</tr>
				</xsl:if>
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#dddddd">
			<tbody><tr><td/></tr></tbody>
		</table>
	</xsl:template>
	<xsl:template match="gml:featureMember/gsml:GeologicUnit">
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#000066">
			<tbody>
				<tr>
					<td colspan="2" rowspan="1" style="vertical-align: top;">
						<font color="#FFFFFF"><b>GeoScience Victoria - Seamless Geology Project - Geological Unit</b></font></td>
				</tr>
				<tr>
					<xsl:for-each select="gml:name">
						<xsl:if test="contains(node(),'[previous')=false">
							<xsl:if test="contains(node(),'urn:')=false">
								<td colspan="1" rowspan="1" style="vertical-align: top;">
									<font color="#FFFFFF"><b><xsl:value-of select="."/></b></font>
								</td>
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
					<xsl:for-each select="gml:name">
						<xsl:if test="contains(node(),'urn:')">
							<td style="vertical-align: top;" align="right">
								<font color="#FFFFFF"><b><small><xsl:value-of select="."/></small></b></font>
							</td>
						</xsl:if>
					</xsl:for-each>
				</tr>
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#dddddd">
			<tbody>
				<xsl:for-each select="gml:name">
					<tr>
						<td style="vertical-align: top; width: 60px;"/>
						<xsl:if test="contains(node(),'[previous')">
							<td style="vertical-align: top;"><xsl:value-of select="."/></td>
						</xsl:if>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0">
			<tbody style="">
				<tr>
					<td style="vertical-align: top; width: 150px; textsize: -1"><b>Type</b></td>
					<td style="vertical-align: top; width: 250px; text-transform: lowercase;">
					    <xsl:value-of select="translate(substring-after(gsml:geologicUnitType/@xlink:href,'2008:'),'_',' ')"/></td>
					<td style="vertical-align: top; width: 150px;"><b>Rank</b></td>
					<td style="vertical-align: top; width: 250px; text-transform: lowercase;"><xsl:value-of select="gsml:rank"/></td>
				</tr>
				<tr>
					<td style="vertical-align: top;"><b>Description</b></td>
					<td colspan="3" rowspan="1" style="vertical-align: top; border-top: 1px solid #dddddd;"><xsl:value-of select="gml:description"/></td>
				</tr>
				<tr>
					<td style="vertical-align: top;"><b>Observation Method</b></td>
					<td style="vertical-align: top; text-transform: lowercase; border-top: 1px solid #dddddd;">
						<xsl:value-of select="translate(substring-after(gsml:observationMethod/gsml:CGI_TermValue/gsml:value,'OGC:'),'_',' ')"/></td>
					<td style="vertical-align: top; border-top: 1px solid #dddddd;"><b>Purpose</b></td>
					<td style="vertical-align: top; border-top: 1px solid #dddddd;"><xsl:value-of select="gsml:purpose"/></td>
				</tr>
				<xsl:if test="gsml:geologicHistory">
					<xsl:variable name="preferredAgeID" select="translate(gsml:preferredAge/@xlink:href,'#','')"/>
					<tr>
						<td style="vertical-align: top;"><b>Geological History</b></td>
						<td colspan="3" rowspan="1" style="vertical-align: top; border-top: 1px solid #dddddd;">
							<table style="text-align: left; width: 100%;" border="0" cellpadding="3" cellspacing="0">
								<tbody>
									<xsl:for-each select="gsml:geologicHistory/gsml:GeologicEvent">
										<xsl:variable name="geologicEventID" select="@gml:id"/>
										<tr>
											<td style="vertical-align: top; width: 150px;"><b>Age</b></td>
											<td style="vertical-align: top; width: 400px;">
												<xsl:variable name="lowerAgeIn">
													<xsl:value-of select="gsml:eventAge/gsml:CGI_TermRange/gsml:lower/gsml:CGI_TermValue/gsml:value"/>
												</xsl:variable>
												<xsl:variable name="lowerAge">
													<xsl:choose>
														<xsl:when test="contains($lowerAgeIn,'Upper')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Upper'"/>
																<xsl:with-param name="replacement" select="'Upper_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:when test="contains($lowerAgeIn,'Middle')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Middle'"/>
																<xsl:with-param name="replacement" select="'Middle_'"/>
															</xsl:call-template>	
														</xsl:when>
														<xsl:when test="contains($lowerAgeIn,'Lower')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$lowerAgeIn"/>
																<xsl:with-param name="target" select="'Lower'"/>
																<xsl:with-param name="replacement" select="'Lower_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise><xsl:value-of select="$lowerAgeIn"/></xsl:otherwise>
													</xsl:choose>
												</xsl:variable>
												<xsl:variable name="upperAgeIn">
													<xsl:value-of select="gsml:eventAge/gsml:CGI_TermRange/gsml:upper/gsml:CGI_TermValue/gsml:value"/>
												</xsl:variable>
												<xsl:variable name="upperAge">
													<xsl:choose>
														<xsl:when test="contains($upperAgeIn,'Upper')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Upper'"/>
																<xsl:with-param name="replacement" select="'Upper_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:when test="contains($upperAgeIn,'Middle')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Middle'"/>
																<xsl:with-param name="replacement" select="'Middle_'"/>
															</xsl:call-template>	
														</xsl:when>
														<xsl:when test="contains($upperAgeIn,'Lower')">
															<xsl:call-template name="globalReplace">
																<xsl:with-param name="outputString" select="$upperAgeIn"/>
																<xsl:with-param name="target" select="'Lower'"/>
																<xsl:with-param name="replacement" select="'Lower_'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise><xsl:value-of select="$upperAgeIn"/></xsl:otherwise>
													</xsl:choose>
												</xsl:variable>
												<xsl:value-of select="translate(substring-after($lowerAge,'2008:'),'_',' ')"/> to <xsl:value-of select="translate(substring-after($upperAge,'2008:'),'_',' ')"/>
											</td>
											<td style="vertical-align: top; width: 100px" align="right">
												<xsl:if test="$preferredAgeID = $geologicEventID">
													<font color="green"><b>Preferred Age</b></font>
												</xsl:if>
											</td>
										</tr>
										<xsl:if test="gsml:eventProcess">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Process</b></td>
												<td colspan="2" style="vertical-align: top;  width: 500px;">
													<table style="text-align: left; width: 100%;" border="0" cellpadding="3" cellspacing="0">
														<!--<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">-->
															<xsl:for-each select="gsml:eventProcess">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="*/gsml:value"/>
																	</td>
																</tr>
															</xsl:for-each>
														<!--</tbody>-->
													</table>
												</td>
											</tr>
										</xsl:if>
										<xsl:if test="gsml:eventEnvironment">
											<tr>
												<td style="vertical-align: top; width: 150px;"><b>Environment</b></td>
												<td colspan="2" style="vertical-align: top;">
													<table style="text-align: left; width: 100%;" border="0" cellpadding="3" cellspacing="0">
														<tbody style="text-align: left;" border="0" cellpadding="3" cellspacing="0">
															<xsl:for-each select="gsml:eventEnvironment">
																<tr>
																	<td style="vertical-align: top;">
																		<xsl:value-of select="*/gsml:value"/>
																	</td>
																</tr>
															</xsl:for-each>
														</tbody>
													</table>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</tbody>
							</table>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="gsml:composition">
					<tr>
						<td style="vertical-align: top;"><b>Component Material</b></td>
						<td colspan="3" rowspan="1" style="vertical-align: top;">
							<table style="text-align: left; width: 100%;" border="0" cellpadding="3" cellspacing="0">
								<tbody>
									<tr>
										<td style="vertical-align: top; width: 250px; border-top: 1px solid #dddddd;"><b>Lithology</b></td>
										<td style="vertical-align: top; width: 200px; border-top: 1px solid #dddddd;"><b>Role</b></td>
										<td style="vertical-align: top; width: 200px; border-top: 1px solid #dddddd;"><b>Proportion</b></td>
									</tr>
									<xsl:for-each select="gsml:composition/gsml:CompositionPart">
										<tr>
											<td style="vertical-align: top;"><xsl:value-of select="translate(substring-after(gsml:lithology/@xlink:href,'2008:'),'_',' ')"/></td>
											<td style="vertical-align: top;"><xsl:value-of select="gsml:role"/></td>
											<td style="vertical-align: top;"><xsl:value-of select="gsml:proportion/gsml:CGI_TermValue/gsml:value"/></td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
						</td>
					</tr>
				</xsl:if>
				<!--<tr>
					<td style="vertical-align: top;"><b>Physical Property</b></td>
					<td colspan="3" rowspan="1" style="vertical-align: top;"></td>
				</tr>
				<tr>
					<td style="vertical-align: top;"><b>Outcrop Character</b></td>
					<td style="vertical-align: top;"></td>
					<td style="vertical-align: top;"><b>Body Morphology</b></td>
					<td style="vertical-align: top;"></td>
				</tr>-->
			</tbody>
		</table>
		<table style="text-align: left; width: 800px;" border="0" cellpadding="3" cellspacing="0" bgcolor="#dddddd">
			<tbody><tr><td/></tr></tbody>
		</table>
	</xsl:template>
</xsl:stylesheet>