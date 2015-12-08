/**
 * A display field extension that makes the data component
 * of the field the most prominent (compared to the actual label).
 *
 * This particular display field is specialised into rendering an image as the data component
 */
Ext.define('portal.widgets.field.ImageDisplayField', {
    alias : 'widget.imagedisplayfield',
    extend : 'Ext.form.field.Display',

    /**
     * This is hardcoded, the label will appear in the 'display' part of the field.
     */
    hideLabel : true,

    fieldCls: Ext.baseCSSPrefix + 'form-image-field',
    labelCls: Ext.baseCSSPrefix + 'form-image-field-label',


    /**
     * URL of the image to render
     */
    imgHref : null,
    /**
     * Width of the image to render in pixels
     */
    imgWidth : null,
    /**
     * Height of the image to render in pixels
     */
    imgHeight : null,
    

    /**
     * This is set in the constructor
     */
    fieldSubTpl : null,

    constructor : function(config) {

        this.imgHref = config.imgHref;
        this.imgWidth = config.imgWidth;
        this.imgHeight = config.imgHeight;
        this.fieldLabel = config.fieldLabel ? config.fieldLabel : '';

        //This template defines the rendering of the data display field (and uom if appropriate)
        //We need to inject the uom values into the template via definitions
        this.fieldSubTpl = [
          '<div id="{id}"',
          '<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>',
          ' class="{fieldCls}">',
          '<img style="margin: auto;" width="{[imgWidth]}" height="{[imgHeight]}" src="{[imgHref]}" alt="Loading..."/>',
          '</div>',
          '<div class="x-form-value-field-label">',
          '{[fieldLabel]}',
          '</div>',{
              compiled: true,
              disableFormats: true,
              //This is where we 'inject' our own variables for use within the template
              definitions : [
                  this.createDefinition('imgHref', this.imgHref),
                  this.createDefinition('imgHeight', this.imgHeight),
                  this.createDefinition('imgWidth', this.imgWidth),
                  this.createDefinition('fieldLabel', this.fieldLabel)
              ]
        }];

        this.callParent(arguments);
    },

    /**
     * Utility function for generating javascript in the form
     * 'var variableName = value'
     */
    createDefinition : function(variableName, value) {
        if (Ext.isString(value)) {
            return Ext.util.Format.format('var {0} = "{1}";', variableName, value);
        } else if (Ext.isObject(value)) {
            //Assume an object is a style object
            return this.createDefinition(variableName, Ext.DomHelper.generateStyles(value));
        } else {
            return Ext.util.Format.format('var {0} = {1};', variableName, value);
        }
    }
});


