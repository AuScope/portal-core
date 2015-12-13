/**
 * Collates a series of Admin.Tests.BaseTest extensions into a panel for running the tests and visualising the results.
 */
Ext.define('admin.TestResultsPanel', {
    extend : 'Ext.grid.Panel',
    alias : 'widget.testresultspanel',

    /**
     * Contains the Admin.Tests.BaseTest objects mapped by their ID
     */
    _testMap : {
        //initially empty and should be populated by _initialiseTests
    },

    _cswRecordStore : null,
    _knownLayerStore : null,
    _tests : null,

    /**
     * Accepts all the configuration options of a Ext.grid.Panel with the following additions
     * {
     *  tests : String[] - the class names of every test to instantiate
     * }
     */
    constructor : function(cfg) {
        this._tests = cfg.tests;

        var testStore = Ext.create('Ext.data.Store', {
            fields: [
                {name : 'id'},  //String: The unique ID of this test.
                {name : 'title'},  //String: Text appears under the title column
                {name : 'description'},  //String: Text that appears when the row is 'expanded'
                {name : 'status'},  //Admin.Tests.TestStatus: The current test status
            ],
            proxy : {
                type : 'memory',
                reader : {
                    type : 'json',
                    idProperty : 'id',
                    rootProperty : 'data'
                }
            }
        });

        this._cswRecordStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            groupField: 'contactOrg',
            proxy : {
                type : 'ajax',
                url : 'getUnmappedCSWRecords.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            }
        });

        this._knownLayerStore = Ext.create('Ext.data.Store', {
            model : 'portal.knownlayer.KnownLayer',
            groupField: 'group',
            proxy : {
                type : 'ajax',
                url : 'getKnownLayers.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                }
            }
        });

        //Configure our test results panel
        Ext.apply(cfg, {
            store : testStore,
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl : [
                    '<p>{description}</p><br>'
                ]
            },{
                ptype: 'celltips'
            }],
            columns: [{
                    id : 'title',
                    header : 'Test',
                    flex : 1,
                    dataIndex : 'title'
                },{
                    id : 'status',
                    header: '',
                    width: 44,
                    dataIndex: 'status',
                    align: 'center',
                    hasTip : true,
                    tipRenderer : Ext.bind(function(value, testObj, column, tip) {
                        var test = this._testMap[testObj.get('id')];
                        return test.getStatusTip();
                    }, this),
                    renderer: function(value, metaData, record) {
                        switch(value) {
                        case admin.tests.TestStatus.Success:
                            return '<img src="portal-core/img/tick.png">';
                        case admin.tests.TestStatus.Warning:
                            return '<img src="portal-core/img/warning.png">';
                        case admin.tests.TestStatus.Error:
                            return '<img src="portal-core/img/exclamation.png">';
                        case admin.tests.TestStatus.Running:
                            return '<img src="portal-core/img/loading.gif">';
                        case admin.tests.TestStatus.Initialising:
                            return '<img src="portal-core/img/notloading.gif">';
                        default:
                            return '<img src="portal-core/img/cross.png">';
                        }
                    }
                }
            ],
            listeners : {
                afterrender : this._initialise
            }
        });

        this.callParent(arguments);
    },

    /**
     * Called whenever a test changes status
     */
    _onTestStatusChange : function(test, status) {
        var store = this.getStore();
        var testRecord = store.getById(test.getId());
        if (testRecord) {
            testRecord.set('description', test.getDescription());
            testRecord.set('status', status);

            store.sort('id', 'ASC'); //workaround to force recalculation of description in row expander
            this.doLayout();
        }
    },

    /**
     * Initialises the _testMap with every test this panel will use
     */
    _initialiseTests : function() {
        var addTestFn = function(panel, test) {
            test.on('statuschanged', panel._onTestStatusChange, panel);
            panel._testMap[test.getId()] = test;
        };

        //Create new test instances
        var cfg = {
            cswRecordStore : this._cswRecordStore,
            knownLayerStore : this._knownLayerStore
        };

        for (var i = 0; i < this._tests.length; i++) {
            addTestFn(this, Ext.create(this._tests[i], cfg));
        }
    },

    /**
     * Loads the internal datastore with every test
     */
    _loadTestStore : function() {
        //Load them into the datastore
        var data = [];
        for (var testId in this._testMap) {
            var test = this._testMap[testId];

            //Create our fake JSON objects to push into the datastore
            data.push({
                id : testId,
                title : test.getTitle(),
                description : test.getDescription(),
                status : admin.tests.TestStatus.Initialising //our default status
            });
        }

        //Load our data store
        var ds = this.getStore();
        ds.loadData(data);
    },

    /**
     * Runs every test contained in this panel
     */
    _runAllTests : function() {
        for (var testId in this._testMap) {
            var test = this._testMap[testId];
            test.startTest();
        }
    },

    /**
     * Call this function when the panel is ready to be loaded with tests
     */
    _initialise : function() {

        //Create tests
        this._initialiseTests();

        //Load test store
        this._loadTestStore();

        //As there is a relationship between these two stores,
        //We should refresh any GUI components whose view is dependent on these stores
        var me = this;
        me._cswRecordStore.load({callback : function(r, options, success) {
            me._knownLayerStore.load({callback : function() {
                //After loading our record/known layer stores - begin testing
                me._cswRecordStore.fireEvent('datachanged');
                me._runAllTests();
            }});
        }});
    }
});