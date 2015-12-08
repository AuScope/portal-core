/**
 * A specialisation of portal.widgets.panel.BaseRecordPanel for rendering
 * records conforming to the portal.knownlayer.KnownLayer Model
 */
Ext.define('portal.widgets.panel.KnownLayerPanel', {
    extend : 'portal.widgets.panel.BaseRecordPanel',

    constructor : function(cfg) {
        this.callParent(arguments);
    },

  

    /**
     * Implements method - see parent class for details.
     */
    getTitleForRecord : function(record) {
        return record.get('name');
    },

    /**
     * Implements method - see parent class for details.
     */
    getCSWRecordsForRecord : function(record) {
        return record.get('cswRecords');
    },

    /**
     * Implements method - see parent class for details.
     */
    getOnlineResourcesForRecord : function(record) {
        var onlineResources = [];
        var cswRecords = record.get('cswRecords');

        for (var i = 0; i < cswRecords.length; i++) {
            onlineResources = onlineResources.concat(cswRecords[i].getAllChildOnlineResources());
        }

        return onlineResources;
    },

    /**
     * Implements method - see parent class for details.
     */
    getSpatialBoundsForRecord : function(record) {
        var bboxes = [];
        var cswRecords = record.data.cswRecords;

        for (var i = 0; i < cswRecords.length; i++) {
            if(cswRecords[i].get('noCache')==true){
                bboxes = bboxes.concat(this.getWholeGlobeBounds());
            }else{
                bboxes = bboxes.concat(cswRecords[i].get('geographicElements'));
            }
        }

        return bboxes;
    }
});