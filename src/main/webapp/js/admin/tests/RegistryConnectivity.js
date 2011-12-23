Ext.ns('Admin.Tests');

/**
 * Tests that configured registries are responding to basic CSW requests
 */
Admin.Tests.RegistryConnectivity = Ext.extend(Admin.Tests.SingleAJAXTest, {

    constructor : function(cfg) {
        Admin.Tests.RegistryConnectivity.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'Registry connectivity';
    },

    getDescription : function() {
        var baseDescription = 'This test ensures that the backend is capable of making basic CSW requests to all configured registries. The registry responses are also tested for validity.';

        baseDescription += Admin.Tests.RegistryConnectivity.superclass.getDescription.call(this);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        this._singleAjaxTest('testCSWConnectivity.diag');
    }
});