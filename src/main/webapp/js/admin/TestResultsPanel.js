/**
 * Collates a series of Admin.Tests.BaseTest extensions into a panel for running the tests and visualising the results.
 */
Ext.ns('Admin');
Admin.TestResultsPanel = Ext.extend(Ext.grid.GridPanel, {
    /**
     * Contains the Admin.Tests.BaseTest objects mapped by their ID
     */
    _testMap : {
        //initially empty and should be populated by _initialiseTests
    },

    _cswRecordStore : null,
    _knownLayerStore : null,

    /**
     * Accepts all the configuration options of a Ext.grid.GridPanel with the following additions
     * {
     * }
     */
    constructor : function(cfg) {
        var testStore = new Ext.data.Store({
            reader      : new Ext.data.JsonReader({
                idProperty      : 'id',
                root            : 'data',
                fields          : [
                    {name : 'id'},  //String: The unique ID of this test.
                    {name : 'title'},  //String: Text appears under the title column
                    {name : 'description'},  //String: Text that appears when the row is 'expanded'
                    {name : 'status'},  //Admin.Tests.TestStatus: The current test status
                ]
            })
        });

        this._cswRecordStore = new CSWRecordStore('getCSWRecords.do');
        this._knownLayerStore = new KnownLayerStore('getKnownLayers.do');

        var rowExpander = new Ext.grid.RowExpander({
            tpl : new Ext.Template('<p>{description}</p><br>'),
            enableCaching : false
        });

        Ext.apply(cfg, {
            plugins : [rowExpander],
            store : testStore,
            autoExpandColumn : 'title',
            columns: [
                rowExpander,
                {
                    id : 'title',
                    header : 'Test',
                    dataIndex : 'title'
                },{
                    id : 'status',
                    header: '',
                    width: 44,
                    dataIndex: 'status',
                    align: 'center',
                    renderer: function(value, metaData, record) {
                        switch(value) {
                        case Admin.Tests.TestStatus.Success:
                            return '<img src="img/tick.png">';
                        case Admin.Tests.TestStatus.Warning:
                            return '<img src="img/warning.png">';
                        case Admin.Tests.TestStatus.Error:
                            return '<img src="img/exclamation.png">';
                        case Admin.Tests.TestStatus.Running:
                            return '<img src="js/external/extjs/resources/images/default/grid/loading.gif">';
                        case Admin.Tests.TestStatus.Initialising:
                            return '<img src="js/external/extjs/resources/images/default/grid/nowait.gif">';
                        default:
                            return '<img src="img/cross.png">';
                        }
                    }
                }
            ],
            listeners : {
                afterrender : this._initialise,
                mouseover : this._onMouseover
            }
        });

        Admin.TestResultsPanel.superclass.constructor.call(this, cfg);
    },

    /**
     * Called whenever a test changes status
     */
    _onTestStatusChange : function(test, status) {
        var testRecord = this.getStore().getById(test.getId());
        if (testRecord) {
            testRecord.set('description', test.getDescription());
            testRecord.set('status', status);
            this.doLayout();
        }
    },

    /**
     * Called when the user mouseovers a test
     */
    _onMouseover :  function(e, t) {
        e.stopEvent();

        var row = e.getTarget('.x-grid3-row');
        var col = e.getTarget('.x-grid3-col');

        //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
        if (col !== null && (!this.currentToolTip || !this.currentToolTip.isVisible())) {
            //get the actual data record
            var theRow = this.getView().findRow(row);
            var testObj = this.getStore().getAt(theRow.rowIndex);
            var test = this._testMap[testObj.get('id')];
            var autoWidth = !Ext.isIE6 && !Ext.isIE7;

            //This is for the 'record type' column
            if (col.cellIndex == '2') {
                this.currentToolTip = new Ext.ToolTip({
                    target: e.target,
                    autoHide : true,
                    html: test.getStatusTip(),
                    anchor: 'bottom',
                    trackMouse: true,
                    showDelay:60,
                    autoHeight:true,
                    autoWidth: autoWidth,
                    listeners : {
                        hide : function(component) {
                            component.destroy();
                        }
                    }
                });
            }
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
        addTestFn(this, new Admin.Tests.ExternalConnectivity(cfg));
        addTestFn(this, new Admin.Tests.RegistryConnectivity(cfg));
        addTestFn(this, new Admin.Tests.Vocabulary(cfg));
        addTestFn(this, new Admin.Tests.KnownLayerWFS(cfg));
        addTestFn(this, new Admin.Tests.KnownLayerWMS(cfg));
        addTestFn(this, new Admin.Tests.RegisteredLayerWMS(cfg));
        addTestFn(this, new Admin.Tests.RegisteredLayerWFS(cfg));
    },

    /**
     * Loads the internal datastore with every test
     */
    _loadTestStore : function() {
        //Load them into the datastore
        var dataObj = {
            data : []
        };
        for (var testId in this._testMap) {
            var test = this._testMap[testId];

            //Create our fake JSON objects to push into the datastore
            dataObj.data.push({
                id : testId,
                title : test.getTitle(),
                description : test.getDescription(),
                status : Admin.Tests.TestStatus.Initialising //our default status
            });
        }

        //Load our data store
        var ds = this.getStore();
        ds.loadData(dataObj);
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

Ext.reg('testresultspanel', Admin.TestResultsPanel);