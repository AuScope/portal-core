/**
 * A utility function for representing a function that hasn't been implemented yet. Any calls to this function
 * will result in console error logs and exceptions being thrown
 */
Ext.define('portal.util.UnimplementedFunction', {
    singleton: true
}, function() {
    portal.util.UnimplementedFunction = function() {
        if (window.console) {
            console.error('This function is not implemented yet', arguments);
        }
        throw 'NotImplemented';
    }
});