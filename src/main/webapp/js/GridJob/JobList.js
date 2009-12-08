/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

// reference local blank image
Ext.BLANK_IMAGE_URL = 'js/ext/resources/images/default/s.gif';

Ext.namespace('JobList');

JobList.ControllerURL = "joblist.html";

////////////////////////
////// Callbacks ///////
////////////////////////

// called when a JsonStore fails retrieving data from the server
JobList.onLoadException = function(proxy, options, response, e) {
    JobList.hideProgressDlg();
    if (response.status != 0) {
        JobList.showError("Could not interpret server response "+
            "(most likely your session has expired). "+
            "Please try reloading the page.");
    } else {
        JobList.showError("Could not retrieve data from the server ("+
            response.statusText+").");
    }
}

// called when an Ajax request fails
JobList.onRequestFailure = function(response, request) {
    JobList.showError('Could not execute last request. Status: '+
        response.status+' ('+response.statusText+')');
}

// callback for killJob action
JobList.onKillJobResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    }
    JobList.jobStore.reload();
}

//callback for deleteJob action
JobList.onDeleteJobResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    }
    JobList.jobStore.reload();
}

//callback for deleteSeries action
JobList.onDeleteSeriesResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    }
    JobList.seriesStore.reload();
}

// callback for retrieveFiles action
JobList.onRetrieveFilesResponse = function(response, request) {
    var resp = Ext.decode(response.responseText);
    if (resp.error != null) {
        JobList.showError(resp.error);
    } else {
        Ext.Msg.alert("Success", "The output files of the selected job will be transferred as soon as possible. Please note that it may take a while though.");
    }
}

////////////////////////
////// Functions ///////
////////////////////////

// shows an error dialog with given message
JobList.showError = function(message) {
    JobList.errorDlg = Ext.Msg.show({
        title: 'Error',
        msg: message,
        buttons: Ext.Msg.OK,
        icon: Ext.Msg.ERROR,
        fn: function() { JobList.errorDlg = undefined; }
    });
}

// hides the data retrieval progress dialog
JobList.hideProgressDlg = function() {
    if (JobList.progressDlg) {
        JobList.progressDlg.hide();
        JobList.progressDlg = undefined;
    }
}

// notifies the user through a progress dialog that data is being retrieved
JobList.showProgressDlg = function() {
    if (!JobList.progressDlg && !JobList.errorDlg) {
        JobList.progressDlg = Ext.Msg.wait('Retrieving data, please wait...');
    }
}

// retrieves filelist of selected job and updates the Details panel
JobList.updateJobDetails = function() {
    var jobGrid = Ext.getCmp('job-grid');
    var descEl = Ext.getCmp('description-panel').body;
    var detailsPanel = Ext.getCmp('details-panel');

    if (JobList.prevJobId) {
        var idx = JobList.jobStore.find("id", JobList.prevJobId);
        JobList.prevJobId = undefined;
        if (idx >= 0) {
            jobGrid.getSelectionModel().selectRow(idx);
            return;
        }
    }

    if (jobGrid.getSelectionModel().getSelected()) {
        var jobData = jobGrid.getSelectionModel().getSelected().data;
        JobList.jobFileStore.baseParams.jobId = jobData.id;
        JobList.jobFileStore.reload();
        jobData.seriesName = Ext.getCmp('series-grid').
            getSelectionModel().getSelected().data.name;
        JobList.jobDescTpl.overwrite(descEl, jobData);
        detailsPanel.enable();
    } else {
        JobList.jobFileStore.removeAll();
        detailsPanel.setActiveTab('description-tab');
        detailsPanel.disable();
    }
}

// retrieves list of jobs of selected series
JobList.updateJobList = function() {
    var jobGrid = Ext.getCmp('job-grid');
    var seriesGrid = Ext.getCmp('series-grid');
    var descEl = Ext.getCmp('description-panel').body;

    JobList.hideProgressDlg();

    if (JobList.prevSeriesId) {
        var idx = JobList.seriesStore.find("id", JobList.prevSeriesId);
        JobList.prevSeriesId = undefined;
        if (idx >=0) {
            seriesGrid.getSelectionModel().selectRow(idx);
            return;
        }
    }

    if (seriesGrid.getSelectionModel().getSelected()) {
        jobGrid.enable();
        var seriesData = seriesGrid.getSelectionModel().getSelected().data;
        JobList.jobStore.baseParams.seriesId = seriesData.id;
        JobList.jobStore.reload();
        JobList.seriesDescTpl.overwrite(descEl, seriesData);
    } else {
        JobList.jobStore.removeAll();
        jobGrid.disable();
        if (seriesGrid.getStore().getTotalCount() > 0) {
            descEl.update('<p class="jobdesc-title">Please select a series to see its details and the associated jobs.</p>');
        } else {
            descEl.update('<p class="jobdesc-title">No series found.</p>');
        }
    }
}

// (re-)retrieves job details from the server
JobList.refreshAll = function() {
    var jobGrid = Ext.getCmp('job-grid');
    var seriesGrid = Ext.getCmp('series-grid');
    if (seriesGrid.getSelectionModel().getSelected()) {
        JobList.prevSeriesId = seriesGrid.getSelectionModel().getSelected().data.id;
    }
    if (jobGrid.getSelectionModel().getSelected()) {
        JobList.prevJobId = jobGrid.getSelectionModel().getSelected().data.id;
    }
    JobList.seriesStore.reload();
}

// loads series matching given query
JobList.querySeries = function(user, name, desc) {
    if (Ext.isEmpty(user)) {
        JobList.seriesStore.baseParams.qUser = null;
    } else {
        JobList.seriesStore.baseParams.qUser = user;
    }
    if (Ext.isEmpty(name)) {
        JobList.seriesStore.baseParams.qSeriesName = null;
    } else {
        JobList.seriesStore.baseParams.qSeriesName = name;
    }
    if (Ext.isEmpty(desc)) {
        JobList.seriesStore.baseParams.qSeriesDesc = null;
    } else {
        JobList.seriesStore.baseParams.qSeriesDesc = desc;
    }
    JobList.seriesStore.reload();
}

JobList.retrieveFiles = function(jobId) {
    Ext.Ajax.request({
        url: '/retrieveJobFiles.do',
        success: JobList.onRetrieveFilesResponse,
        failure: JobList.onRequestFailure, 
        params: {  
    	          'jobId': jobId }
    });
}

// submits a "kill job" request after asking for confirmation
JobList.killJob = function(jobId) {
    Ext.Msg.show({
        title: 'Cancel Job',
        msg: 'Are you sure you want to cancel the selected job?',
        buttons: Ext.Msg.YESNO,
        icon: Ext.Msg.WARNING,
        animEl: 'job-grid',
        closable: false,
        fn: function(btn) {
            if (btn == 'yes') {
                Ext.Ajax.request({
                    url: '/killJob.do',
                    success: JobList.onKillJobResponse,
                    failure: JobList.onRequestFailure, 
                    params: { 'jobId': jobId }
                });
            }
        }
    });
}

//submits a "delete job" request after asking for confirmation
JobList.deleteJob = function(jobId) {
    Ext.Msg.show({
        title: 'Delete Job',
        msg: 'Are you sure you want to delete the selected job?',
        buttons: Ext.Msg.YESNO,
        icon: Ext.Msg.WARNING,
        animEl: 'job-grid',
        closable: false,
        fn: function(btn) {
            if (btn == 'yes') {
                Ext.Ajax.request({
                    url: '/deleteJob.do',
                    success: JobList.onDeleteJobResponse,
                    failure: JobList.onRequestFailure, 
                    params: { 
                	          'jobId': jobId }
                });
            }
        }
    });
}

JobList.killSeriesJobs = function(seriesId) {
    Ext.Msg.show({
        title: 'Cancel Series Jobs',
        msg: 'Are you sure you want to cancel ALL jobs in the selected series?',
        buttons: Ext.Msg.YESNO,
        icon: Ext.Msg.WARNING,
        animEl: 'series-grid',
        closable: false,
        fn: function(btn) {
            if (btn == 'yes') {
                Ext.Ajax.request({
                    url: '/killSeriesJobs.do',
                    success: JobList.onKillJobResponse,
                    failure: JobList.onRequestFailure, 
                    params: { 
                	          'seriesId': seriesId }
                });
            }
        }
    });
}


JobList.deleteSeriesJobs = function(seriesId) {
    Ext.Msg.show({
        title: 'Delete Series Jobs',
        msg: 'Are you sure you want to delete ALL jobs in the selected series?',
        buttons: Ext.Msg.YESNO,
        icon: Ext.Msg.WARNING,
        animEl: 'series-grid',
        closable: false,
        fn: function(btn) {
            if (btn == 'yes') {
                Ext.Ajax.request({
                    url: '/deleteSeriesJobs.do',
                    success: JobList.onDeleteSeriesResponse,
                    failure: JobList.onRequestFailure, 
                    params: { 
                	          'seriesId': seriesId }
                });
            }
        }
    });
}

// downloads given file from a specified job
JobList.downloadFile = function(job, file) {
    window.location =  "/downloadFile.do?jobId="+job+"&filename="+file;
}

// downloads a ZIP file containing given files of given job
JobList.downloadAsZip = function(job, files) {
    var fparam = files[0].data.name;
    for (var i=1; i<files.length; i++) {
        fparam += ','+files[i].data.name;
    }
    window.location = "/downloadAsZip.do?jobId="+job+"&files="+fparam;
}

JobList.resubmitJob = function(job) {
	window.location = "/resubmitJob.do?jobId="+job;
}

JobList.useScript = function(job, file) {
	window.location = "/useScript.do?jobId="+job;
}

JobList.showQueryDialog = function() {
    var queryForm = new Ext.FormPanel({
        bodyStyle: 'padding:5px;',
        defaults: { anchor: "100%" },
        items: [{
            xtype: 'textfield',
            id: 'qUser',
            fieldLabel: 'User Name'
        }, {
            xtype: 'textfield',
            id: 'qSeriesName',
            fieldLabel: 'Series Name'
        }, {
            xtype: 'textfield',
            id: 'qSeriesDesc',
            fieldLabel: 'Description'
        }]
    });

    var queryWindow = new Ext.Window({
        title: 'Query job series',
        plain: true,
        width: 500,
        resizable: false,
        autoScroll: true,
        constrainHeader: true,
        bodyStyle: 'padding:5px;',
        items: queryForm,
        modal: true,
        buttons: [{
            text: 'Query',
            handler: function() {
                var qUser = Ext.getCmp('qUser').getRawValue();
                var qName = Ext.getCmp('qSeriesName').getRawValue();
                var qDesc = Ext.getCmp('qSeriesDesc').getRawValue();
                JobList.querySeries(qUser, qName, qDesc);
                queryWindow.close();
            }
        }, {
            text: 'Cancel',
            handler: function() { queryWindow.close(); }
        }]
    });

    queryWindow.show();
}

//
// This is the main layout definition.
//
JobList.initialize = function() {
    
    Ext.QuickTips.init();

    JobList.seriesStore = new Ext.data.JsonStore({
        url: '/querySeries.do',
        root: 'series',
        autoLoad: true,
        fields: [
            { name: 'id', type: 'int' },
            { name: 'name', type: 'string' },
            { name: 'description', type: 'string' },
            { name: 'user', type: 'string'}
        ],
        listeners: {
            'beforeload': JobList.showProgressDlg,
            'load': JobList.updateJobList,
            'loadexception': JobList.onLoadException
        }
    });

    JobList.jobStore = new Ext.data.JsonStore({
        url: '/listJobs.do',
        root: 'jobs',
        fields: [
            { name: 'id', type: 'int' },
            { name: 'name', type: 'string' },
            { name: 'description', type: 'string' },
            { name: 'site', type: 'string' },
            { name: 'version', type: 'string' },
            { name: 'checkpointPrefix', type: 'string' },
            { name: 'numTimesteps', type: 'int' },
            { name: 'numParticles', type: 'int' },
            { name: 'numBonds', type: 'int' },
            { name: 'outputDir', type: 'string' },
            { name: 'reference', type: 'string' },
            { name: 'scriptFile', type: 'string' },
            { name: 'status', type: 'string'},
            { name: 'submitDate', type: 'date', dateFormat: 'Ymd_His' }
        ],
        listeners: {
            'beforeload': JobList.showProgressDlg,
            'load': JobList.hideProgressDlg,
            'loadexception': JobList.onLoadException
        }
    });

    JobList.jobFileStore = new Ext.data.JsonStore({
        url: '/jobFiles.do',
        root: 'files',
        sortInfo: { field: 'name', direction: 'ASC' },
        fields: [
            { name: 'name', type: 'string' },
            { name: 'size', type: 'int' }
        ],
        listeners: {
            'beforeload': JobList.showProgressDlg,
            'load': JobList.hideProgressDlg,
            'loadexception': JobList.onLoadException
        }
    });

    //
    // Series Grid & functions
    //
    var cancelSeriesAction = new Ext.Action({
        text: 'Cancel jobs',
        iconCls: 'cross-icon',
        handler: function() {
            var seriesId = seriesGrid.getSelectionModel().getSelected().data.id;
            JobList.killSeriesJobs(seriesId);
        }
    });
    
    var deleteSeriesAction = new Ext.Action({
        text: 'Delete jobs',
        iconCls: 'cross-icon',
        handler: function() {
            var seriesId = seriesGrid.getSelectionModel().getSelected().data.id;
            JobList.deleteSeriesJobs(seriesId);
        }
    });

    var seriesGrid = new Ext.grid.GridPanel({
        id: 'series-grid',
        title: 'Series List',
        region: 'center',
        height: 250,
        store: JobList.seriesStore,
        columns: [
            { header: 'User', width: 150, sortable: true, dataIndex: 'user'},
            { header: 'Series Name', sortable: true, dataIndex: 'name'}
        ],
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: { 'selectionchange': function(sm) {
                            JobList.updateJobList();
                         }
                       }
        }),
        stripeRows: true
    });

    seriesGrid.on({
        'rowcontextmenu': function(grid, rowIndex, e) {
            grid.getSelectionModel().selectRow(rowIndex);
            if (!this.contextMenu) {
                this.contextMenu = new Ext.menu.Menu({
                    items: [ cancelSeriesAction, deleteSeriesAction ]
                });
            }
            e.stopEvent();
            this.contextMenu.showAt(e.getXY());
        }
    });

    //
    // Job Grid & functions
    //
    function jobStatusRenderer(value, cell, record) {
        if (value == "Failed") {
            return '<span style="color:red;">' + value + '</span>';
        } else if (value == "Active") {
            return '<span style="color:green;">' + value + '</span>';
        } else if (value == "Done") {
            return '<span style="color:blue;">' + value + '</span>';
        }
        return value;
    }

    var cancelJobAction = new Ext.Action({
        text: 'Cancel Job',
        iconCls: 'cross-icon',
        handler: function() {
            var jobId = jobGrid.getSelectionModel().getSelected().data.id;
            JobList.killJob(jobId);
        }
    });

    var deleteJobAction = new Ext.Action({
        text: 'Delete Job',
        iconCls: 'cross-icon',
        handler: function() {
            var jobId = jobGrid.getSelectionModel().getSelected().data.id;
            JobList.deleteJob(jobId);
        }
    });
    
    var resubmitJobAction = new Ext.Action({
        text: 'Re-submit Job',
        handler: function() {
            var jobId = jobGrid.getSelectionModel().getSelected().data.id;
            JobList.resubmitJob(jobId);
        }
    });

    var jobGrid = new Ext.grid.GridPanel({
        id: 'job-grid',
        title: 'Jobs of selected series',
        region: 'south',
        split: true,
        height: 200,
        store: JobList.jobStore,
        columns: [
            { header: 'Job Name', sortable: true, dataIndex: 'name'},
            { header: 'Submit Date', width: 160, sortable: true, dataIndex: 'submitDate', renderer: Ext.util.Format.dateRenderer('d M Y, H:i:s')},
            { header: 'Status', sortable: true, dataIndex: 'status', renderer: jobStatusRenderer}
        ],
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: { 'selectionchange': function(sm) {
                            JobList.updateJobDetails();
                         }
                       }
        }),
        stripeRows: true
    });

    jobGrid.on({
        'rowcontextmenu': function(grid, rowIndex, e) {
            grid.getSelectionModel().selectRow(rowIndex);
            if (!this.contextMenu) {
                this.contextMenu = new Ext.menu.Menu({
                    items: [ cancelJobAction, deleteJobAction ]
                });
            }
            e.stopEvent();
            var jobData = grid.getStore().getAt(rowIndex).data;
            if (jobData.status == 'Active' || jobData.status == 'StageIn') {
                cancelJobAction.setDisabled(false);
                deleteJobAction.setDisabled(true);
            } else {
                cancelJobAction.setDisabled(true);
                deleteJobAction.setDisabled(false);
            }
            this.contextMenu.showAt(e.getXY());
        }
    });

    //
    // File Grid & functions
    //
    function fileTypeRenderer(value, cell, record) {
        var jobData = jobGrid.getSelectionModel().getSelected().data;
        if (value == jobData.scriptFile) {
            return '<span style="font-weight:bold;">*' + value + '</span>';
        } else if (value.lastIndexOf(".py") == value.length-3) {
            return '<span style="color:green;">' + value + '</span>';
        } else if (jobData.checkpointPrefix.length > 0 &&
                value.indexOf(jobData.checkpointPrefix) == 0) {
            return '<span style="color:blue;">' + value + '</span>';
        }
        return value;
    }

    var downloadAction = new Ext.Action({
        text: 'Download',
        disabled: true,
        iconCls: 'disk-icon',
        handler: function() {
            var jobData = jobGrid.getSelectionModel().getSelected().data;
            var fileName = fileGrid.getSelectionModel().getSelected().data.name;
            JobList.downloadFile(jobData.id, fileName);
        }
    });
    var downloadZipAction = new Ext.Action({
        text: 'Download as Zip',
        disabled: true,
        iconCls: 'disk-icon',
        handler: function() {
            var jobData = jobGrid.getSelectionModel().getSelected().data;
            var files = fileGrid.getSelectionModel().getSelections();
            JobList.downloadAsZip(jobData.id, files);
        }
    });
    var useScriptAction = new Ext.Action({
        text: 'Edit and use script',
        iconCls: 'grid-icon',
        handler: function() {
            var jobData = jobGrid.getSelectionModel().getSelected().data;
            JobList.useScript(jobData.id);
        }
    });
    var retrieveFilesAction = new Ext.Action({
        text: 'Retrieve latest files',
        iconCls: 'grid-icon',
        handler: function() {
            var jobData = jobGrid.getSelectionModel().getSelected().data;
            JobList.retrieveFiles(jobData.id);
        }
    });
    
    fileGrid = new Ext.grid.GridPanel({
        id: 'file-grid',
        title: 'Files',
        store: JobList.jobFileStore,
        columns: [
            { header: 'Filename', width: 200, sortable: true, dataIndex: 'name', renderer: fileTypeRenderer},
            { header: 'Size', width: 100, sortable: true, dataIndex: 'size', renderer: Ext.util.Format.fileSize, align: 'right'}
        ],
        stripeRows: true,
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: false,
            listeners: {
                'selectionchange': function(sm) {
                    if (fileGrid.getSelectionModel().getCount() == 0) {
                        downloadAction.setDisabled(true);
                        downloadZipAction.setDisabled(true);
                    } else {
                        if (fileGrid.getSelectionModel().getCount() != 1) {
                            downloadAction.setDisabled(true);
                        } else {
                            downloadAction.setDisabled(false);
                        }
                        downloadZipAction.setDisabled(false);
                    }
                }
            }
        }),
        tbar: [{
            text: 'Actions',
            iconCls: 'folder-icon',
            menu: [ downloadAction, downloadZipAction, retrieveFilesAction]
        }]
    });

    fileGrid.on({
        'rowdblclick': function(grid, rowIndex, e) {
            var jobData = jobGrid.getSelectionModel().getSelected().data;
            var fileName = grid.getStore().getAt(rowIndex).data.name;
            JobList.downloadFile(jobData.id, fileName);
        },
        'rowcontextmenu': function(grid, rowIndex, e) {
            grid.getSelectionModel().selectRow(rowIndex);
            if (!this.contextMenu) {
                this.contextMenu = new Ext.menu.Menu({
                    items: [ downloadAction, downloadZipAction ]
                });
            }
            e.stopEvent();
            this.contextMenu.showAt(e.getXY());
        }
    });

    // a template for the job description html area
    JobList.jobDescTpl = new Ext.Template(
        '<p class="jobdesc-title">{name}</p>',
        '<table width="100%"><col width="150px"></col><col class="jobdesc-content"></col>',
        '<tr><td class="jobdesc-key">Part of series:</td><td>{seriesName}</td></tr>',
        '<tr><td class="jobdesc-key">Submitted on:</td><td>{submitDate}</td></tr>',
        '<tr><td class="jobdesc-key">Computation site:</td><td>{site}</td></tr>',
        '<tr><td class="jobdesc-key">Version:</td><td>{version}</td></tr></table><br/>',
        '<p class="jobdesc-key">Description:</p><br/><p>{description}</p>'
    );
    JobList.jobDescTpl.compile();

    // a template for the series description html area
    JobList.seriesDescTpl = new Ext.Template(
        '<p class="jobdesc-title">{name}</p><br/>',
        '<p class="jobdesc-key">Description:</p><br/><p>{description}</p>'
    );
    JobList.seriesDescTpl.compile();

    jobDetails = new Ext.TabPanel({
        id: 'details-panel',
        disabled: true,
        activeTab: 0,
        split: true,
        items: [
            {
                id: 'description-tab',
                title: 'Description',
                bodyStyle: 'padding:10px',
                defaults: { border: false },
                layout: 'fit',
                items: [ { id: 'description-panel', html: '' } ]
            },
            fileGrid
        ]
    });

    new Ext.Viewport({
        layout: 'border',
        items: [{
            xtype: 'box',
            region: 'north',
            applyTo: 'body',
            height: 100
        },{
            id: 'job-panel',
            border: false,
            region: 'west',
            split: true,
            margins: '2 2 2 0',
            layout: 'border',
            width: '400',
            buttons: [{
                text: 'My Jobs',
                tooltip: 'Retrieves the list of your series and jobs',
                handler: JobList.querySeries
            }, {
                text: 'Query...',
                tooltip: 'Displays the query dialog to search for jobs',
                handler: JobList.showQueryDialog,
                cls: 'x-btn-text-icon',
                iconCls: 'find-icon'
            }, {
                text: 'Refresh',
                tooltip: 'Reloads the status and file list of currently displayed jobs',
                handler: JobList.refreshAll,
                cls: 'x-btn-text-icon',
                iconCls: 'refresh-icon'
            }],
            items: [ seriesGrid, jobGrid ]
        },{
            id: 'main-panel',
            title: 'Details',
            region: 'center',
            margins: '2 2 2 0',
            layout: 'fit',
            items: [ jobDetails ]
        }]
    });

    if (JobList.error != null) {
        JobList.showError(JobList.error);
        JobList.error = null;
    }
}


Ext.onReady(JobList.initialize);

