package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.*;
import org.geotools.ows.ServiceException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.FeatureComparators;
import org.geotools.feature.FeatureCollection;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.feature.Feature;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.filter.Filter;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.*;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

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
        else if(node.equals("nvcl"))
            return getFeatures();

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

        Map gps = new HashMap();
        gps.put("id", "gps");
        gps.put("text", "Australian Regional GPS Network");
        gps.put("checked", Boolean.FALSE);
        gps.put("leaf", Boolean.TRUE);
        gps.put("icon", "img/geodesy/gps_stations_on.png");
        jsonArray.add(gps);

        Map gnnsGPS = new HashMap();
        gnnsGPS.put("id", "gnns");
        gnnsGPS.put("text", "Global Navigation Satellite Systems (GNSS)");
        gnnsGPS.put("checked", Boolean.FALSE);
        gnnsGPS.put("leaf", Boolean.TRUE);
        gnnsGPS.put("icon", "img/gnss/gps_stations_on.png");
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

        Map nvcl = new HashMap();
        nvcl.put("id", "nvcl");
        nvcl.put("text", "National Virtual Core Library");
        nvcl.put("checked", Boolean.FALSE);
        nvcl.put("leaf", Boolean.TRUE);
        nvcl.put("icon", "img/nvcl/borehole_on.png");
        jsonArray.add(nvcl);

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
   }

   private ModelAndView getLayers(String node) {
       //String server  = "http://localhost:8090/geoserver/wms?";
       //String server  = "http://c3dmm2.ivec.org/geoserver/wms?";
       String server  = "http://c3dmm2.ivec.org/geoserver/gwc/service/wms?";

       WebMapServer wms = null;
        try {
            wms = new WebMapServer(new URL(server));
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
            layerNode.put("layerType", "wms");

            layerNode.put("wmsUrl", server);
            layerNode.put("tileOverlay", "");

            //layerNode.put("layerURL", layer.get)
            jsonArray.add(layerNode);
        }

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        return new ModelAndView(new Something(), model);
    }

    private ModelAndView getFeatures() {
        //String url = "http://localhost:8090/geoserver/wfs";
        String url = "http://auscope-portal.arrc.csiro.au/gnss/wfs";
        HashMap params = new HashMap();
        JSONArray jsonArray = new JSONArray();

        try {
            params.put(WFSDataStoreFactory.URL.key, WFSDataStoreFactory.createGetCapabilitiesRequest(new URL(url)));
            DataStore dataStore = new WFSDataStoreFactory().createDataStore(params);

            for(String name : dataStore.getTypeNames()) {

                Map layerNode = new HashMap();
                layerNode.put("id", name);
                layerNode.put("text", name);
                layerNode.put("checked", Boolean.FALSE);
                layerNode.put("leaf", Boolean.TRUE);
                layerNode.put("layerType", "wfs");

                layerNode.put("wfsUrl", "http://localhost:8080/geoserver/wfs");
                layerNode.put("tileOverlay", "");

                //layerNode.put("layerURL", layer.get)
                jsonArray.add(layerNode);

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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


class blah {
    public static void main(String[] args) {
        HashMap params = new HashMap();

        String url = "http://auscope-portal.arrc.csiro.au/nvcl/wfs";
        //String url = "http://www.gsv-tb.dpi.vic.gov.au/GeoSciMLv2.0/GeologicUnit/wfs";
        //String url = "http://auscope-portal.arrc.csiro.au/geodesy/wfs";

        //String url = "http://localhost:8080/geoserver/wfs";

        try {
            //params.put(WFSDataStoreFactory.URL.key, WFSDataStoreFactory.createGetCapabilitiesRequest(new URL(url)));
            params.put(WFSDataStoreFactory.URL.key, WFSDataStoreFactory.createGetCapabilitiesRequest(new URL(url)));

            DataStore dataStore = new WFSDataStoreFactory().createDataStore(params);


            //System.out.println(dataStore.getNames().));
            System.out.println("1");
            for(String name : dataStore.getTypeNames()) {
                //Query query = new DefaultQuery("gsml:Borehole");
                //FeatureReader ft = dataStore.getFeatureReader(query,Transaction.AUTO_COMMIT);
                FeatureSource fs = dataStore.getFeatureSource("gsml:Borehole");
                FeatureCollection coll = fs.getFeatures();

                System.out.println(coll.size());
                /*try {
                     int count = 0;
                     while(ft.hasNext())
                        if(ft.next()!=null)
                            count++;
                     System.out.println("Found "+count+" features");
                } catch(IOException e){
                    e.printStackTrace();
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                } catch (IllegalAttributeException e) {
                    e.printStackTrace();
                } finally {
                     ft.close();
                }*/


                /*FeatureSource featureSource = dataStore.getFeatureSource(name);
                FeatureCollection featureCollection= featureSource.getFeatures();

                while (featureCollection.iterator().hasNext()) {
                    Feature o = (Feature)featureCollection.iterator().next();
                    System.out.println(o.getName());                                 
                }*/


                //System.out.println(featureSource.getFeatures().size());
                System.out.println(name);
            }

            
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (SchemaException e) {
           // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        //}

    }
}