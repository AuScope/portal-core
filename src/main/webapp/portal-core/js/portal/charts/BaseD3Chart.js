/**
 * Panel extension for graphing a D3 (d3.org) chart. This class
 * is intended to be extended and plot overridden.
 * 
 * This class provides a wrapper around a chart that will handle 
 * all resizing and aspect ratio setup.
 */
Ext.define('portal.charts.BaseD3Chart', {
    extend: 'Ext.panel.Panel',

    d3svg : null, //D3 SVG element. Will always be set after render
    d3 : null, //D3 elements (graphs, lines etc). Can be null
    targetWidth: null,
    targetHeight : null,


    /**
     * Adds the following config
     * {
     *  initialPlotData - Object - [Optional] - Passed to the plot function after render if specified (otherwise nothing will initially render)
     *  preserveAspectRatio - boolean - Should the graph preserve a 4x2 aspect ratio or should it stretch. Default false
     *  targetWidth - Number - (Only useful if preserveAspectRatio is set) - The target width to use in aspect ratio
     *  targetHeight - Number - (Only useful if preserveAspectRatio is set)  - The target height to use in aspect ratio
     *  svgClass - String - [Optional] - CSS Class to apply to underlying SVG element (defaults to nothing)
     * }
     *
     */
    constructor : function(config) {

        this.initialPlotData = config.initialPlotData ? config.initialPlotData : null;
        this.d3svg = null;
        this.d3 = null;
        this.preserveAspectRatio = config.preserveAspectRatio ? true : false;
        this.targetWidth = config.targetWidth ? config.targetWidth : 800;
        this.targetHeight = config.targetHeight ? config.targetHeight : 400;

        this.innerId = Ext.id();

        Ext.apply(config, {
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>', this.innerId)
        });

        this.callParent(arguments);

        this.on('render', this._afterRender, this);
        this.on('resize', this._onResize, this);
    },

    /**
     * Removes any chart elements. Optionally displays a message
     */
    clearPlot : function(message) {
        if (!this.d3svg) {
            return;
        }

        this.d3svg.select('*').remove();
        this.d3svg.select('.title').remove();
        this.d3 = null;

        this.maskClear();

        if (message) {
            this.d3svg.append("text")
            .attr('x', this.targetWidth/2)
            .attr('y', (this.targetHeight / 2) - 20)
            .attr("width", this.targetWidth)
            .attr("class", "error-text")
            .style("text-anchor", "middle")
            .style("font-size", "32px")
            .text(message)
        }

    },

    /**
     * Set a loading mask for this chart with the specified message
     */
    mask : function(message) {
        if (this._loadMask) {
            this.maskClear();
        }

        this._loadMask = new Ext.LoadMask({
            msg:message,
            target : this
            });
        this._loadMask.show();
    },

    /**
     * Clear any loading mask (or error message) from this panel.
     */
    maskClear : function() {

        this.d3svg.select('.error-text').remove();

        if (this._loadMask) {
            this._loadMask.hide();
            this._loadMask = null;
        }
    },

    /**
     * Will plot the specified data
     * 
     * function(data)
     * data - Object -  
     */
    plot : Ext.util.UnimplementedFunction,


    _afterRender : function() {
        //Load our D3 Instance
        var container = d3.select("#" + this.innerId);
        this.d3svg = container.append("svg")
            .attr("preserveAspectRatio", this.preserveAspectRatio ? "xMidYMid" : "none")
            .attr("viewBox",  Ext.util.Format.format("0 0 {0} {1}", this.targetWidth, this.targetHeight));
        
        if (this.svgClass) {
            this.d3svg.attr("class", this.svgClass)
        }

        //Force a resize
        this._onResize(container.node().offsetWidth, container.node().offsetHeight);

        //Load our data
        if (this.initialPlotData) {
            this.plot(this.initialPlotData);
        }
    },

    _onResize : function(me, width, height) {
        this.d3svg
            .attr("width", width)
            .attr("height", height);
    }


});