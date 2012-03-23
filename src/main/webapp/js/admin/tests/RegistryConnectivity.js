/**
 * Tests that configured registries are responding to basic CSW requests
 */
Ext.define('admin.tests.RegistryConnectivity', {
    extend : 'admin.tests.SingleAJAXTest',

    getTitle : function() {
        return 'Registry connectivity';
    },

    getDescription : function() {
        var baseDescription = 'This test ensures that the backend is capable of making basic CSW requests to all configured registries. The registry responses are also tested for validity.';

        baseDescription += this.callParent(arguments);

        return baseDescription;
    },

    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        this._singleAjaxTest('testCSWConnectivity.diag');
    }
});