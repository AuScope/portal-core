Ext.define('Ticket.view.dashboard.Dashboard', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.app-dashboard',
    
    controller: 'dashboard',
    viewModel: {
        type: 'dashboard'
    },
    
    requires: [
        'Ticket.view.dashboard.DashboardController',
        'Ticket.view.dashboard.DashboardModel',
        'Ext.grid.column.Widget',
        'Ticket.override.grid.column.Date',
        'Ext.form.field.Display',
        'Ext.chart.*',
        'Ext.layout.container.HBox',
        'Ext.layout.container.VBox',
        'Ext.layout.container.Fit',
        'Ext.layout.container.Border'
    ],
    
    bodyPadding: 20,
    bodyCls: 'app-dashboard',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [{
        xtype: 'container',
        layout: 'hbox',
        items: [{
            xtype: 'component',
            margin: '10 0 20 0',
            cls: 'title',
            html: 'Project Summary',
            bind: 'Project Summary - {theProject.name}'
        }]
    }, {
        xtype: 'container',
        layout: {
            type: 'hbox',
            align: 'stretch'
        },
        flex: 1,
        items: [{
            xtype: 'container',
            flex: 1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'panel',
                flex: 1,
                title: 'Ticket Status Summary',
                layout: 'fit',
                items: {
                    xtype: 'polar',
                    bind: '{ticketStatusSummary}',
                    interactions: 'rotatePie3d',
                    animate: {
                        duration: 500,
                        easing: 'easeIn'
                    },
                    series: [
                        {
                            type: 'pie3d',
                            field: 'total',
                            donut: 30,
                            distortion: 0.6,
                            style: {
                                stroke: "white",
                                opacity: 0.90
                            }
                        }
                    ]
                }
            }, {
                xtype: 'panel',
                flex: 1,
                margin: '20 0 0 0',
                title: '1 Month Ticket Open Summary',
                layout: 'fit',
                items: {
                    xtype: 'cartesian',
                    bind: '{ticketOpenSummary}',
                    axes: [{
                        type: 'numeric',
                        position: 'left',
                        fields: ['total']
                    }, {
                        type: 'time',
                        dateFormat: 'm/d',
                        position: 'bottom',
                        fields: ['date']
                    }],
                    series: [{
                        type: 'line',
                        axis: 'left',
                        xField: 'date',
                        yField: 'total'
                    }]
                }
            }]
        }, {
            xtype: 'container',
            flex: 1,
            margin: '0 0 0 40',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'grid',
                reference: 'activeTickets',
                flex: 1,
                tbar: [{
                    text: 'Refresh',
                    handler: 'onActiveTicketRefreshClick'
                }],
                listeners: {
                    itemdblclick: 'onTicketDblClick'
                },
                title: 'My Active Tickets',
                bind: '{myActiveTickets}',
                viewConfig: {
                    emptyText: 'You have no active tickets for this project'
                },
                columns: [{
                    text: 'Id',
                    dataIndex: 'id',
                    width: 100
                }, {
                    text: 'Title',
                    dataIndex: 'title',
                    flex: 1
                }, {
                    xtype: 'datecolumn',
                    width: 120,
                    text: 'Created',
                    dataIndex: 'created'
                }, {
                    xtype: 'datecolumn',
                    width: 120,
                    text: 'Last Modified',
                    dataIndex: 'modified'
                }, {
                    xtype: 'actioncolumn',
                    width: 20,
                    handler: 'onTicketClick',
                    items: [{
                        tooltip: 'View ticket',
                        iconCls: 'ticket'
                    }]
                }]
            }, {
                xtype: 'grid',
                flex: 1,
                title: 'Project Members',
                margin: '20 0 0 0',
                bind: {
                    store: '{sortedUsers}',
                    title: 'Project Members - Lead: {theProject.lead.name}'
                },
                columns: [{
                    text: 'Name',
                    dataIndex: 'name',
                    flex: 1
                }, {
                    xtype: 'widgetcolumn',
                    width: 100,
                    widget: {
                        xtype: 'button',
                        text: 'Edit',
                        listeners: {
                            click: 'onGridEditClick'
                        }
                    }
                }]
            }]
        }]
    }]
});
