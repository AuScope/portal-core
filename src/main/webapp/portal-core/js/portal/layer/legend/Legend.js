/**
 * An abstract class for providing the ability
 * for specific portal.layer.Renderer instances
 * to present to the user a Legend that describes
 * what it is that they are rendering on the map.
 *
 */
Ext.define('portal.layer.legend.Legend', {
    extend: 'Ext.util.Observable',
    requires : ['portal.util.UnimplementedFunction'],

    constructor: function(config){

        // Copy configured listeners into *this* object so that the base class's
        // constructor will add them.
        this.listeners = config.listeners;

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * An abstract function for generating a component representing
     * the legend that can be displayed to the user
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer,
     *          function(portal.layer.legend.Legend this, portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer, bool success, portal.layer.legend.BaseComponent legendGui) callback
     *
     * returns - void
     *
     * resources - an array of data sources that were used to render data
     * filterer - custom filter that was applied when rendering the specified data sources
     * callback - Will be called when the legend creation process is completed and passed an instance of this Legend and the parameters used to call this function
     * staticLegendUrl - a url to a canned legend image
     */
    getLegendComponent : portal.util.UnimplementedFunction,

    /**
     * An abstract function for generating a html snippet for some form of icon
     * that will be used to represent an overview of this legend.
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer)
     *
     * returns - string
     *
     * resources - an array of data sources that were used to render data
     * filterer - custom filter that was applied when rendering the specified data sources
     */
    getLegendIconHtml : portal.util.UnimplementedFunction

});