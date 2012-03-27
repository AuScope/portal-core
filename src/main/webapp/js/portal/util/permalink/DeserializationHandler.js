/**
 * Class for turning the contents of the MapStateSerializer into
 * something the AuScope portal can utilise i.e. layers.
 *
 * Adds the following events
 *      deserializedlayer(portal.util.permalink.DeserializationHandler this, portal.layer.Layer newLayer)
 */
Ext.define('portal.util.permalink.DeserializationHandler', {
    extend : 'Ext.util.Observable',


    mapStateSerializer : null,
    knownLayerStore : null,
    cswRecordStore : null,
    layerStore : null,
    map : null,

    /**
     * Creates a new instance of this class with the following config {
     *  mapStateSerializer : [Optional] The portal.util.permalink.MapStateSerializer to load from (if empty, it will be decoded from window.location)
     *  knownLayerStore : A Ext.data.Store containing portal.knownlayer.KnownLayer models
     *  cswRecordStore : A Ext.data.Store containing portal.csw.CSWRecord models
     *  layerStore : A portal.layer.LayerStore which will be populated with deserialized layers
     *  map : A portal.util.gmap.GMapWrapper instance
     * }
     */
    constructor : function(cfg) {
        this.addEvents({
            'deserializedlayer' : true
        });

        Ext.apply(this, cfg);

        this.callParent(arguments);

        //Ensure our deserialisation occurs now (if appropriate) or when our datastores finish loading
        this.knownLayerStore.on('load', this._deserializeIfReady, this, {single : true});
        this.cswRecordStore.on('load', this._deserializeIfReady, this, {single : true});
        this._deserializeIfReady();
    },

    _deserializeIfReady : function() {
        //Ensure both stores are loaded before proceeding
        if (this.knownLayerStore.getCount() === 0 ||
            this.cswRecordStore.getCount() === 0) {
            return;
        }

        //Prepare our map state serializer (if necessary)
        if (!this.mapStateSerializer) {
            var urlParams = Ext.Object.fromQueryString(window.location.search.substring(1));
            if (urlParams && urlParams.state) {
              //IE will truncate our URL at 2048 characters which destroys our state string.
                //Let's warn the user if we suspect this to have occurred
                if (Ext.isIE && window.location.href.length === 2047) {
                    Ext.MessageBox.show({
                        title : 'Mangled Permanent Link',
                        icon : Ext.window.MessageBox.WARNING,
                        msg : 'The web browser you are using (Internet Explorer) has likely truncated the permanent link you are using which will probably render it unuseable. The AuScope portal will attempt to restore the saved state anyway.',
                        buttons : Ext.window.MessageBox.OK,
                        multiline : false
                    });
                }

                this.mapStateSerializer = Ext.create('portal.util.permalink.MapStateSerializer');
                this.mapStateSerializer.deserialize(urlParams.state);
            } else {
                return;
            }
        }

        this._deserialize();
    },

    /**
     * Configures the specified layer with set parameters
     */
    _configureLayer : function(layer, filterParams, visible) {
        var renderer = layer.get('renderer');
        var filterer = layer.get('filterer');
        var filterForm = layer.get('filterForm');

        //Turn off any events before configuring
        layer.suspendEvents(false);
        renderer.suspendEvents(false);
        filterer.suspendEvents(false);

        //Configure our layer/dependencies
        layer.set('deserialized', true);
        renderer.setVisible(visible);
        if (filterParams) {
            filterer.setParameters(filterParams);
        }

        //Turn back on events before proceeding (skipping this will break the portal)
        layer.resumeEvents();
        renderer.resumeEvents();
        filterer.resumeEvents();

        //Configure the filter form (either now or very soon after it loads its internal stores)
        if (filterParams) {
            filterForm.on('formloaded', Ext.bind(function(filterForm, filterer) {
                filterForm.readFromFilterer(filterer);
            }, this, [filterForm, filterer], false));
            if (filterForm.getIsFormLoaded()) {
                filterForm.readFromFilterer(filterer);
            }
        }
    },


    /**
     * Returns the first CSWRecord to contain all specified online resource objects
     */
    _findCSWRecordsByOnlineResources : function(onlineResources) {
        for (var i = 0; i < this.cswRecordStore.getCount(); i++) {
            var cswRecord = this.cswRecordStore.getAt(i);

            var matches = false;
            for (var j = 0; j < onlineResources.length && !matches; j++) {
                matches = cswRecord.containsOnlineResource(onlineResources[j]);
            }

            if (matches) {
                return cswRecord;
            }
        }

        return null;
    },

    _deserialize : function() {
        var s = this.mapStateSerializer;
        var layerFactory = Ext.create('portal.layer.LayerFactory', {map : this.map});
        var missingLayers = false;

        //Update our map location to the specified bounds
        this.map.setZoom(s.mapState.zoom);
        this.map.setCenter(s.mapState.center.lat, s.mapState.center.lng);

        //Add the layers, attempt to load whatever layers are available
        //but warn the user if some layers no longer exist
        for (var i = 0; i < s.serializedLayers.length; i++) {
            var serializedLayer = s.serializedLayers[i];
            if (serializedLayer.source === portal.layer.Layer.KNOWN_LAYER) {
                var id = serializedLayer.id;
                if (!id) {
                    continue;
                }

                var knownLayer = this.knownLayerStore.getById(id);
                if (!knownLayer) {
                    missingLayers = true;
                    continue;
                }

                //Create our new layer
                var newLayer = layerFactory.generateLayerFromKnownLayer(knownLayer);

                //Configure it
                this._configureLayer(newLayer, serializedLayer.filter, serializedLayer.visible);

                //Add this layer to the internal store
                this.layerStore.add(newLayer);

            } else if (serializedLayer.source === portal.layer.Layer.CSW_RECORD) {

                //Turn our serialized online resources into 'actual' online resources
                var onlineResources = [];
                for (var j = 0; j < serializedLayer.onlineResources.length; j++) {
                    onlineResources.push(Ext.create('portal.csw.OnlineResource', {
                        name : serializedLayer.onlineResources[j].name,
                        type : serializedLayer.onlineResources[j].type,
                        description : serializedLayer.onlineResources[j].description,
                        url : serializedLayer.onlineResources[j].url
                    }));
                }

                //Perform a 'best effort' to find a matching CSWRecord
                var cswRecord = this._findCSWRecordsByOnlineResources(onlineResources);
                if (!cswRecord) {
                    missingLayers = true;
                    continue;
                }

                var newLayer = layerFactory.generateLayerFromCSWRecord(cswRecord);

                //Configure it
                this._configureLayer(newLayer, serializedLayer.filter, serializedLayer.visible);

                //Add this layer to the internal store
                this.layerStore.add(newLayer);
            }
        }

        if (missingLayers) {
            Ext.MessageBox.show({
                title : 'Missing Layers',
                icon : Ext.MessageBox.WARNING,
                buttons : Ext.MessageBox.OK,
                msg : 'Some of the saved layers no longer exist and will be ignored. The remaining layers will load normally.',
                multiline : false
            });
        }
    }
});