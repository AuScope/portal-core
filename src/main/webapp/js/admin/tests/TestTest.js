Ext.ns('Admin.Tests');

Admin.Tests.TestTest = Ext.extend(Admin.Tests.BaseTest, {

    constructor : function(cfg) {
        Admin.Tests.TestTest.superclass.constructor.call(this, cfg);
    },

    getTitle : function() {
        return 'This is an example test';
    },

    getDescription : function() {
        switch (this._status) {
        case Admin.Tests.TestStatus.Success:
            return 'This test was a success';
        case Admin.Tests.TestStatus.Warning:
            return 'This test was a warning';
        case Admin.Tests.TestStatus.Error:
            return 'This test was an error';
        case Admin.Tests.TestStatus.Running:
            return 'I am still running';
        default:
            return 'Not really sure what goes here....';
        }
    },


    startTest : function() {
        var me = this;

        var initTask = new Ext.util.DelayedTask(function(){
            me._changeStatus(Admin.Tests.TestStatus.Running);

            var runTask = new Ext.util.DelayedTask(function(){
                me._changeStatus(Math.floor(Math.random()*3));
            });
            runTask.delay(Math.floor(1000 + Math.random()*4000));
        });

        initTask.delay(Math.floor(1000 + Math.random()*4000));
    }
});