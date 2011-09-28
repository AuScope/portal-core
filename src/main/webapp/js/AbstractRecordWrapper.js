/**
 * An abstract class that wraps an ExtJS Ext.data.Record with the
 * intention of subclasses providing domain specific functions/events
 * that are much more user friendly than the typical record interface
 */
AbstractRecordWrapper = Ext.extend(Ext.util.Observable, {
    internalRecord : null,

    /**
     * Accepts a Ext.util.Observable configuration object with the following extensions
     * {
     *  internalRecord : Ext.data.Record - The record to be wrapped (will be assigned to internalRecord)
     * }
     */
    constructor : function(cfg) {
        this.internalRecord = cfg.internalRecord;
        AbstractRecordWrapper.superclass.constructor.call(this, cfg);
    },

    /**
     * Given a field name, return its value.
     *
     * If the fieldName returns a null or undefined response
     * defaultValue will instead be returned
     */
    getFieldWithDefault : function(fieldName, defaultValue) {
        var fieldValue = this.internalRecord.get(fieldName);
        if (fieldValue === null || fieldValue === undefined) {
            return defaultValue;
        }
        return fieldValue;
    },

    /**
     * Given a field name, return it's value as a String
     *
     * If defaultValue is not specified, an empty string will be used
     */
    getStringField : function(fieldName, defaultValue) {
        if (!defaultValue) {
            defaultValue = '';
        }
        return this.getFieldWithDefault(fieldName, defaultValue)
    },

    /**
     * Given a field name, return it's value as an Array
     *
     * If defaultValue is not specified, an empty Array will be used
     */
    getArrayField : function(fieldName, defaultValue) {
        if (!defaultValue) {
            defaultValue = [];
        }
        return this.getFieldWithDefault(fieldName, defaultValue)
    },

    /**
     * Given a field name, return it's value as a Boolean
     *
     * If defaultValue is not specified, false will be used
     */
    getBooleanField : function(fieldName, defaultValue) {
        if (defaultValue === null || defaultValue === undefined) {
            defaultValue = false;
        }
        return this.getFieldWithDefault(fieldName, defaultValue)
    },

    /**
     * Given a field name, return it's value as a Number
     *
     * If defaultValue is not specified, 0 will be used
     */
    getNumberField : function(fieldName, defaultValue) {
        if (defaultValue === null || defaultValue === undefined) {
            defaultValue = 0;
        }
        return this.getFieldWithDefault(fieldName, defaultValue)
    }
});