/**
 * An abstract Ext.Panel extension forming the base
 * for all Generic Parser components to derive from
 */
Ext.ns('GenericParser');
GenericParser.BaseComponent = Ext.extend(Ext.Panel, {
    /**
     * Accepts all Ext.Panel configuration options
     */
    constructor : function(cfg) {
        GenericParser.BaseComponent.superclass.constructor.call(this, cfg);
    }
});

Ext.reg('genericparsercomponent', GenericParser.BaseComponent);