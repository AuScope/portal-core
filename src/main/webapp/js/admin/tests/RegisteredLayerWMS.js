Ext.ns('Admin.Tests');

/**
 * A test for ensuring that the WMS's that are in the registry but not part of a Known Layer are working as expected
 */
Admin.Tests.RegisteredLayerWMS = Ext.extend(Admin.Tests.SingleAJAXTest, {

    constructor : function(cfg) {
        Admin.Tests.RegisteredLayerWMS.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'Registered layer WMS availability';
    },

    getDescription : function() {
        var baseDescription = 'This tests the backend connection to all web map services that are in the registry but NOT part of a known layer. A simple GetMap and GetFeatureInfo request is with an artificial bounding box.';

        baseDescription += Admin.Tests.RegisteredLayerWMS.superclass.getDescription.call(this);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        //Init our params
        var bbox = new BBox(-31, -32, 116, 115); //rough bounds near Perth, Western Australia
        var layerNames = [];
        var serviceUrls = [];

        var onlineResources = this._getRegisteredLayerOnlineResources('WMS');
        if (onlineResources.length == 0) {
            this._changeStatus(Admin.Tests.TestStatus.Unavailable);
            return;
        }

        for (var i = 0; i < onlineResources.length; i++) {
            layerNames.push(onlineResources[i].name);
            serviceUrls.push(onlineResources[i].url);
        }

        //Run our test
        this._singleAjaxTest('testWMS.diag', {
            bbox : Ext.util.JSON.encode(bbox),
            serviceUrls : serviceUrls,
            layerNames : layerNames
        });
    }
});