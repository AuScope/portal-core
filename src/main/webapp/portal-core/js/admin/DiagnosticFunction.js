/**
 * A diagnostic function is a piece of functionality that is run on demand.
 *
 * It typically does some work and returns a result. They are intended to be used
 * to help administer a running portal.
 */
Ext.define('admin.DiagnosticFunction', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'string' }, //A short descriptive name of this function
        { name: 'description', type: 'string' }, //A longer description of this function
        { name: 'group', type: 'string'}, //A descriptive group that will be used for organising like functionality
        { name: 'executeFn', type: 'auto'} //A function that when called will attempt to perform the work of this diagnostic function.
                                           //The work can be performed asynchronously hence the function is passed a callback function as an arg
                                           //executeFn(Function callback)
                                           //callback - function(success, message) - will be called when the diagnostic function finishes doing its work

    ]
});