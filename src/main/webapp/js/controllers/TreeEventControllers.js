var treeCheckChangeController = function(node, isChecked, map, statusBar, viewport, downloadUrls, filterPanel) {

    var getFeatureTypeHandler = function(featureTypeString) {
        switch (featureTypeString) {
            case 'mo:MiningActivity': return miningActivityHandler; break;
            case 'mo:Mine': return mineHandler; break;
            case 'mo:MineralOccurrence': return mineralOccurrenceHandler; break;
            default: return simpleKmlHandler; break;
        }
    };

    var miningActivityHandler = function() {
        if (node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
            node.attributes.filterPanel = new buildMiningActivityFilterForm(node.id, "/getMineNames.do", "/doMiningActivityFilter.do", node.attributes.wfsUrl, function(form, action) {
                addKmlLayer(node, action.result.data.kml, viewport);
            }, function() {
                if (node.attributes.tileOverlay instanceof GeoXml) {
                    node.attributes.tileOverlay.clear();
                    node.attributes.tileOverlay = null;
                }
            });
        }

        showNodesFilterPanel(node, filterPanel);
    };

    var mineHandler = function() {
        if (node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
            node.attributes.filterPanel = new buildMineFilterForm(node.id, "/getMineNames.do", "/doMineFilter.do", node.attributes.wfsUrl, function(form, action) {
                addKmlLayer(node, action.result.data.kml, viewport);
            }, function() {
                if (node.attributes.tileOverlay instanceof GeoXml) {
                    node.attributes.tileOverlay.clear();
                    node.attributes.tileOverlay = null;
                }
            });
        }

        showNodesFilterPanel(node, filterPanel);
    };

    var mineralOccurrenceHandler = function() {
        if (node.attributes.filterPanel == null || node.attributes.filterPanel == "") {
            node.attributes.filterPanel = new buildMineralOccurrenceFilterForm(node.id, "/getMineNames.do", "/doMineralOccurrenceFilter.do", node.attributes.wfsUrl, function(form, action) {
                addKmlLayer(node, action.result.data.kml, viewport);
            }, function() {
                if (node.attributes.tileOverlay instanceof GeoXml) {
                    node.attributes.tileOverlay.clear();
                    node.attributes.tileOverlay = null;
                }
            });
        }

        showNodesFilterPanel(node, filterPanel);
    };

    var simpleKmlHandler = function() {
        GDownloadUrl(kmlProxyUrl + node.attributes.kmlUrl, function(pData, pResponseCode) {
            if (pResponseCode == 200) {
                var exml;
                var icon = new GIcon(G_DEFAULT_ICON, node.attributes.icon);
                icon.iconSize = new GSize(32, 32);
                exml = new GeoXml("theglobalexml", map, null, {baseicon:icon, markeroptions:{markerHandler:function(marker) {
                    marker.featureType = node.attributes.featureType;
                    marker.wfsUrl = node.attributes.kmlUrl;
                }}});
                exml.parseString(pData);
                node.enable();
                statusBar.setVisible(false);
                viewport.doLayout();
                statusBar.clearStatus();

                node.attributes.tileOverlay = exml;

                downloadUrls.put(node.attributes.wfsUrl, node.attributes.wfsUrl);
            }
        });
    };

    var wmsHandler = function() {
        var tileLayer = new GTileLayer(null, null, null, {
            tileUrlTemplate: node.attributes.wmsUrl + 'layers=' + node.id + '&zoom={Z}&x={X}&y={Y}',
            isPng:true,
            opacity:1.0 }
                );
        node.attributes.tileOverlay = new GTileLayerOverlay(tileLayer);
        map.addOverlay(node.attributes.tileOverlay);
    };

    //the check was checked on
    if (isChecked) {
        if (node.attributes.layerType == 'wms' && (node.attributes.tileOverlay == null || node.attributes.tileOverlay == '')) {
            new wmsHandler();
        }
        else if (node.attributes.layerType == 'wfs') {
            statusBar.setStatus({
                text: 'Finished loading',
                iconCls: 'ok-icon',
                clear: true
            });
            statusBar.setVisible(true);
            viewport.doLayout();
            statusBar.showBusy();
            node.disable();

            getFeatureTypeHandler(node.attributes.featureType)();

            node.enable();
            statusBar.setVisible(false);
            viewport.doLayout();
            statusBar.clearStatus();
        }
    }
    //the check was checked off so remove the overlay
    else {
        if (node.attributes.layerType == 'wfs') {
            downloadUrls.remove(node.attributes.wfsUrl);
        }

        if (node.attributes.tileOverlay instanceof GeoXml)
            node.attributes.tileOverlay.clear();
        else if (node.attributes.tileOverlay != null)
            map.removeOverlay(node.attributes.tileOverlay);

        node.attributes.tileOverlay = null;

        filterPanel.getLayout().setActiveItem(0);
        //isFilterPanelNeeded(node, viewport, filterPanel);
    }
};

var treeNodeOnClickController = function(node, event, viewport, filterPanel) {
    showNodesFilterPanel(node, filterPanel);
};

var showNodesFilterPanel = function(node, filterPanel) {
    try {
        filterPanel.add(node.attributes.filterPanel);
        filterPanel.doLayout();
        filterPanel.getLayout().setActiveItem(node.id);
    } catch(err) {
        filterPanel.getLayout().setActiveItem(0);  
    }
};

var addKmlLayer = function(node, kml, viewport) {
    var exml;
    var icon = new GIcon(G_DEFAULT_ICON, node.attributes.icon);
    icon.iconSize = new GSize(32, 32);
    exml = new GeoXml("theglobalexml", map, null, {baseicon:icon, markeroptions:{markerHandler:function(marker) {
        marker.featureType = node.attributes.featureType;
        marker.wfsUrl = node.attributes.kmlUrl;
    }}});
    exml.parseString(kml);
    node.enable();
    statusBar.setVisible(false);
    viewport.doLayout();
    statusBar.clearStatus();

    node.attributes.tileOverlay = exml;
};
