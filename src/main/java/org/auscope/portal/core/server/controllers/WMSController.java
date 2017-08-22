package org.auscope.portal.core.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BaseCSWController;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWResponsibleParty;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles GetCapabilites (WFS)WMS queries.
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
@Scope("session")
//this can't be a singleton as each request by a user may be targeting a specific wms version
public class WMSController extends BaseCSWController {

    // ----------------------------------------------------- Instance variables

    private WMSService wmsService;
    private final Log log = LogFactory.getLog(getClass());
    protected static int BUFFERSIZE = 1024 * 1024;
    HttpServiceCaller serviceCaller;

    // ----------------------------------------------------------- Constructors

    @Autowired
    public WMSController(WMSService wmsService, ViewCSWRecordFactory viewCSWRecordFactory,
            ViewKnownLayerFactory knownLayerFact, HttpServiceCaller serviceCaller) {
        super(viewCSWRecordFactory, knownLayerFact);
        this.wmsService = wmsService;
        this.serviceCaller = serviceCaller;
    }

    // ------------------------------------------- Property Setters and Getters

    /**
     * Gets all WMS data records from a discovery service, and then creates JSON response for the WMS layers list in the portal
     *
     * @param serviceUrl
     *            The WMS URL to query
     *
     * @param weakCheck
     *            Turns off checking for the correct EPSG records before URL resolution
     *
     * @return a JSON representation of the CSWRecord equivalent records
     *
     * @throws Exception
     */
    @RequestMapping("/getCustomLayers.do")
    public ModelAndView getCustomLayers(@RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value="weakCheck", defaultValue = "N") String weakCheck) throws Exception {

        CSWRecord[] records;
        int invalidLayerCount = 0;
        try {
            //VT:We have absolutely no way of finding out wms version in custom layer so we have to
            //guess the version by setting version to null.
            GetCapabilitiesRecord capabilitiesRec = wmsService.getWmsCapabilities(serviceUrl, null);

            List<CSWRecord> cswRecords = new ArrayList<CSWRecord>();

            if (capabilitiesRec != null) {
                //Make a best effort of parsing a WMS into a CSWRecord
                for (GetCapabilitiesWMSLayerRecord rec : capabilitiesRec.getLayers()) {
                    //If weakCheck is not 'Y' then check if layers are EPSG:4326 or EPSG:3857 SRS
                    String[] uniqueSRSList = getSRSList(capabilitiesRec.getLayerSRS(), rec.getChildLayerSRS());
                    if (!weakCheck.equals("Y") && !((Arrays.binarySearch(uniqueSRSList, "epsg:3857")) >= 0 || (Arrays.binarySearch(uniqueSRSList,
                            "epsg:4326")) >= 0)) {
                        invalidLayerCount += 1;
                        continue;
                    }

                    if (rec.getName() == null || rec.getName().isEmpty()) {
                        continue;
                    }

                    String serviceName = rec.getTitle();
                    //VT:Ext.DomQuery.selectNode('#rowexpandercontainer-' + record.id, el.parentNode); cannot handle : and .
                    String fileId = "unique-id-"
                            + StringUtils.replaceEach(rec.getName(), new String[] {":", "."}, new String[] {"", ""});
                    String recordInfoUrl = null;
                    String dataAbstract = rec.getAbstract();
                    CSWResponsibleParty responsibleParty = new CSWResponsibleParty();
                    responsibleParty.setOrganisationName(capabilitiesRec.getOrganisation());

                    CSWGeographicElement[] geoEls = null;
                    CSWGeographicBoundingBox bbox = rec.getBoundingBox();
                    if (bbox != null) {
                        geoEls = new CSWGeographicElement[] {bbox};
                    }

                    AbstractCSWOnlineResource[] onlineResources = new AbstractCSWOnlineResource[1];

                    if (capabilitiesRec.getVersion().equals("1.3.0")) {
                        onlineResources[0] = new CSWOnlineResourceImpl(new URL(capabilitiesRec.getMapUrl()),
                                "OGC:WMS-1.3.0-http-get-map",
                                rec.getName(),
                                rec.getTitle());
                    } else {
                        onlineResources[0] = new CSWOnlineResourceImpl(new URL(capabilitiesRec.getMapUrl()),
                                "OGC:WMS-1.1.1-http-get-map",
                                rec.getName(),
                                rec.getTitle());
                    }

                    CSWRecord newRecord = new CSWRecord(serviceName, fileId, recordInfoUrl, dataAbstract,
                            onlineResources, geoEls);
                    newRecord.setContact(responsibleParty);
                    cswRecords.add(newRecord);

                }
            } else {
                // Cannot find any WMS capability records
                log.debug("Cannot find any WMS capability records");
                return generateJSONResponseMAV(false, "I can resolve your WMS URL, but cannot find any WMS capability records", null);
            }

            //generate the same response from a getCachedCSWRecords call
            records = cswRecords.toArray(new CSWRecord[cswRecords.size()]);
        } catch (MalformedURLException e) {
            log.debug(e.getMessage());
            return generateJSONResponseMAV(false, "I cannot resolve your WMS URL, there was a malformed URL error: "+e.getMessage(), null);
        } catch (Exception e) {
            String excStr = e.getMessage();
            log.debug(excStr);
            // Fix up the least informative messages
            if (excStr.equals("Not Found")) {
                return generateJSONResponseMAV(false, "I cannot resolve your WMS URL: page not found", null);

            } else if (excStr.equals("null")) {
                return generateJSONResponseMAV(false, "I cannot resolve your WMS URL", null);
            }

            return generateJSONResponseMAV(false, excStr, null);
        }

        if (records.length == 0) {
            return generateJSONResponseMAV(false, "Your WMS does not appear to support EPSG:3857 (WGS 84 / Pseudo-Mercator) or EPSG:4326 (WGS 84). This is required to be able to display your map in this Portal. If you are certain that your service supports EPSG:3857, click Yes for portal to attempt loading of the layer else No to exit.", null);
        }

        ModelAndView mav = generateJSONResponseMAV(records);
        mav.addObject("invalidLayerCount", invalidLayerCount);
        return mav;
    }

    public String[] getSRSList(String[] layerSRS, String[] childLayerSRS) {
        try {
            int totalLength = layerSRS.length;
            totalLength += childLayerSRS.length;
            String[] totalSRS = new String[totalLength];
            System.arraycopy(layerSRS, 0, totalSRS, 0, layerSRS.length);
            System.arraycopy(childLayerSRS, 0, totalSRS, layerSRS.length, childLayerSRS.length);
            Arrays.sort(totalSRS);

            int k = 0;
            for (int i = 0; i < totalSRS.length; i++) {
                if (i > 0 && totalSRS[i].equals(totalSRS[i - 1])) {
                    continue;
                }
                totalSRS[k++] = totalSRS[i];
            }
            String[] uniqueSRS = new String[k];
            System.arraycopy(totalSRS, 0, uniqueSRS, 0, k);

            for (int i = 0; i < uniqueSRS.length; i++) {
                uniqueSRS[i] = uniqueSRS[i].toLowerCase();
            }

            return uniqueSRS;
        } catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Gets all the valid GetMap formats that a service defines
     *
     * @param serviceUrl
     *            The WMS URL to query
     */
    @RequestMapping("/getLayerFormats.do")
    public ModelAndView getLayerFormats(@RequestParam("serviceUrl") String serviceUrl) throws Exception {
        try {

            GetCapabilitiesRecord capabilitiesRec = wmsService.getWmsCapabilities(serviceUrl, null);

            List<ModelMap> data = new ArrayList<ModelMap>();
            for (String format : capabilitiesRec.getGetMapFormats()) {
                ModelMap formatItem = new ModelMap();
                formatItem.put("format", format);
                data.add(formatItem);
            }

            return generateJSONResponseMAV(true, data, "");
        } catch (Exception e) {
            log.warn(String.format("Unable to download WMS layer formats for '%1$s'", serviceUrl));
            log.debug(e);
            return generateJSONResponseMAV(false, "Unable to process request", null);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @param wmsUrl
     * @param latitude
     * @param longitude
     * @param queryLayers
     * @param x
     * @param y
     * @param bbox
     *            A CSV string formatted in the form - longitude,latitude,longitude,latitude
     * @param width
     * @param height
     * @param infoFormat
     * @param sld_body
     *            - sld_body
     * @param postMethod
     *            Use getfeatureinfo POST method rather then GET
     * @param version
     *            - the wms version to use
     * @throws Exception
     */
    @RequestMapping(value = "/wmsMarkerPopup.do", method = {RequestMethod.GET, RequestMethod.POST})
    public void wmsUnitPopup(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("lat") String latitude,
            @RequestParam("lng") String longitude,
            @RequestParam("QUERY_LAYERS") String queryLayers,
            @RequestParam("x") String x,
            @RequestParam("y") String y,
            @RequestParam("BBOX") String bbox,
            @RequestParam("WIDTH") String width,
            @RequestParam("HEIGHT") String height,
            @RequestParam("INFO_FORMAT") String infoFormat,
            @RequestParam(value = "SLD_BODY", defaultValue = "") String sldBody,
            @RequestParam(value = "postMethod", defaultValue = "false") Boolean postMethod,
            @RequestParam("version") String version,
            @RequestParam(value = "feature_count", defaultValue = "0") String feature_count) throws Exception {

        String[] bboxParts = bbox.split(",");
        double lng1 = Double.parseDouble(bboxParts[0]);
        double lng2 = Double.parseDouble(bboxParts[2]);
        double lat1 = Double.parseDouble(bboxParts[1]);
        double lat2 = Double.parseDouble(bboxParts[3]);

        String responseString = wmsService.getFeatureInfo(serviceUrl, infoFormat, queryLayers, "EPSG:3857",
                Math.min(lng1, lng2), Math.min(lat1, lat2), Math.max(lng1, lng2), Math.max(lat1, lat2),
                Integer.parseInt(width), Integer.parseInt(height), Double.parseDouble(longitude),
                Double.parseDouble(latitude),
                (int) (Double.parseDouble(x)), (int) (Double.parseDouble(y)), "", sldBody, postMethod, version,
                feature_count, true);
        //VT: Ugly hack for the GA wms layer in registered tab as its font is way too small at 80.
        //VT : GA style sheet also mess up the portal styling of tables as well.
        if (responseString.contains("table, th, td {")) {
            responseString = responseString.replaceFirst("table, th, td \\{",
                    ".ausga table, .ausga th, .ausga td {");
            responseString = responseString.replaceFirst("th, td \\{", ".ausga th, .ausga td {");
            responseString = responseString.replaceFirst("th \\{", ".ausga th {");
            responseString = responseString.replace("<table", "<table class='ausga'");
        }

        InputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
        FileIOUtil.writeInputToOutputStream(responseStream, response.getOutputStream(), BUFFERSIZE, true);
    }

    @RequestMapping("/getDefaultStyle.do")
    public void getDefaultStyle(
            HttpServletResponse response,
            @RequestParam("layerName") String layerName)
                    throws Exception {

        String style = this.getStyle(layerName, "#ed9c38");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    /**
     * A proxy to make http post request to get map.
     * @param response
     * @param layerName
     * @throws Exception
     */
    @RequestMapping(value = "/getWMSMapViaProxy.do", method = {RequestMethod.GET, RequestMethod.POST})
    public void getWMSMapViaProxy(
            @RequestParam("url") String url,
            @RequestParam("layer") String layer,
            @RequestParam("bbox") String bbox,
            @RequestParam("sldUrl") String sldUrl,
            @RequestParam("version") String version,
            HttpServletResponse response,
            HttpServletRequest request)
                    throws Exception {

        response.setContentType("image/png");

        HttpClientInputStream styleStream = this.wmsService.getMap(url, layer, bbox, request.getRequestURL().toString().replace(request.getServletPath(),"") + sldUrl, version);
        OutputStream outputStream = response.getOutputStream();
        IOUtils.copy(styleStream,outputStream);
        //FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);
        styleStream.close();
        outputStream.close();
    }

    public String getStyle(String name, String color) {
        //VT : This is a hack to get around using functions in feature chaining
        // https://jira.csiro.au/browse/SISS-1374
        // there are currently no available fix as wms request are made prior to
        // knowing app-schema mapping.

        String style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" xmlns:mo=\"http://xmlns.geoscience.gov.au/minoccml/1.0\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + name + "</Name>"
                + "<UserStyle>" + "<Name>portal-style</Name>"
                + "<Title>" + name + "</Title>"
                + "<Abstract>EarthResource</Abstract>"
                + "<IsDefault>1</IsDefault>" + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>" + name + "</Name>"
                + "<Abstract>" + name + "</Abstract>"
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>square</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">" + color + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>6</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
        return style;
    }

}
