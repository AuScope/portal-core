/**
 * Tests that certain globally accessable URL's are available via HTTP and HTTPS
 */
Ext.define('admin.tests.ExternalConnectivity', {
    extend : 'admin.tests.SingleAJAXTest',

    getTitle : function() {
        return 'External connectivity';
    },

    getDescription : function() {
        var baseDescription = 'This test seeks to connect the backend server to a globally accessable URL via HTTP and HTTPS.';

        baseDescription += this.callParent(arguments);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        this._singleAjaxTest('testExternalConnectivity.diag');
    }
});