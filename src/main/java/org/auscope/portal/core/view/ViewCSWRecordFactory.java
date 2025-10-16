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
import org.auscope.portal.core.services.responses.csw.CSWTemporalExtent;
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
        
        // Contact Organisation, Contact Person & Adminstrative Area
        CSWResponsibleParty rp = record.getContact();
        String adminArea = null;
        String contactOrg = "Unknown";
        String contactPerson = null;
        if (rp != null) {
            if (rp.getOrganisationName() != null && !rp.getOrganisationName().isEmpty()) {
                contactOrg = rp.getOrganisationName();
            }
            if (rp.getIndividualName() != null && !rp.getIndividualName().isEmpty()) {
                contactPerson = rp.getIndividualName();
            }
            adminArea = (rp.getContactInfo() == null ? null : rp.getContactInfo().getAddressAdministrativeArea());
        }
        obj.put("adminArea", adminArea);
        obj.put("contactOrg", contactOrg);
        obj.put("contactPerson", contactPerson);
        
        // Funder
        CSWResponsibleParty frp = record.getFunder();
        String funderOrg = "Unknown";
        if (frp != null) {
        	if (frp.getOrganisationName() != null && !frp.getOrganisationName().isEmpty()) {
                funderOrg = frp.getOrganisationName();
            }
        }
        obj.put("funderOrg", funderOrg);

        // Authors
        List<String> authors = new ArrayList<>();
        if (record.getAuthors() != null) {
            for (CSWResponsibleParty authorParty : record.getAuthors()) {
                if (authorParty.getIndividualName() != null) {
                    authors.add(authorParty.getIndividualName());
                }
            }
        }
        obj.put("authors", authors);


        // Online resources
        List<Map<String, Object>> onlineResources = new ArrayList<>();
        if (record.getOnlineResources() != null) {
            for (AbstractCSWOnlineResource res : record.getOnlineResources()) {
                if (res.getLinkage() != null) {
                    onlineResources.add(this.toView(res));
                }
            }
        }
        obj.put("onlineResources", onlineResources);

        // Geographic Elements
        List<Map<String, Object>> geographicElements = new ArrayList<>();
        if (record.getCSWGeographicElements() != null) {
            for (CSWGeographicElement geo : record.getCSWGeographicElements()) {
                geographicElements.add(this.toView(geo));
            }
        }
        obj.put("geographicElements", geographicElements);
        
        // Temporal Extent
        if(record.getTemporalExtent() != null)
        	obj.put("temporalExtent", temporalExtentToView(record.getTemporalExtent()));

        // Descriptive Keywords
        List<String> descriptiveKeywords = new ArrayList<>();
        if (record.getDescriptiveKeywords() != null) {
            for (String s : record.getDescriptiveKeywords()) {
                descriptiveKeywords.add(s);
            }
        }
        obj.put("descriptiveKeywords", descriptiveKeywords);

        // Dataset URIs
        List<String> datasetURIs = new ArrayList<>();
        if (record.getDataSetURIs() != null) {
            for (String s : record.getDataSetURIs()) {
                datasetURIs.add(s);
            }
        }
        obj.put("datasetURIs", datasetURIs);

        // Constraints
        List<String> constraints = new ArrayList<>();
        if (record.getConstraints() != null) {
            for (String s : record.getConstraints()) {
                constraints.add(s);
            }
        }
        obj.put("constraints", constraints);
        
        // Use limit constraints
        List<String> useLimitConstraints = new ArrayList<>();
        if (record.getUseLimitConstraints() != null) {
            for (String s : record.getUseLimitConstraints()) {
            	useLimitConstraints.add(s);
            }
        }
        obj.put("useLimitConstraints", useLimitConstraints);

        //Access constraints
        List<String> accessConstraints = new ArrayList<>();
        if (record.getAccessConstraints() != null) {
            for (String s : record.getAccessConstraints()) {
            	accessConstraints.add(s);
            }
        }
        obj.put("accessConstraints", accessConstraints);

        // Child records
        List<Map<String, Object>> childRecords = new ArrayList<>();
        if (record.hasChildRecords()) {
            for (CSWRecord childRecord : record.getChildRecords()) {
                childRecords.add(this.toView(childRecord));
            }
        }
        obj.put("childRecords", childRecords);

        // Date
        String dateString = "";
        if (record.getDate() != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss zzz");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateString = df.format(record.getDate());
        }
        obj.put("date", dateString);

        // Min scale, max scale
        obj.put("minScale", record.getMinScale());
        obj.put("maxScale", record.getMaxScale());
        
        // Known layer ids
        List<String> knownLayerIds = new ArrayList<String>();
        if (record.getKnownLayerIds() != null && record.getKnownLayerIds().size() > 0) {
        	for (String s: record.getKnownLayerIds()) {
        		knownLayerIds.add(s);
        	}
        	obj.put("knownLayerIds", knownLayerIds);
        }
        
        // Known layer names
        List<String> knownLayerNames = new ArrayList<String>();
        if (record.getKnownLayerNames() != null && record.getKnownLayerNames().size() > 0) {
        	for (String s: record.getKnownLayerNames()) {
        		knownLayerNames.add(s);
        	}
        	obj.put("knownLayerNames", knownLayerNames);
        }
        
        // Known layer descriptions
        List<String> knownLayerDescriptions = new ArrayList<String>();
        if (record.getKnownLayerDescriptions() != null && record.getKnownLayerDescriptions().size() > 0) {
        	for (String s: record.getKnownLayerDescriptions()) {
        		knownLayerDescriptions.add(s);
        	}
        	obj.put("knownLayerDescriptions", knownLayerDescriptions);
        }

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
        obj.put("description", res.getDescription());
        obj.put("version", res.getVersion());
        obj.put("applicationProfile", res.getApplicationProfile());
        obj.put("protocolRequest", res.getProtocolRequest());
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
    
    /**
     * Converts a CSWTemporalExtent to its view equivalent. If ex is not a supported implementation of CSWTemporalExtent a IllegalArgumentException
     * will be thrown.
     *
     * @param el
     * @return
     */
    public ModelMap temporalExtentToView(CSWTemporalExtent ex) {
        ModelMap obj = new ModelMap();
        if (ex instanceof CSWTemporalExtent) {
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            CSWTemporalExtent temporalExtent = (CSWTemporalExtent) ex;
            obj.put("beginPosition", sdf.format(temporalExtent.getBeginPosition()));
            obj.put("endPosition", sdf.format(temporalExtent.getEndPosition()));
            return obj;
        } else {
            throw new IllegalArgumentException("unsupported type - " + ex.getClass());
        }
    }
}
