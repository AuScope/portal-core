package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.util.List;
import java.net.URL;
import java.io.IOException;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

/**
 * User: Mathew Wyatt
 * Date: 12/01/2009
 * Time: 2:01:42 PM
 */
public class GetDataSourcesJSONController extends AbstractController {

   protected final Log logger = LogFactory.getLog(getClass());
    private Enumeration params;

    @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        /*logger.info("started!");

        params = request.getParameterNames();
        while(params.hasMoreElements()) {
            String element = (String)params.nextElement();
            System.out.println( element + " " + request.getParameter(element));
        }*/

        String node = request.getParameter("node");

        if(node.equals("root"))
            return getThemes();
        else if(node.equals("SurfaceSpectra"))
            return getSpectraInstitionalProviders();
        else if(node.equals("borholes"))
            return getBorholeInstitionalProviders();
        else if(node.equals("waCoe"))
            return getLayers(node);

        return new ModelAndView(new Something(), new HashMap());
   }

    //sets up the data theme nodes
   private ModelAndView getThemes() {
        JSONArray jsonArray = new JSONArray();

        Map spectral = new HashMap();
        spectral.put("id", "SurfaceSpectra");
        spectral.put("text", "Surface Spectra");
        spectral.put("checked", Boolean.FALSE);
        spectral.put("leaf", Boolean.FALSE);
        jsonArray.add(spectral);

        Map borholes = new HashMap();
        borholes.put("id", "borholes");
        borholes.put("text", "Boreholes");
        borholes.put("checked", Boolean.FALSE);
        borholes.put("leaf", Boolean.FALSE);
        jsonArray.add(borholes);

        Map mineralOccurrences = new HashMap();
        mineralOccurrences.put("id", "mineralOccurrences");
        mineralOccurrences.put("text", "MineralOccurences");
        mineralOccurrences.put("checked", Boolean.FALSE);
        mineralOccurrences.put("leaf", Boolean.FALSE);
        jsonArray.add(mineralOccurrences);

        Map geologicalUnits = new HashMap();
        geologicalUnits.put("id", "geologicalUnits");
        geologicalUnits.put("text", "Geological Units");
        geologicalUnits.put("checked", Boolean.FALSE);
        geologicalUnits.put("leaf", Boolean.FALSE);
        jsonArray.add(geologicalUnits);

        Map gnnsGPS = new HashMap();
        gnnsGPS.put("id", "gnnsGPS");
        gnnsGPS.put("text", "GNNS/GPS");
        gnnsGPS.put("checked", Boolean.FALSE);
        gnnsGPS.put("leaf", Boolean.FALSE);
        jsonArray.add(gnnsGPS);

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
   }

   private ModelAndView getSpectraInstitionalProviders() {
        JSONArray jsonArray = new JSONArray();

        Map coe = new HashMap();
        coe.put("id", "waCoe");
        coe.put("text", "WA Center of Excellence for 3D Mineral Mapping");
        coe.put("checked", Boolean.FALSE);
        coe.put("leaf", Boolean.FALSE);
        jsonArray.add(coe);

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
   }

    private ModelAndView getBorholeInstitionalProviders() {
        JSONArray jsonArray = new JSONArray();

        Map coe = new HashMap();
        coe.put("id", "nvcl");
        coe.put("text", "National Virtual Core Library");
        coe.put("checked", Boolean.FALSE);
        coe.put("leaf", Boolean.FALSE);
        jsonArray.add(coe);

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
   }

   private ModelAndView getLayers(String node) {

       WebMapServer wms = null;
        try {
           //wms = new WebMapServer(new URL("http://c3dmm2.ivec.org/geoserver/gwc/service/wms?"));
            wms = new WebMapServer(new URL("http://c3dmm2.ivec.org/geoserver/wms?"));
        } catch (IOException e) {
           e.printStackTrace();
        } catch (ServiceException e) {
           e.printStackTrace();
        }
        WMSCapabilities capabilities = wms.getCapabilities();

        JSONArray jsonArray = new JSONArray();

        List<Layer> layers = capabilities.getLayerList();
        for(Layer layer : layers) {
            Map layerNode = new HashMap();
            layerNode.put("id", layer.getName());
            layerNode.put("text", layer.getName());
            layerNode.put("checked", Boolean.FALSE);
            layerNode.put("leaf", Boolean.TRUE);

            //layerNode.put("wmsUrl", "http://c3dmm2.ivec.org/geoserver/gwc/service/wms?");
            layerNode.put("wmsUrl", "http://c3dmm2.ivec.org/geoserver/wms?");
            layerNode.put("tileOverlay", "");

            //layerNode.put("layerURL", layer.get)
            jsonArray.add(layerNode);
        }

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
    }
}


class Something extends AbstractView {

    public Something() {
        super();
        setContentType("application/json");    
    }

    protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        response.getWriter().write(JSONSerializer.toJSON(model.get("JSON_OBJECT")).toString());
    }

}