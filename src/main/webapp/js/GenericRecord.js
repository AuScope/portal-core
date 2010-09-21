
/**
 * Represents a Generic Reocrd
 * @param 
 * @param 
 * @param 
 * @param 
 */
GenericRecord = function(title, description, contactOrg, proxyUrl, serviceType, id, typeName, serviceURLs,
		layerVisibleStatus, loadingStatus, dataSourceImage, opacity, bboxes, descriptiveKeywords) {
	this.title = title;
	this.description = description;
	this.contactOrg = contactOrg;
	this.proxyUrl = proxyUrl;
	this.serviceType = serviceType;
	this.id = id;
	this.typeName = typeName;
	this.serviceURLs = serviceURLs;
	this.layerVisibleStatus = layerVisibleStatus;
	this.loadingStatus = loadingStatus;
	this.dataSourceImage = dataSourceImage;
	this.opacity = opacity;
	this.bboxes = bboxes;
	this.descriptiveKeywords = descriptiveKeywords;
};

GenericRecord.prototype.title = "";
GenericRecord.prototype.description = "";
GenericRecord.prototype.contactOrg = "";
GenericRecord.prototype.proxyUrl = "";
GenericRecord.prototype.serviceType = "";
GenericRecord.prototype.id = "";
GenericRecord.prototype.typeName = "";
GenericRecord.prototype.serviceURLs = "";
GenericRecord.prototype.layerVisibleStatus = "";
GenericRecord.prototype.loadingStatus = "";
GenericRecord.prototype.dataSourceImage = "";
GenericRecord.prototype.opacity = "";
GenericRecord.prototype.bboxes = "";
GenericRecord.prototype.descriptiveKeywords = "";
