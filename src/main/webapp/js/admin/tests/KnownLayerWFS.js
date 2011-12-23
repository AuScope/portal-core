Ext.ns('Admin.Tests');

/**
 * A test for ensuring that the WFS's registered in Known Layers are working as expected
 */
Admin.Tests.KnownLayerWFS = Ext.extend(Admin.Tests.SingleAJAXTest, {

    constructor : function(cfg) {
        Admin.Tests.KnownLayerWFS.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'Known layer WFS availability';
    },

    getDescription : function() {
        var baseDescription = 'This tests the backend connection to all web feature services that belong to known layers. A simple GetFeature request is made both with and without a bounding box.';

        baseDescription += Admin.Tests.KnownLayerWFS.superclass.getDescription.call(this);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        //Init our params
        var bbox = new BBox(-3, -47, 160, 110); //rough bounds around Australia.
        var typeNames = [];
        var serviceUrls = [];

        var onlineResources = this._getKnownLayerOnlineResources('WFS');
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