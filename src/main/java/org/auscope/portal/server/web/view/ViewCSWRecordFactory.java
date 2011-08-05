package org.auscope.portal.server.web.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.auscope.portal.csw.record.CSWGeographicElement;
import org.auscope.portal.csw.record.CSWOnlineResource;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.csw.record.CSWResponsibleParty;
import org.springframework.stereotype.Repository;
import org.springframework.ui.ModelMap;

/**
 * A factory class for generating serializable CSWRecord objects that can be used to communicate with the view.
 * @author vot002
 *
 */
@Repository
public class ViewCSWRecordFactory {

    /**
     * Converts a CSWRecord to its simplified view equivalent
     * @param record
     * @return
     */
    public ModelMap toView(CSWRecord record) {
        ModelMap obj = new ModelMap();

        obj.put("serviceName", record.getServiceName());
        obj.put("resourceProvider", record.getResourceProvider());
        obj.put("fileIdentifier", record.getFileIdentifier());
        obj.put("recordInfoUrl", record.getRecordInfoUrl());
        obj.put("dataIdentificationAbstract", record.getDataIdentificationAbstract());

        CSWResponsibleParty rp = record.getContact();
        if (rp != null) {
            obj.put("contactOrganisation", rp.getOrganisationName());
        } else {
            obj.put("contactOrganisation", "");
        }

        List<Map<String, Object>> onlineResources = new ArrayList<Map<String, Object> >();
        if (record.getOnlineResources() != null ) {
            for (CSWOnlineResource res : record.getOnlineResources()) {
                if (res.getLinkage() != null) {
                    onlineResources.add(this.toView(res));
                }
            }
        }
        obj.put("onlineResources", onlineResources);


        List<Map<String, Object> > geographicElements = new ArrayList<Map<String, Object> >();
        if (record.getCSWGeographicElements() != null) {
            for (CSWGeographicElement geo : record.getCSWGeographicElements()) {
                geographicElements.add(this.toView(geo));
            }
        }
        obj.put("geographicElements", geographicElements);


        List<String> descriptiveKeywords = new ArrayList<String>();
        if (record.getDescriptiveKeywords() != null ) {
            for (String s : record.getDescriptiveKeywords()) {
                descriptiveKeywords.add(s);
            }
        }
        obj.put("descriptiveKeywords", descriptiveKeywords);

        List<String> constraints = new ArrayList<String>();
        if(record.getConstraints() != null) {
            for (String s : record.getConstraints()) {
                constraints.add(s);
            }
        }
        obj.put("constraints", constraints);

        return obj;
    }

    /**
     * Converts a CSWOnlineResource to its view equivalent
     * @param res
     * @return
     */
    public ModelMap toView(CSWOnlineResource res) {
        ModelMap obj = new ModelMap();

        obj.put("url", res.getLinkage().toString());
        obj.put("onlineResourceType", res.getType().name());
        obj.put("name", res.getName());
        obj.put("description", res.getDescription().toString());

        return obj;
    }

    /**
     * Converts a CSWGeographicElement to its view equivalent. If el is not a supported
     * implementation of CSWGeographicBoundingBox a IllegalArgumentException will be thrown.
     * @param el
     * @return
     */
    public ModelMap toView(CSWGeographicElement el) {
        ModelMap obj = new ModelMap();

        if (el instanceof CSWGeographicBoundingBox) {

            CSWGeographicBoundingBox bbox = (CSWGeographicBoundingBox) el;
            obj.put("type", "bbox");
            obj.put("eastBoundLongitude", bbox.getEastBoundLongitude());
            obj.put("westBoundLongitude", bbox.getWestBoundLongitude());
            obj.put("northBoundLatitude", bbox.getNorthBoundLatitude());
            obj.put("southBoundLatitude", bbox.getSouthBoundLatitude());

            return obj;
        } else {
            throw new IllegalArgumentException("unsupported type - " + el.getClass());
        }
    }
}
