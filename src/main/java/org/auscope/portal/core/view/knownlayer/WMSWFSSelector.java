package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

public class WMSWFSSelector implements KnownLayerSelector {

	private String featureTypeName;
	private String layerName;

	private String[] relatedRecords;
	
	private String[] serviceEndpoints;
	private boolean includeEndpoints;

	public WMSWFSSelector(String featureTypeName, String layerName) {
		this.featureTypeName = featureTypeName;
		this.layerName = layerName;
	}
	
	public WMSWFSSelector(String featureTypeName, String layerName, String[] serviceEndpoints,
			boolean includeEndpoints) {

		this.featureTypeName = featureTypeName;
		this.layerName = layerName;
		this.serviceEndpoints = serviceEndpoints;
		this.includeEndpoints = includeEndpoints;
	}

	@Override
	public RelationType isRelatedRecord(CSWRecord record) {
		AbstractCSWOnlineResource[] wmsResources = record.getOnlineResources();

		// Check for strong association to begin with
		for (AbstractCSWOnlineResource onlineResource : wmsResources) {
			if (layerName.equals(onlineResource.getName()) || featureTypeName.equals(onlineResource.getName())) {
				// OK we have a match, check we don't explicitly/implicitly
				// exclude it
				// based on its URL
				if (serviceEndpoints != null && serviceEndpoints.length > 0) {
					boolean matched = false;
					for (String url : serviceEndpoints) {
						if (url.equals(onlineResource.getLinkage().toString())) {
							matched = true;
							break;
						}
					}

					// Our list of endpoints will be saying either
					// 'Include only this list of urls'
					// 'Exclude any of these urls'
					if ((includeEndpoints && matched) || (!includeEndpoints && !matched)) {
						return RelationType.Belongs;
					}
				} else {
					// Otherwise this knownlayer makes no restrictions on URL
					return RelationType.Belongs;
				}
			}
		}

		
		// next we check for a weaker relation (does the type name exist in
		// the list of related feature type names?)
		if (relatedRecords != null) {
			for (String relatedRecord : relatedRecords) {
				for (AbstractCSWOnlineResource onlineResource : wmsResources) {
					if (onlineResource.getName().equals(relatedRecord)) {
						return RelationType.Related;
					}
				}
			}
		}

		return RelationType.NotRelated;
	}

	

	

	public String getFeatureTypeName() {
		return featureTypeName;
	}

	public void setFeatureTypeName(String featureTypeName) {
		this.featureTypeName = featureTypeName;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

}
