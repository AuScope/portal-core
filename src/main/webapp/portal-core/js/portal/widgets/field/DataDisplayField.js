/**
 * A display field extension that makes the data component
 * of the field the most prominent (compared to the actual label).
 *
 * This field is dependent on the css defined in portal-ux.css
 *
 * Also has support for rendering units of measure.
 */
Ext.define('portal.widgets.field.DataDisplayField', {
    alias : 'widget.datadisplayfield',
    extend : 'Ext.form.field.Display',

    /**
     * This is hardcoded, the label will appear in the 'display' part of the field.
     */
    hideLabel : true,

    fieldCls: Ext.baseCSSPrefix + 'form-value-field',
    labelCls: Ext.baseCSSPrefix + 'form-value-field-label',
    uomCls : Ext.baseCSSPrefix + 'form-value-field-uom',

    /**
     * A unit of measure that is rendered alongside the actual value. Will be omitted if null/undefined
     */
    uom : null,

    /**
     * Style configuration to apply to the uom portion of the field
     */
    uomStyle : null,

    /**
     * This is set in the constructor
     */
    fieldSubTpl : null,

    constructor : function(config) {

        this.uom = config.uom;
        this.uomStyle = config.uomStyle;
        this.uomCls = config.uomCls ? config.uomCls : this.uomCls;
        this.fieldLabel = config.fieldLabel ? config.fieldLabel : '';

        //This template defines the rendering of the data display field (and uom if appropriate)
        //We need to inject the uom values into the template via definitions
        this.fieldSubTpl = [
          '<div id="{id}"',
          '<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>',
          ' class="{fieldCls}">{value}',
          '<tpl if="[uom]"> <span class="{[uomCls]}" style="{[uomStyle]}">{[uom]}</span></tpl>',
          '</div>',
          '<div class="x-form-value-field-label">',
          '{[fieldLabel]}',
          '</div>',{
              compiled: true,
              disableFormats: true,
              //This is where we 'inject' our own variables for use within the template
              definitions : [
                  this.createDefinition('uom', this.uom),
                  this.createDefinition('uomStyle', this.uomStyle),
                  this.createDefinition('uomCls', this.uomCls),
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


