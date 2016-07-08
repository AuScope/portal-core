/**
 * An abstract class represent a 'hash map esque' class
 * representing a custom filter that may be applied a layer
 * for the purposes of subsetting it's referenced data sources
 *
 * events :
 *      change(portal.layer.filterer.Filterer this, String[] keys)
 *          Fired whenever the filter changes, passed an array of all keys that have changed.
 */
Ext.define('portal.layer.filterer.Filterer', {
    extend: 'portal.util.ObservableMap',

    statics : {
        BBOX_FIELD : 'bbox' //the portal wide name for a bounding box field
    },

    config : {
        spatialParam : null //this should always be an instance of portal.util.BBox
    },

    constructor: function(config){     
        this.callParent(arguments);       
    },


    /**
     * Gets the set of parameters configured within this map as
     * a simple javascript object with key/value pairs
     *
     * The spatial component will be written to portal.layer.filterer.SpatialFilterer.BBOX_FIELD
     *
     * returns - a javascript object
     */
    getParameters : function() {
        var params = this.callParent(arguments);

        var bbox = this.getSpatialParam();
        if (bbox) {
            params[portal.layer.filterer.Filterer.BBOX_FIELD] = Ext.JSON.encode(bbox);
        }

        return params;
    },

    /**
     * Gets the set of parameters configured within this map as
     * a simple javascript object with key/value pairs
     *
     * The spatial component will be written to portal.layer.filterer.SpatialFilterer.BBOX_FIELD
     *
     * returns - a javascript object
     */
    getMercatorCompatibleParameters : function() {
        var params = this.getParameters();

        var bbox = this.getSpatialParam();            	

        if(bbox) {
            if (bbox.crs=='EPSG:4326') {
                var bounds = new OpenLayers.Bounds(bbox.westBoundLongitude, bbox.southBoundLatitude, bbox.eastBoundLongitude, bbox.northBoundLatitude);
                bounds = bounds.transform('EPSG:4326','EPSG:3857');
                bbox = Ext.create('portal.util.BBox', {
                    northBoundLatitude : bounds.top,
                    southBoundLatitude : bounds.bottom,
                    eastBoundLongitude : bounds.right,
                    westBoundLongitude : bounds.left,
                    crs : 'EPSG:3857'
                });
            }
            
            params[portal.layer.filterer.Filterer.BBOX_FIELD] = Ext.JSON.encode(bbox);
        }

        return params;
    },

    /**
     * Sets the internal bbox field with value - value can be a Object, BBox or JSON String
     */
    applySpatialParam : function(value) {
        if (!value) {
            return value;
        }

        if (value instanceof portal.util.BBox) {
            return value;
        }

        //Any string should be a JSON string
        if (Ext.isString(value)) {
            value = Ext.JSON.decode(value);
        }

        if (Ext.isObject(value)) {
            return Ext.create('portal.util.BBox', value);
        }

        throw 'unable to parse value';
    },

    /**
     * Given a set of parameters as a plain old javascript object of
     * key/value pairs, apply it's contents to this map.
     *
     * This is a useful function if you want to set multiple parameters
     * and only raise a single event
     *
     * Any parameter named portal.layer.filterer.SpatialFilterer.BBOX_FIELD will be treated as a special
     * case and
     *
     * parameters - a plain old javascript object
     * clearFirst - [Optional] if true, then the internal map will be cleared BEFORE any values are added
     */
    setParameters : function(parameters, clearFirst) {
        //Get rid of the 'bbox' field, we treat it as a special case
        var noBboxParams = Ext.apply({}, parameters);
        var bbox = parameters.bbox;
        noBboxParams[portal.layer.filterer.Filterer.BBOX_FIELD] = undefined;

        //Only apply the bbox value (undefined, null or otherwise) if
        //    a) we are explicitly clearing values - it doesn't matter what bbox's value is
        //       it will overwrite the internal BBOX_FIELD
        //    b) the bbox value has been explicitly included in parameters
        //In the event that an empty object is passed to parameters '{}' we don't wan't to be assigning
        //'undefined' to bbox as it is inconsistent with the behaviour of other parameters
        if (portal.layer.filterer.Filterer.BBOX_FIELD in parameters || clearFirst) {
            this.setSpatialParam(bbox, true);
        }

        //Proceed normally
        this.callParent([noBboxParams, clearFirst]);
    },

    /**
     * Sets a single parameter of this map
     *
     * key - a string key whose value will be set. Will override any existing key of the same name
     * value - The object value to set
     * quiet[optional] - qu
     */
    setParameter : function(key, value, quiet){
        if (key === portal.layer.filterer.Filterer.BBOX_FIELD) {
            this._setBboxField(value);
            this.fireEvent('change', this, [key]);
        } else {
            this.callParent(arguments);
        }
    },

    /**
     * Gets the value of the specified key as an Object
     *
     * key - A string key whose value will be fetched.
     *
     * returns - a javascript object matching key
     */
    getParameter : function(key) {
        if (key === portal.layer.filterer.Filterer.BBOX_FIELD) {
            if (this.bbox) {
                return Ext.JSON.encode(this.getSpatialParam());
            } else {
                return undefined;
            }
        } else {
            return this.callParent(arguments);
        }
    },

    /**
     * Sets the value of the internal spatialParam and fires a change event
     * (unless suppressEvents is specified)
     */
    setSpatialParam : function(spatialParam, suppressEvents) {
        this.spatialParam = this.applySpatialParam(spatialParam);

        if (!suppressEvents) {
            this.fireEvent('change', this, [portal.layer.filterer.Filterer.BBOX_FIELD]);
        }
    },
    
    /**
     * Sets the value of the internal spatialParam and fires a change event
     * (unless suppressEvents is specified)
     */
    getSpatialParam : function() {
        return this.spatialParam;
    },

    /**
     * Returns a shallow clone of this filterer with the exception of the spatial params,
     * in which a true copy is returned
     */
    clone : function() {
        var clonedObj = Ext.create('portal.layer.filterer.Filterer', {});
        var thisBBox = this.getSpatialParam();

        Ext.apply(clonedObj.parameters, this.parameters);
        clonedObj.setSpatialParam(thisBBox ? thisBBox.clone() : thisBBox, true);

        return clonedObj;
    }
});