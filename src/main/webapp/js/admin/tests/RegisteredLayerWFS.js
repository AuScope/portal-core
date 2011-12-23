Ext.ns('Admin.Tests');

/**
 * A test for ensuring that the WFS's that are in the registry but not part of a Known Layer are working as expected
 */
Admin.Tests.RegisteredLayerWFS = Ext.extend(Admin.Tests.SingleAJAXTest, {

    constructor : function(cfg) {
        Admin.Tests.RegisteredLayerWFS.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'Registered layer WFS availability';
    },

    getDescription : function() {
        var baseDescription = 'This tests the backend connection to all web feature services that are in the registry but NOT part of a known layer. A simple GetFeature request is made both with and without a bounding box.';

        baseDescription += Admin.Tests.RegisteredLayerWFS.superclass.getDescription.call(this);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        //Init our params
        var bbox = new BBox(-31, -32, 116, 115); //rough bounds near Perth, Western Australia
        var typeNames = [];
        var serviceUrls = [];

        var onlineResources = this._getRegisteredLayerOnlineResources('WFS');
        if (onlineResources.length == 0) {
            this._changeStatus(Admin.Tests.TestStatus.Unavailable);
            return;
        }

        for (var i = 0; i < onlineResources.length; i++) {
            typeNames.push(onlineResources[i].name);
            serviceUrls.push(onlineResources[i].url);
        }

        //Run our test
        this._singleAjaxTest('testWFS.diag', {
            bbox : Ext.util.JSON.encode(bbox),
            serviceUrls : serviceUrls,
            typeNames : typeNames
        });
    }
});