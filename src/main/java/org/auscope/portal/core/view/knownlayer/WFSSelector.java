package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * An extension of KnownLayer that specializes into representing a collection WFS's
 * 
 * @author Josh Vote
 *
 */
public class WFSSelector implements KnownLayerSelector {

    private String featureTypeName;
    private String[] serviceEndpoints;
    private boolean includeEndpoints;
    private String[] relatedFeatureTypeNames;

    /**
     * @param featureTypeName
     *            The feature type name used to identify members of this layer
     */
    public WFSSelector(String featureTypeName) {
        this.featureTypeName = featureTypeName;
    }

    /**
     * @param featureTypeName
     *            The feature type name used to identify members of this layer
     * @param serviceEndpoints
     *            A list of the end points that will either be included or excluded from the WFS, depending on the value of includeEndpoints
     * @param includeEndpoints
     *            A flag indicating whether the listed service end points will be included or excluded from the WFS
     */
    public WFSSelector(String featureTypeName, String[] serviceEndpoints, boolean includeEndpoints) {
        this(featureTypeName);
        this.serviceEndpoints = serviceEndpoints;
        this.includeEndpoints = includeEndpoints;
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public String[] getServiceEndpoints() {
        return serviceEndpoints;
    }

    public boolean includeEndpoints() {
        return includeEndpoints;
    }

    /**
     * @return the relatedFeatureTypeNames
     */
    public String[] getRelatedFeatureTypeNames() {
        return relatedFeatureTypeNames;
    }

    /**
     * @param relatedFeatureTypeNames
     *            the relatedFeatureTypeNames to set
     */
    public void setRelatedFeatureTypeNames(String[] relatedFeatureTypeNames) {
        this.relatedFeatureTypeNames = relatedFeatureTypeNames;
    }

    /**
     * Compares records based on related feature type names
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        AbstractCSWOnlineResource[] wfsResources = record.getOnlineResourcesByType(OnlineResourceType.WFS);

        //Check for strong association to begin with
        for (AbstractCSWOnlineResource onlineResource : wfsResources) {
            if (featureTypeName.equals(onlineResource.getName())) {
                //OK we have a match, check we don't explicitly/implicitly exclude it
                //based on its URL
                if (serviceEndpoints != null && serviceEndpoints.length > 0) {
                    boolean matched = false;
                    for (String url : serviceEndpoints) {
                        if (url.equals(onlineResource.getLinkage().toString())) {
                            matched = true;
                            break;
                        }
                    }

                    //Our list of endpoints will be saying either
                    //'Include only this list of urls'
                    //'Exclude any of these urls'
                    if ((includeEndpoints && matched) ||
                            (!includeEndpoints && !matched)) {
                        return RelationType.Belongs;
                    }
                } else {
                    //Otherwise this knownlayer makes no restrictions on URL
                    return RelationType.Belongs;
                }
            }
        }

        //next we check for a weaker relation (does the type name exist in
        //the list of related feature type names?)
        if (relatedFeatureTypeNames != null) {
            for (String relatedType : relatedFeatureTypeNames) {
                for (AbstractCSWOnlineResource onlineResource : wfsResources) {
                    if (onlineResource.getName().equals(relatedType)) {
                        return RelationType.Related;
                    }
                }
            }
        }

        return RelationType.NotRelated;
    }

}
