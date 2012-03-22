/**
 * Utility class for handling a series of portal.layer.querier.QueryTarget objects
 * by opening selection menus (if appropriate) or by simply passing along the
 * QueryTarget objects to the appropriate querier instances.
 */
Ext.define('portal.layer.querier.QueryTargetHandler', {

    _infoWindowHeight : 300,
    _infoWindowWidth : 600,

    /**
     * Accepts a config with {
     *  infoWindowHeight : [Optional] Number height of info window in pixels
     *  infoWindowWidth : [Optional] Number width of info window in pixels
     * }
     */
    constructor : function(config) {
        if (config.infoWindowHeight) {
            this._infoWindowHeight = config.infoWindowHeight;
        }
        if (config.infoWindowWidth) {
            this._infoWindowWidth = config.infoWindowWidth;
        }
        this.callParent(arguments);
    },

    /**
     * Re-entrant show function for a particular loadMask
     */
    _showLoadMask : function(loadMask) {
        if (!loadMask) {
            return;
        }
        if (!loadMask._showCount) {
            loadMask._showCount = 0;
        }

        if (loadMask._showCount++ === 0) {
            loadMask.show();
        }
    },

    /**
     * Re-entrant hide function for a particular loadMask
     */
    _hideLoadMask : function(loadMask) {
        if (!loadMask) {
            return;
        }
        if (--loadMask._showCount === 0) {
            loadMask.hide();
        }
    },

    /**
     * Handles a query response by opening up info windows
     */
    _queryCallback : function(querier, baseComponents, queryTarget, mapWrapper, loadMask) {
        this._hideLoadMask(loadMask); //ensure this gets called or we'll have a forever floating 'Loading...'
        if (!baseComponents || baseComponents.length === 0) {
            return; //if the query failed, don't show a popup
        }

        //We need to open an info window with a number of tabs for each of the base components
        //Each tab will need to have an appropriately sized parent container rendered into it
        //AND once they are all rendered, we need to then add each element of baseComponents
        //to each of the tabs

        //Build our info window content (sans parent containers)
        var width = this._infoWindowWidth;
        var height = this._infoWindowHeight;
        var infoWindowIds = []; //this holds the unique ID's to bind to
        var infoWindowTabs = []; //this holds GInfoWindowTab instances
        for (var i = 0; i < baseComponents.length; i++) {
            infoWindowIds.push(Ext.id());
            var html = Ext.util.Format.format('<html><body><div id="{0}" style="width: {1}px; height: {2}px;"></div></body></html>', infoWindowIds[i], width, height);
            infoWindowTabs.push(new GInfoWindowTab(baseComponents[i].tabTitle, html));
        }
        var initFunctionParams = { //this will be passed to the info window manager callback
            width : width,
            height : height,
            infoWindowIds : infoWindowIds,
            baseComponents : baseComponents
        };
        var infoWindowParams = undefined; //we don't dictate any extra info window options

        //Show our info window - create our parent components
        var windowLocation = new GLatLng(queryTarget.get('lat'), queryTarget.get('lng'));
        mapWrapper.openInfoWindow(windowLocation, infoWindowTabs, infoWindowParams, initFunctionParams, function(map, location, params) {
            for (var i = 0; i < params.baseComponents.length; i++) {
                Ext.create('Ext.container.Container', {
                    renderTo : params.infoWindowIds[i],
                    border : 0,
                    width : params.width,
                    height : params.height,
                    layout : 'fit',
                    items : [params.baseComponents[i]],
                    listeners : {
                        //To workaround some display issues with ext JS under Google maps
                        //We need to force a layout of the ExtJS container when the GMap tab
                        //changes. GMap doesn't offer anyway of doing that so we instead monitor
                        //the underlying DOM for style changes referencing the 'display' CSS attribute.
                        //See: http://www.sencha.com/forum/showthread.php?186027-Ext-4.1-beta-3-Strange-layout-on-grids-rendered-into-elements-with-display-none&p=752916#post752916
                        afterrender : function(container) {
                            //Find the parent info window DOM
                            var el = container.getEl();
                            var tabParentDiv = el.findParentNode('div.gmnoprint', 10, true);
                            var headerParentDiv = tabParentDiv.findParentNode('div.gmnoprint', 10, true);

                            //Firstly get all child div's (these are our tabs).
                            //This tells us how many headers there should be (one for each tab)
                            var tabElements = tabParentDiv.select('> div');
                            var tabElementsArr = [];
                            tabElements.each(function(div) {
                                tabElementsArr.push(div.dom);   //don't store a reference to div, it's the Ext.flyWeight el. Use div.dom
                            });

                            //Now there are a lot of divs under the header parent, we are interested
                            //in the last N (which represent the N headers of the above tabs)
                            var allParentDivs = headerParentDiv.select('> div');
                            var allParentDivsArr = [];
                            allParentDivs.each(function(div) {
                                allParentDivsArr.push(div.dom); //don't store a reference to div, it's the Ext.flyWeight el. Use div.dom
                            });
                            var headerDivsArr = allParentDivsArr.slice(allParentDivsArr.length - tabElementsArr.length);

                            //Start iterating from the second index - the first tab will never need a forced layout
                            for (var i = 1; i < headerDivsArr.length; i++) {
                                var headerDiv = new Ext.Element(headerDivsArr[i]);
                                var tabDiv = new Ext.Element(tabElementsArr[i]);

                                headerDiv.on('click', Ext.bind(function(e, t, eOpts, headerElement, tabElement) {
                                    //Find the container which belongs to t
                                    for (var i = 0; i < params.baseComponents.length; i++) {
                                        var container = params.baseComponents[i];
                                        var containerElId = container.getEl().id;

                                        //Only layout the child of the element firing the event (i.e. the tab
                                        //which is visible)
                                        var matchingElements = tabElement.select(Ext.util.Format.format(':has(#{0})', containerElId));
                                        if (matchingElements.getCount() > 0) {
                                            //Only perform the layout once for performance reasons
                                            if (!container._portalTabLayout) {
                                                container._portalTabLayout = true;
                                                container.doLayout();
                                            }
                                        }
                                    }
                                }, this, [headerDiv, tabDiv], true));
                            }
                        }
                    }
                });
            }
        });
    },

    /**
     * Just query everything in queryTargets
     */
    _handleWithQuery : function(queryTargets, mapWrapper) {
        var loadMask = new Ext.LoadMask(mapWrapper.container.getEl(), {}); //For some reason LoadMask isn't designed to work with Ext.create
        for (var i = 0; i < queryTargets.length; i++) {
            var queryTarget = queryTargets[i];
            var layer = queryTarget.get('layer');
            var querier = layer.get('querier');

            this._showLoadMask(loadMask);
            querier.query(queryTarget, Ext.bind(this._queryCallback, this, [mapWrapper, loadMask], true));
        }
    },

    /**
     * Show some form of selection to the user, ask them to decide
     * which query target they meant
     */
    _handleWithSelection : function(queryTargets, mapWrapper) {
        //Build a list of menu item objects from our query targets
        var items = [];
        var lat = 0;
        var lng = 0;
        for (var i = 0; i < queryTargets.length; i++) {
            var cswRecord = queryTargets[i].get('cswRecord');
            var onlineResource = queryTargets[i].get('onlineResource');
            if (!cswRecord) {
                continue;
            }

            lat = queryTargets[i].get('lat');
            lng = queryTargets[i].get('lng');

            var shortTitle = cswRecord.get('name');
            var maxTitleLength = 90;
            if (onlineResource && onlineResource.get('name')) {
                shortTitle += ' - ' + onlineResource.get('name');
            }
            if(shortTitle.length > maxTitleLength) {
                shortTitle = shortTitle.substr(0, maxTitleLength) + "...";
            }

            //Figure out our icon class
            var type = onlineResource ? onlineResource.get('type') : '';
            var iconCls = undefined;
            switch(type) {
            case portal.csw.OnlineResource.WFS:
            case portal.csw.OnlineResource.WCS:
            case portal.csw.OnlineResource.OPeNDAP:
                iconCls = 'data';
                break;
            default:
                iconCls = 'portrayal';
                break;
            }

            items.push({
                text : shortTitle,
                queryTarget : queryTargets[i],
                iconCls : iconCls,
                listeners : {
                    click : Ext.bind(function(queryTarget, mapWrapper) {
                        this._handleWithQuery([queryTarget], mapWrapper);
                    }, this, [queryTargets[i], mapWrapper])
                }
            });
        }

        //If we couldn't make any menu items, no point in proceeding
        if (items.length === 0) {
            return;
        }

        var menu = Ext.create('Ext.menu.Menu', {
            id : 'querytargethandler-selection-menu',
            autoWidth : true,
            margin: '0 0 10 0',
            enableScrolling: true,
            items : items
        });

        mapWrapper.showContextMenuAtLatLng(lat, lng, menu);
    },

    /**
     * Given an array of portal.layer.querier.QueryTarget objects,
     * figure out how to pass these to appropriate querier instances
     * and optionally show popup information on the map.
     *
     * This function will likely open info windows on the map and
     * hide/show loading masks where appropriate.
     *
     * @param mapWrapper An instance of portal.util.gmap.GMapWrapper
     * @param queryTargets Array of portal.layer.querier.QueryTarget objects
     */
    handleQueryTargets : function(mapWrapper, queryTargets) {
        //Ensure subsequent clicks destroy the popup menu
        var menu = Ext.getCmp('querytargethandler-selection-menu');
        if (menu) {
            menu.destroy();
        }

        if (!queryTargets || queryTargets.length === 0) {
            return;
        }

        var explicitTargets = []; // all QueryTarget instances with the explicit flag set
        for (var i = 0; i < queryTargets.length; i++) {
            if (queryTargets[i].get('explicit')) {
                explicitTargets.push(queryTargets[i]);
            }
        }

        //If we have an ambiguous set of targets - let's just ask the user what they meant
        if (explicitTargets.length > 1) {
            this._handleWithSelection(explicitTargets, mapWrapper);
            return;
        }

        //If we have a single explicit target, then our decision is really easy
        if (explicitTargets.length === 1) {
            this._handleWithQuery(explicitTargets, mapWrapper);
            return;
        }

        //Otherwise query everything
        this._handleWithQuery(queryTargets, mapWrapper);
    }
});