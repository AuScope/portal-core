/**
 * Utility class for handling a series of portal.layer.querier.QueryTarget objects
 * by opening selection menus (if appropriate) or by simply passing along the
 * QueryTarget objects to the appropriate querier instances.
 */
Ext.define('portal.layer.querier.QueryTargetHandler', {

    _infoWindowHeight : 350,
    _infoWindowWidth : 600,

    /**
     * Accepts a config with {
     *  infoWindowHeight : [Optional] Number height of info window in pixels
     *  infoWindowWidth : [Optional] Number width of info window in pixels
     * }
     * 
     * VT: this might be legacy code however it feels a bit pointless to have this overriding window size option at the constructor level
     * as it does not allow fine grain tunning of windows size. A more suitable place would be in the baseComponents that makes up the
     * infowindow. Hence in _queryCallback, I have added checks on baseComponent for object overrideInfoWindowSize and if found, use that window size 
     * instead.
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
        //VT: if any overrideInfoWindowSize is found under the baseComponents, use that instead.
        for(var i = 0; i < baseComponents.length; i++){
            if(baseComponents[i].overrideInfoWindowSize){
                width = baseComponents[i].overrideInfoWindowSize.width;
                height = baseComponents[i].overrideInfoWindowSize.height;
                break;
            }
        }

        //Show our info window - create our parent components
        var windowLocation = Ext.create('portal.map.Point', {
            latitude : queryTarget.get('lat'),
            longitude : queryTarget.get('lng')
        });
        //function(windowLocation, width, height, content, initFunction)
        mapWrapper.openInfoWindow(windowLocation, width, height, baseComponents, queryTarget.get('layer'));
    },

    /**
     * Just query everything in queryTargets
     */
    _handleWithQuery : function(queryTargets, mapWrapper) {
        var loadMask = new Ext.LoadMask({
            msg : 'Loading...',
            target : mapWrapper.container
        });
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
            
            var provider = portal.util.ProviderNameTransformer.abbreviateName(cswRecord.get('contactOrg'));

            var maxTitleLength = 120;
            
            // append the name of the organisation that supplied the record
            shortTitle += ' - ' + provider;
            
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
            header: {
                xtype: 'header',
                titlePosition: 0,
                title: 'Please select a query source',
                cls: 'x-panel-header-light'
            },
            autoWidth : true,
            closable : true,
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
                if (this._queryScaleCheck(mapWrapper, queryTargets[i])) {
                    explicitTargets.push(queryTargets[i]);
                }
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
    },

    /**
     * Checks whether the map scale is within the query target scale bounds
     *
     * @param mapWrapper
     * @param queryTarget
     * @returns {boolean}
     */
    _queryScaleCheck : function(mapWrapper, queryTarget) {

        var cswRecord = queryTarget.get('cswRecord');
        if (!cswRecord) {
            return false
        }

        var minScale = cswRecord.get('minScale');
        var maxScale = cswRecord.get('maxScale');

        var mapScale = mapWrapper.map.getScale();

        if (minScale && minScale > mapScale) {
            return false;
        }
        if (maxScale && maxScale < mapScale) {
            return false;
        }

        return true;
    }
});