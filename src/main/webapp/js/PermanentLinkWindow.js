/**
 * A basic window to show a permanent link to the current map state.
 * 
 * It consists of a simple warning and the link itself
 * 
 * mapStateSerializer - MapStateSerializer - The map state to be 'linked' to
 */
PermanentLinkWindow = function(mapStateSerializer) {
    
    //Rewrite our current URL with the new state info
    var urlParams = Ext.urlDecode(location.search.substring(1));
    urlParams.state = mapStateSerializer.serialize();
    var linkedUrl = location.href.split('?')[0];
    linkedUrl = Ext.urlAppend(linkedUrl, Ext.urlEncode(urlParams));
    
    PermanentLinkWindow.superclass.constructor.call(this, {
        title: 'Permanent Link',
        autoDestroy : true,
        width : 500,
        height : 300,
        autoScroll : true,
        items : [{
            xtype : 'panel',
            html : '<p><b>Warning:</b></p>' + 
                    '<p>This link will only save your selected layers and queries. The actual data received and displayed can be subject to change</p></br>' + 
                    '<p>' + linkedUrl + '</p>'
        }]
    });
};

Ext.extend(PermanentLinkWindow, Ext.Window, {
    
});

function permaLinkClickHandler() {
    var activeLayersStore = Ext.StoreMgr.get('active-layers-store');
    
    var serializer = new MapStateSerializer();
    
    serializer.addMapState(map);
    serializer.addActiveLayers(activeLayersStore);
    
    var permaWindow = new PermanentLinkWindow(serializer);
    permaWindow.show();
};