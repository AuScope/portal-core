Ext.ns('Admin.Tests');

/**
 * Tests that certain globally accessable URL's are available via HTTP and HTTPS
 */
Admin.Tests.ExternalConnectivity = Ext.extend(Admin.Tests.SingleAJAXTest, {

    constructor : function(cfg) {
        Admin.Tests.ExternalConnectivity.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'External connectivity';
    },

    getDescription : function() {
        var baseDescription = 'This test seeks to connect the backend server to a globally accessable URL via HTTP and HTTPS.';

        baseDescription += Admin.Tests.ExternalConnectivity.superclass.getDescription.call(this);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        this._singleAjaxTest('testExternalConnectivity.diag');
    }
});