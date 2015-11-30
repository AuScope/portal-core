/**
 * Represents a simple Bounding Box
 * {
 * northBoundLatitude : Northern most latitude
 * southBoundLatitude : Southern most latitude
 * eastBoundLongitude : Eastern most longitude (in the range [-180, 180) )
 * westBoundLongitude : Western most longitude (in the range [-180, 180) )
 * crs : Coordinate reference system
 * }
 */
Ext.define('portal.util.BBox', {
    constructor : function(cfg) {
        Ext.apply(this, cfg);
        if (!this.crs) {
            this.crs = 'EPSG:4326';
        }

        this.callParent(arguments);
    },
    
    //VT: when longtitude goes over 180, geoserver process it the other way around the globe.
    statics : {
        datelineCorrection : function(eastBound,epsg){
            eastBound = parseInt(eastBound);
            if(eastBound < -120 && epsg=="EPSG:4326"){
                eastBound = 180 + (180 + eastBound);
            }
            return eastBound
        }
    },

    /**
     * Returns true if the bounding box spans the entire planet
     */
    isGlobal : function() {
        return this.eastBoundLongitude === 180 && this.northBoundLatitude === 90 &&
        this.southBoundLatitude === -90 && this.westBoundLongitude === -180;
    },

    /**
     * Returns a clone of this bounding box
     */
    clone : function() {
        return Ext.create('portal.util.BBox', {
            northBoundLatitude : this.northBoundLatitude,
            southBoundLatitude : this.southBoundLatitude,
            eastBoundLongitude : this.eastBoundLongitude,
            westBoundLongitude : this.westBoundLongitude,
            crs : this.crs
        });
    },

    /**
     * Combines this bounding box with the specified bbox by taking the maxima/minima of both bounding boxes.
     *
     * The 'super' bounding box will be returned as a BBox
     */
    combine : function(bbox) {
        return Ext.create('portal.util.BBox', {
            northBoundLatitude : Math.max(this.northBoundLatitude, bbox.northBoundLatitude),
            southBoundLatitude : Math.min(this.southBoundLatitude, bbox.southBoundLatitude),
            eastBoundLongitude : Math.max(this.eastBoundLongitude, bbox.eastBoundLongitude),
            westBoundLongitude : Math.min(this.westBoundLongitude, bbox.westBoundLongitude),
            crs : this.crs
        });
    },

    /**
     * Returns a list of BBox objects representing the bbox being split into 2 at the
     * specified latitude and longitudes (Will return 1,2 or 4 bbox objects)
     * @param longitude [Optional] The longitude to split at in the range [-180, 180)
     * @param latitude [Optional] The latitude to split at
     * @return
     */
    splitAt : function(longitude, latitude) {
        var splitter = function (left, right, value, splits) {
            var newSplits = [];

            for (var i = 0; i < splits.length; i++) {
                var bbox = splits[i];
                var leftSplit = bbox.clone();
                var rightSplit = bbox.clone();

                //If we split across a range that sees a sign flip
                //ensure the sign across each split rectangle is equal
                var leftSplitValue = value;
                while (leftSplitValue < 0 && leftSplit[left] > 0) {
                    leftSplitValue += 360;
                }
                while (leftSplitValue > 0 && leftSplit[left] < 0) {
                    leftSplitValue -= 360;
                }
                var rightSplitValue = value;
                while (rightSplitValue < 0 && rightSplit[right] > 0) {
                    rightSplitValue += 360;
                }
                while (rightSplitValue > 0 && rightSplit[right] < 0) {
                    rightSplitValue -= 360;
                }

                leftSplit[left] = bbox[left];
                leftSplit[right] = leftSplitValue;
                rightSplit[left] = rightSplitValue;
                rightSplit[right] = bbox[right];

                newSplits.push(leftSplit);
                newSplits.push(rightSplit);
            }

            return newSplits;
        };

        var splits = [this];

        if (longitude !== undefined) {
            splits = splitter('westBoundLongitude', 'eastBoundLongitude', longitude, splits);
        }

        if (latitude !== undefined) {
            splits = splitter('northBoundLatitude', 'southBoundLatitude', latitude, splits);
        }

        return splits;
    },

    /**
     * Recursively splits the specified bbox
     *
     * bbox : The bounding box to split
     * resultList : A list that the results will be appended to
     */
    _splitBboxes : function(bbox, resultList) {

        //SPLIT CASE 1: Polygon crossing meridian
        if (bbox.westBoundLongitude < 0 && bbox.eastBoundLongitude > 0) {
            var splits = bbox.splitAt(0);
            for (var i = 0; i < splits.length; i++) {
                this._splitBboxes(splits[i], resultList);
            }
            return resultList;
        }

        //SPLIT CASE 2: Polygon crossing anti meridian
        if (bbox.westBoundLongitude > 0 && bbox.eastBoundLongitude < 0) {
            var splits = bbox.splitAt(-180);
            for (var i = 0; i < splits.length; i++) {
                this._splitBboxes(splits[i], resultList);
            }
            return resultList;
        }

        //SPLIT CASE 3: Polygon is too wide (Gmap can't handle click events for wide polygons)
        //VT: There has been alot of work that have gone into calculating these bounding box and I am not sure why.
        //it may be due to some legacy map bug but I am unable to replicate it.

//        if (Math.abs(bbox.westBoundLongitude - bbox.eastBoundLongitude) > 60) {
//            var splits = bbox.splitAt((bbox.westBoundLongitude + bbox.eastBoundLongitude) / 2);
//            for (var i = 0; i < splits.length; i++) {
//                this._splitBboxes(splits[i], resultList);
//            }
//            return resultList;
//        }

        //OTHERWISE - bounding box is OK to render
        resultList.push(bbox);
        return resultList;
    },

    /**
     * Converts a portal bbox into an array of portal.map.primitives.Polygon objects as instantiated
     * by the specified portal.map.BaseMap
     *
     * Normally a single polygon is returned but if the polygon wraps around the antimeridian, it will be split
     * around the meridians.
     *
     * @param baseMap A portal.map.BaseMap instance
     * @param strokeColor String The color of the vertices (CSS color string)
     * @param strokeWeight Number The width of the vertices
     * @param strokeOpacity Number The transparency of the vertices [0, 1]
     * @param fillColor String The color of the fill (CSS color string)
     * @param fillOpacity Number The transparency of the fill [0, 1]
     * @param opts Object - additional GMap config options
     * @param id [Optional] String ID of generated polygons
     * @param cswRecord [Optional] portal.csw.CSWRecord parent of these polygons
     * @param sourceOnlineResource [Optional] portal.csw.OnlineResource parent of these polygons
     * @param sourceLayer [Optional] portal.layer.Layer parent of these polygons
     */
    toPolygon : function(baseMap, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity, opts,
                             id, cswRecord, sourceOnlineResource, sourceLayer) {
        var splits = this._splitBboxes(this, []);
        var result = [];

        for (var i = 0; i < splits.length; i++) {
            var splitBbox = splits[i];
            var ne = Ext.create('portal.map.Point', {latitude : splitBbox.northBoundLatitude, longitude : splitBbox.eastBoundLongitude});
            var se = Ext.create('portal.map.Point', {latitude : splitBbox.southBoundLatitude, longitude : splitBbox.eastBoundLongitude});
            var sw = Ext.create('portal.map.Point', {latitude : splitBbox.southBoundLatitude, longitude : splitBbox.westBoundLongitude});
            var nw = Ext.create('portal.map.Point', {latitude : splitBbox.northBoundLatitude, longitude : splitBbox.westBoundLongitude});

            result.push(baseMap.makePolygon(id, cswRecord, sourceOnlineResource, sourceLayer,
                    [sw, nw, ne, se, sw], strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity, opts));
        }

        return result;
    },

    /**
     * Returns true IFF both bboxes share the same CRS and that they intersect one another.
     * Algorithm sourced from - http://tekpool.wordpress.com/2006/10/11/rectangle-intersection-determine-if-two-given-rectangles-intersect-each-other-or-not/
     */
    intersects : function(bbox) {
        if (this.crs !== bbox.crs) {
            return false;
        }

        //If a bbox wraps the international date line such that east is in fact less than west
        //We should split the wrapping bbox at the dateline for an easier comparison
        var bboxEast = bbox.eastBoundLongitude;
        var bboxWest = bbox.westBoundLongitude;
        var thisEast = this.eastBoundLongitude;
        var thisWest = this.westBoundLongitude;
        
        if (bboxEast < bboxWest) {
            var left = Ext.create('portal.util.BBox',{
                westBoundLongitude : bboxWest, 
                eastBoundLongitude : 180, 
                southBoundLatitude : bbox.southBoundLatitude, 
                northBoundLatitude : bbox.northBoundLatitude
            });
            var right = Ext.create('portal.util.BBox',{
                westBoundLongitude : -180, 
                eastBoundLongitude : bboxEast, 
                southBoundLatitude : bbox.southBoundLatitude, 
                northBoundLatitude : bbox.northBoundLatitude
            });
            
            return this.intersects(left) || this.intersects(right);
        }
        if (thisEast < thisWest) {
            var left = Ext.create('portal.util.BBox',{
                westBoundLongitude : thisWest, 
                eastBoundLongitude : 180, 
                southBoundLatitude : this.southBoundLatitude, 
                northBoundLatitude : this.northBoundLatitude
            });
            var right = Ext.create('portal.util.BBox',{
                westBoundLongitude : -180, 
                eastBoundLongitude : thisEast, 
                southBoundLatitude : this.southBoundLatitude, 
                northBoundLatitude : this.northBoundLatitude
            });
            
            return left.intersects(bbox) || right.intersects(bbox);
        }

        return !(bboxWest > thisEast
                || bboxEast < thisWest
                || bbox.southBoundLatitude > this.northBoundLatitude
                || bbox.northBoundLatitude < this.southBoundLatitude);
    },

    /**
     * Returns true of the specified lat/long is contained by this BBox
     */
    contains : function(latitude, longitude) {
        return this.westBoundLongitude <= longitude &&
                this.eastBoundLongitude >= longitude &&
                this.southBoundLatitude <= latitude &&
                this.northBoundLatitude >= latitude;
    },

    /**
     * Returns true IFF this bounding box completely encloses the specified BBBox
     */
    containsBbox : function(bbox) {
        if (this.crs !== bbox.crs) {
            return false;
        }

        return (this.contains(bbox.northBoundLatitude, bbox.westBoundLongitude) &&
               this.contains(bbox.southBoundLatitude, bbox.eastBoundLongitude));
    },

    /**
     * Function for comparing 2 instances of BBox. If the internal fields are all exactly the same then
     * true will be returned, otherwise false.
     */
    equals : function(bbox) {
        return this.eastBoundLongitude === bbox.eastBoundLongitude &&
            this.westBoundLongitude === bbox.westBoundLongitude &&
            this.southBoundLatitude === bbox.southBoundLatitude &&
            this.northBoundLatitude === bbox.northBoundLatitude &&
            this.crs === bbox.crs;
    },

    /**
     * Covert this bounding box to a new transformation
     */
    transform : function(bbox,newCrs) {
        var bounds = new OpenLayers.Bounds(bbox.westBoundLongitude, bbox.southBoundLatitude, bbox.eastBoundLongitude, bbox.northBoundLatitude);
        bounds.transform(this.crs,newCrs);
        var newBbox = Ext.create('portal.util.BBox', {
            eastBoundLongitude : bounds.right,
            westBoundLongitude : bounds.left,
            northBoundLatitude : bounds.top,
            southBoundLatitude : bounds.bottom
        });
        newBbox.crs=newCrs;
        return newBbox;
    }
});
