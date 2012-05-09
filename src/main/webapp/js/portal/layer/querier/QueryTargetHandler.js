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

        //Build our info window content (sans parent containers)
        var width = this._infoWindowWidth;
        var height = this._infoWindowHeight;

        //Show our info window - create our parent components
        var windowLocation = Ext.create('portal.map.Point', {
            latitude : queryTarget.get('lat'),
            longitude : queryTarget.get('lng')
        });
        //function(windowLocation, width, height, content, initFunction)
        mapWrapper.openInfoWindow(windowLocation, width, height, baseComponents);
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
        var point = null;
        for (var i = 0; i < queryTargets.length; i++) {
            var cswRecord = queryTargets[i].get('cswRecord');
            var onlineResource = queryTargets[i].get('onlineResource');
            if (!cswRecord) {
                continue;
            }


            point = Ext.create('portal.map.Point', {
                latitude : queryTargets[i].get('lat'),
                longitude : queryTargets[i].get('lng')
            });

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
        if (items.length === 0 || point === null) {
            return;
        }

        var menu = Ext.create('Ext.menu.Menu', {
            id : 'querytargethandler-selection-menu',
            autoWidth : true,
            margin: '0 0 10 0',
            enableScrolling: true,
            items : items
        });

        mapWrapper.showContextMenuAtLatLng(point, menu);
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