/**
 * Factory class for creating instances of portal.layer.Layer.
 *
 * Instances are designed to be constructed from portal.cswCSWRecord
 * and portal.knownlayer.KnownLayer objects
 */
Ext.define('portal.layer.LayerFactory', {

    map : null,
    formFactory : null, //an implementation of portal.layer.filterer.FormFactory
    downloaderFactory : null, //an implementation of portal.layer.downloader.DownloaderFactory
    querierFactory : null, //an implementation of portal.layer.querier.QuerierFactory
    rendererFactory : null, //an implementation of portal.layer.renderer.RendererFactory

    /**
     * Creates a new instance of this factory.
     *
     * @param cfg an object in the form
     * {
     *  map : an instance of portal.util.gmap.GMapWrapper
     *  formFactory : an implementation of portal.layer.filterer.FormFactory
     *  downloaderFactory : an implementation of portal.layer.downloader.DownloaderFactory
     *  querierFactory : an implementation of portal.layer.querier.QuerierFactory
     *  rendererFactory : an implementation of portal.layer.renderer.RendererFactory
     * }
     */
    constructor : function(cfg) {
        this.map = cfg.map;
        this.formFactory = cfg.formFactory;
        this.downloaderFactory = cfg.downloaderFactory;
        this.querierFactory = cfg.querierFactory;
        this.rendererFactory = cfg.rendererFactory;

        this.callParent(arguments);
    },

    /**
     * Utility function for generating a new Layer from a set of values. Returns a new instance of portal.layer.Layer
     * @param id String based ID for this instance
     * @param source An instance of portal.csw.CSWRecord or portal.knownlayer.KnownLayer that is generating this layer
     * @param name A human readable name/title of this layer
     * @param description  A human readable description/abstract of this layer
     * @param renderer A concrete implementation of a portal.layer.renderer.Renderer
     * @param filterer A concrete implementation of a portal.layer.filterer.Filterer
     * @param downloader A concrete implementation of a portal.layer.downloader.Downloader
     * @param querier A concrete implementation of a portal.layer.querier.Querier
     * @param cswRecords A single instance or Array of portal.csw.CSWRecord
     */
    generateLayer : function(id, source, name, description, renderer, filterer, downloader, querier, cswRecords) {
        //Generate appropriate sourceType string
        var sourceType = null;
        if (source instanceof portal.knownlayer.KnownLayer) {
            sourceType = portal.layer.Layer.KNOWN_LAYER;
        } else {
            sourceType = portal.layer.Layer.CSW_RECORD;
        }

        //If we have a singleton, turn it into an array
        if (!Ext.isArray(cswRecords)) {
            cswRecords = [cswRecords];
        }

        //Create our instance
        var newLayer = Ext.create('portal.layer.Layer', {
            id : id,
            sourceType : sourceType,
            source : source,
            name : name,
            description : description,
            renderer : renderer,
            filterer : filterer,
            downloader : downloader,
            querier : querier,
            cswRecords : cswRecords,
            loading : false
        });

        //Wire up references to our layer
        renderer.parentLayer = newLayer;

        //Wire up our events so that the layer is listening for changes in its components
        renderer.on('renderstarted', Ext.bind(newLayer.onRenderStarted, newLayer));
        renderer.on('renderfinished', Ext.bind(newLayer.onRenderFinished, newLayer));
        renderer.on('visibilitychanged', Ext.bind(newLayer.onVisibilityChanged, newLayer));
        filterer.on('change', Ext.bind(newLayer.onFilterChanged, newLayer));

        //Create our filter form
        var formFactoryResponse = this.formFactory.getFilterForm(newLayer);
        if (formFactoryResponse) {
            newLayer.set('filterForm', formFactoryResponse.form);
        }
        //VT: Since the new rowExpander design, We do not renderOnAdd for non filtering support layer.
        //newLayer.set('renderOnAdd', !formFactoryResponse.supportsFiltering);
        newLayer.set('renderOnAdd', false);
        
        return newLayer;
    },

    /**
     * Generates a new instance of portal.layer.Layer from an existing KnownLayer object. Appropriate
     * renderers, queriers etc will be generated according to knownLayer's contents
     *
     * @param knownLayer an instance of portal.knownlayer.KnownLayer
     */
    generateLayerFromKnownLayer : function(knownLayer) {
        var id = knownLayer.get('id');
        var source = knownLayer;
        var description = knownLayer.get('description');
        var name = knownLayer.get('name');
        var cswRecords = knownLayer.get('cswRecords');

        //Create our objects for interacting with this layer
        var renderer = this.rendererFactory.buildFromKnownLayer(knownLayer);
        var querier = this.querierFactory.buildFromKnownLayer(knownLayer);
        var filterer = Ext.create('portal.layer.filterer.Filterer', {});
        var downloader = this.downloaderFactory.buildFromKnownLayer(knownLayer);

        return this.generateLayer(id, source, name, description, renderer, filterer, downloader, querier, cswRecords);
    },

    /**
     * Generates a new instance of portal.layer.Layer from an existing CSWRecord object. Appropriate
     * renderers, queriers etc will be generated according to CSWRecord's contents
     *
     * @param cswRecord an instance of portal.csw.CSWRecord
     */
    generateLayerFromCSWRecord : function(cswRecord) {
        
        //VT: a extension of CSWRecord to handle KML
        if(cswRecord.get('resourceProvider')=='kml'){
            return this.generateLayerFromKMLSource(cswRecord);
        }
        
        var id = cswRecord.get('id');
        var source = cswRecord;
        var description = cswRecord.get('description');
        var name = cswRecord.get('name');
        var cswRecords = cswRecord;

        //Create our objects for interacting with this layer
        var renderer = this.rendererFactory.buildFromCswRecord(cswRecord);
        var querier = this.querierFactory.buildFromCswRecord(cswRecord);
        var filterer = Ext.create('portal.layer.filterer.Filterer', {});
        var downloader = this.downloaderFactory.buildFromCswRecord(cswRecord);

        return this.generateLayer(id, source, name, description, renderer, filterer, downloader, querier, cswRecords);
    },
    
    generateLayerFromKMLSource : function(cswRecord){
        var id = cswRecord.get('id');
        var source = cswRecord;
        var description = 'A layer generated from a EPSG:4326 KML file';
        var name = cswRecord.get('name');
        var cswRecords = cswRecord;
        if (!Ext.isArray(cswRecords)) {
            cswRecords = [cswRecords];
        }
        var filterer = Ext.create('portal.layer.filterer.Filterer', {})
        
        var renderer = this.rendererFactory.buildFromKMLRecord(cswRecord);
        
        
        var querier = Ext.create('portal.layer.querier.csw.CSWQuerier', {
            map : this.map
            });
        
        //Create our instance
        var newLayer = Ext.create('portal.layer.Layer', {
            id : id,
            sourceType : portal.layer.Layer.KML_RECORD,
            source : source,
            name : name,
            description : description,
            renderer : renderer,
            filterer : filterer, 
            querier : querier,
            cswRecords : cswRecords,
            loading : false
        });

        //Wire up references to our layer
        renderer.parentLayer = newLayer;

        //Wire up our events so that the layer is listening for changes in its components
        renderer.on('renderstarted', Ext.bind(newLayer.onRenderStarted, newLayer));
        renderer.on('renderfinished', Ext.bind(newLayer.onRenderFinished, newLayer));
        renderer.on('visibilitychanged', Ext.bind(newLayer.onVisibilityChanged, newLayer));
        filterer.on('change', Ext.bind(newLayer.onFilterChanged, newLayer));

        //Create our filter form
        var formFactoryResponse = this.formFactory.getFilterForm(newLayer);
        newLayer.set('filterForm', formFactoryResponse.form);
        newLayer.set('renderOnAdd', true);

        return newLayer;

    }
});