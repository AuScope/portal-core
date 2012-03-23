/**
 * A test for ensuring that the WFS's registered in Known Layers are working as expected
 */
Ext.define('admin.tests.KnownLayerWFS', {
    extend : 'admin.tests.SingleAJAXTest',

    getTitle : function() {
        return 'Known layer WFS availability';
    },

    getDescription : function() {
        var baseDescription = 'This tests the backend connection to all web feature services that belong to known layers. A simple GetFeature request is made both with and without a bounding box.';

        baseDescription += this.callParent(arguments);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        //Init our params
        var bbox = Ext.create('portal.util.BBox',{
            eastBoundLongitude : 160,
            westBoundLongitude : 110,
            northBoundLatitude : -3,
            southBoundLatitude : -47
        }); //rough bounds around Australia.
        var typeNames = [];
        var serviceUrls = [];

        var onlineResources = this._getKnownLayerOnlineResources('WFS');
        if (onlineResources.length == 0) {
            this._changeStatus(admin.tests.TestStatus.Unavailable);
            return;
        }

        for (var i = 0; i < onlineResources.length; i++) {
            typeNames.push(onlineResources[i].get('name'));
            serviceUrls.push(onlineResources[i].get('url'));
        }

        //Run our test
        this._singleAjaxTest('testWFS.diag', {
            bbox : Ext.JSON.encode(bbox),
            serviceUrls : serviceUrls,
            typeNames : typeNames
        });
    }
});