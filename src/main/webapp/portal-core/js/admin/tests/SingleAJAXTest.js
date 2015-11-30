/**
 * An abstract specialisation of BaseTest that involves running a test entirely through a single
 * AJAX call to the backend and then rendering the response.
 */
Ext.define('admin.tests.SingleAJAXTest', {
    extend : 'admin.tests.BaseTest',

    /**
     * The entirety of our test is making a request to the controller and parsing the response
     */
    _singleAjaxTest : function(url, params) {
        this._changeStatus(admin.tests.TestStatus.Running);

        Ext.Ajax.request({
            url : url,
            scope : this,
            params : params,
            timeout : 1000 * 60 * 20, //20 minutes
            callback : function(options, success, response) {
                if (!success) {
                    this._errors.push(response.responseText);
                    this._changeStatus(admin.tests.TestStatus.Unavailable);
                    return;
                }

                var responseObj = Ext.JSON.decode(response.responseText);
                this._handleAdminControllerResponse(responseObj);
            }
        })
    }
});