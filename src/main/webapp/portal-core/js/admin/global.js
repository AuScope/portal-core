/**
 * Define global functions that exist in the namespace (eg. Object.prototype)
 */

/**
 * ObjectId function so all Objects will have a uniqueId.  The method get() will exist on all objects to return this.
 * From http://stackoverflow.com/a/7579956/1019307.
 * 
 * Browser compatibility:
 * 
 * Firefox (Gecko)  Chrome  Internet Explorer   Opera   Safari
 *  4.0               5      9                   11.60   5.1
 */
(function() {
    var id_counter = 1;
    Object.defineProperty(Object.prototype, "__uniqueId", {
        writable: true
    });
    Object.defineProperty(Object.prototype, "uniqueId", {
        get: function() {
            if (this.__uniqueId == undefined)
                this.__uniqueId = id_counter++;
            return this.__uniqueId;
        }
    });
}());

// extend function for objects so can do (http://stackoverflow.com/questions/929776/merging-associative-arrays-javascript) 
// var arr1　= { robert: "bobby", john: "jack" };
// var arr2 = { elizabeth: "liz", jennifer: "jen" };
// var shortnames = Object.extend(arr1,arr2);

Object.extend = function(destination, source) {
    for (var property in source) {
        if (source.hasOwnProperty(property)) {
            destination[property] = source[property];
        }
    }
    return destination;
};

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

// Initialisse tooltip manager for eg. actioncolumn's getTip
Ext.tip.QuickTipManager.init();
