/**
 * A simple data structure resembling a map that has events for
 * whenever key(s) in said map change
 *
 * events :
 *      change(portal.layer.filterer.Filterer this, String[] keys)
 *          Fired whenever the map changes, passed an array of all keys that have changed.
 */
Ext.define('portal.util.ObservableMap', {
    extend: 'Ext.util.Observable',

    parameters : null,

    constructor: function(config){
       

        // Copy configured listeners into *this* object so that the base class's
        // constructor will add them.
        this.listeners = config.listeners;
        this.parameters = {};

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Gets the set of parameters configured within this map as
     * a simple javascript object with key/value pairs
     *
     * returns - a javascript object
     */
    getParameters : function() {
        return Ext.apply({}, this.parameters); //return a copy of the internal object
    },

    /**
     * Given a set of parameters as a plain old javascript object of
     * key/value pairs, apply it's contents to this map.
     *
     * This is a useful function if you want to set multiple parameters
     * and only raise a single event
     *
     * parameters - a plain old javascript object
     * clearFirst - [Optional] if true, then the internal map will be cleared BEFORE any values are added
     */
    setParameters : function(parameters, clearFirst) {
        //keep track of the list of keys that changed
        //Initially only the values in parameters are the values that are changing
        var changedParameters = Ext.apply({}, parameters);

        //We can optionally wipe out all parameters during this function
        if (clearFirst) {
            //However, if we are clearing the map first then a lot of values will be changing
            changedParameters = Ext.apply(changedParameters, this.parameters);
            this.parameters = {}; //clearing first is really easy
        }

        //Apply parameter values to the internal map
        this.parameters = Ext.apply(this.parameters, parameters);

        //Enumerate our list of changed parameters to pass the values
        //to whatever event handlers are listening
        var key;
        var keyList = [];
        for (key in changedParameters) {
            keyList.push(key);
        }
        this.fireEvent('change', this, keyList);
    },

    /**
     * Sets a single parameter of this map
     *
     * key - a string key whose value will be set. Will override any existing key of the same name
     * value - The object value to set
     * quiet[optional] - setting to true won't fire any events
     */
    setParameter : function(key, value, quiet){
        this.parameters[key] = value;
        if(quiet){
            return;
        }
        this.fireEvent('change', this, [key]);
    },

    /**
     * Gets the value of the specified key as an Object
     *
     * key - A string key whose value will be fetched.
     *
     * returns - a javascript object matching key
     */
    getParameter : function(key) {
        return this.parameters[key];
    },

    /**
     * Returns a shallow copy of all of this map's objects and keys.
     */
    clone : function() {
        var clonedObj = Ext.create('portal.util.ObservableMap', {});
        Ext.apply(clonedObj.parameters, this.parameters);
        return clonedObj;
    }
});