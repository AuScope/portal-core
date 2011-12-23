Ext.ns('Admin.Tests');

/**
 * An abstract specialisation of BaseTest that involves running a test entirely through a single
 * AJAX call to the backend and then rendering the response.
 */
Admin.Tests.SingleAJAXTest = Ext.extend(Admin.Tests.BaseTest, {

    constructor : function(cfg) {
        Admin.Tests.SingleAJAXTest.superclass.constructor.call(this, cfg);
    },

    /**
     * The entirety of our test is making a request to the controller and parsing the response
     */
    _singleAjaxTest : function(url, params) {
        this._changeStatus(Admin.Tests.TestStatus.Running);

        Ext.Ajax.request({
            url : url,
            scope : this,
            params : params,
            timeout : 1000 * 60 * 20, //20 minutes
            callback : function(options, success, response) {
                if (!success) {
                    this._errors.push(response.responseText);
                    this._changeStatus(Admin.Tests.TestStatus.Unavailable);
                    return;
                }

                var responseObj = Ext.util.JSON.decode(response.responseText);
                this._handleAdminControllerResponse(responseObj);
            }
        })
    }
});