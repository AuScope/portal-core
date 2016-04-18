package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

public class WMSWFSSelector implements KnownLayerSelector {

	private String featureTypeName;
	private String layerName;

	public WMSWFSSelector(String featureTypeName, String layerName) {
		this.featureTypeName = featureTypeName;
		this.layerName = layerName;
	}

	@Override
	public RelationType isRelatedRecord(CSWRecord record) {
		AbstractCSWOnlineResource[] wmsResources = record.getOnlineResources();

		// Check for strong association to begin with
		for (AbstractCSWOnlineResource onlineResource : wmsResources) {
			if (layerName.equals(onlineResource.getName()) || featureTypeName.equals(onlineResource.getName())) {

				return RelationType.Belongs;

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
