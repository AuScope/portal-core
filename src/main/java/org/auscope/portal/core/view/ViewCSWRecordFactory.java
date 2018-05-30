package org.auscope.portal.core.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWResponsibleParty;
import org.springframework.ui.ModelMap;

/**
 * A factory class for generating serializable CSWRecord objects that can be used to communicate with the view.
 *
 * @author Josh Vote
 *
 */
public class ViewCSWRecordFactory {

    /**
     * Converts a CSWRecord to its simplified view equivalent
     *
     * @param record
     * @return
     */
    public ModelMap toView(CSWRecord record) {
        ModelMap obj = new ModelMap();

        obj.put("name", record.getServiceName());
        obj.put("resourceProvider", record.getResourceProvider());
        obj.put("id", record.getFileIdentifier());
        obj.put("recordInfoUrl", record.getRecordInfoUrl());
        obj.put("description", record.getDataIdentificationAbstract());
        obj.put("noCache", record.getNoCache());
        obj.put("service", record.isService());

        CSWResponsibleParty rp = record.getContact();
        String adminArea = null;
        String contactOrg = "Unknown";
        if (rp != null) {
            if (rp.getOrganisationName() != null && !rp.getOrganisationName().isEmpty()) {
                contactOrg = rp.getOrganisationName();
            }
            adminArea = (rp.getContactInfo() == null ? null : rp.getContactInfo().getAddressAdministrativeArea());
        }
        obj.put("adminArea", adminArea);
        obj.put("contactOrg", contactOrg);

        List<Map<String, Object>> onlineResources = new ArrayList<>();
        if (record.getOnlineResources() != null) {
            for (AbstractCSWOnlineResource res : record.getOnlineResources()) {
                if (res.getLinkage() != null) {
                    onlineResources.add(this.toView(res));
                }
            }
        }
        obj.put("onlineResources", onlineResources);

        List<Map<String, Object>> geographicElements = new ArrayList<>();
        if (record.getCSWGeographicElements() != null) {
            for (CSWGeographicElement geo : record.getCSWGeographicElements()) {
                geographicElements.add(this.toView(geo));
            }
        }
        obj.put("geographicElements", geographicElements);

        List<String> descriptiveKeywords = new ArrayList<>();
        if (record.getDescriptiveKeywords() != null) {
            for (String s : record.getDescriptiveKeywords()) {
                descriptiveKeywords.add(s);
            }
        }
        obj.put("descriptiveKeywords", descriptiveKeywords);

        List<String> datasetURIs = new ArrayList<>();
        if (record.getDataSetURIs() != null) {
            for (String s : record.getDataSetURIs()) {
                datasetURIs.add(s);
            }
        }
        obj.put("datasetURIs", datasetURIs);

        List<String> constraints = new ArrayList<>();
        if (record.getConstraints() != null) {
            for (String s : record.getConstraints()) {
                constraints.add(s);
            }
        }
        obj.put("constraints", constraints);
        
        //added use limit constraints
        
        List<String> useLimitConstraints = new ArrayList<>();
        if (record.getUseLimitConstraints() != null) {
            for (String s : record.getUseLimitConstraints()) {
            	useLimitConstraints.add(s);
            }
        }
        obj.put("useLimitConstraints", useLimitConstraints);

        //added access constraints
        
        List<String> accessConstraints = new ArrayList<>();
        if (record.getAccessConstraints() != null) {
            for (String s : record.getAccessConstraints()) {
            	accessConstraints.add(s);
            }
        }
        obj.put("accessConstraints", accessConstraints);


        List<Map<String, Object>> childRecords = new ArrayList<>();
        if (record.hasChildRecords()) {
            for (CSWRecord childRecord : record.getChildRecords()) {
                childRecords.add(this.toView(childRecord));
            }
        }
        obj.put("childRecords", childRecords);

        String dateString = "";
        if (record.getDate() != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss zzz");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateString = df.format(record.getDate());
        }
        obj.put("date", dateString);

        obj.put("minScale", record.getMinScale());
        obj.put("maxScale", record.getMaxScale());

        return obj;
    }

    /**
     * Converts a CSWOnlineResource to its view equivalent
     *
     * @param res
     * @return
     */
    public ModelMap toView(AbstractCSWOnlineResource res) {
        ModelMap obj = new ModelMap();

        obj.put("url", res.getLinkage().toString());
        obj.put("type", res.getType().name());
        obj.put("name", res.getName());
        obj.put("description", res.getDescription().toString());
        obj.put("version", res.getVersion());
        obj.put("applicationProfile", res.getApplicationProfile());
        return obj;
    }

    /**
     * Converts a CSWGeographicElement to its view equivalent. If el is not a supported implementation of CSWGeographicBoundingBox a IllegalArgumentException
     * will be thrown.
     *
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
