/**
 * A test for ensuring that the WMS's that are in the registry but not part of a Known Layer are working as expected
 */
Ext.define('admin.tests.RegisteredLayerWMS', {
    extend : 'admin.tests.SingleAJAXTest',

    getTitle : function() {
        return 'Registered layer WMS availability';
    },

    getDescription : function() {
        var baseDescription = 'This tests the backend connection to all web map services that are in the registry but NOT part of a known layer. A simple GetMap and GetFeatureInfo request is with an artificial bounding box.';

        baseDescription += this.callParent(arguments);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        //Init our params
        var bbox = Ext.create('portal.util.BBox',{
            eastBoundLongitude : 116,
            westBoundLongitude : 115,
            northBoundLatitude : -31,
            southBoundLatitude : -32
        }); //rough bounds near Perth, WA
        var layerNames = [];
        var serviceUrls = [];

        var onlineResources = this._getCSWRecordOnlineResources('WMS');
        if (onlineResources.length == 0) {
            this._changeStatus(admin.tests.TestStatus.Unavailable);
            return;
        }

        for (var i = 0; i < onlineResources.length; i++) {
            layerNames.push(onlineResources[i].get('name'));
            serviceUrls.push(onlineResources[i].get('url'));
        }

        //Run our test
        this._singleAjaxTest('testWMS.diag', {
            bbox : Ext.JSON.encode(bbox),
            serviceUrls : serviceUrls,
            layerNames : layerNames
        });
    }
});