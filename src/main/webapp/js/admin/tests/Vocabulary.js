/**
 * Tests that configured vocab services are responding to basic SISSVoc requests
 */
Ext.define('admin.tests.Vocabulary', {
    extend : 'admin.tests.SingleAJAXTest',

    getTitle : function() {
        return 'Vocabulary Service';
    },

    getDescription : function() {
        var baseDescription = 'This test ensures that the backend is capable of making basic SISSVoc requests to all configured vocabulary services. The responses are also tested for validity.';

        baseDescription += this.callParent(arguments);

        return baseDescription;
    },


    /**
     * The entirety of our test is making a request to the controller and parsing the resposne
     */
    startTest : function() {
        this._singleAjaxTest('testVocabulary.diag');
    }
});