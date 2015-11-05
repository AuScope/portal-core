/**
 * There are ExtJs events (http://docs-devel.sencha.com/extjs/4.2.1/#!/api/Ext.app.EventDomain) but they require ExtJs
 * Controllers, which the portal doesn't use. Objects can fire events but they have to be to another specific object.
 * And sometimes one or both of the objects are disconnected from each other. And besides, an event mechanism should be
 * decoupled so that one object doesn't have to know about another. So I developed this simple class. It simply allows
 * an object to register itself to receive events and then any class can use this to broadcast events. There are no
 * smarts to it.
 * 
 * To set up a listener simply add it to this Singleton object as a listener and implement whatever events it wants to
 * react to:
 * 
 * 1. portal.events.AppEvents.addListener(this, args)
 * 
 * 2. In the listener - ... listeners { myEvent: function (args) { ... } }
 * 
 * It is up to the app to manage the args required for its events and to check they exist in the listeners.
 * 
 * 3. The broadcasting of events is easy. portal.events.AppEvents.broadcast('myEvent', [arg1, arg2]);
 * 
 * The args broadcast are optional and are combined with the args set with the addListener. The args received by the
 * listener will be an array of addListener args concatenated with broadcast args (addListener args will come first).
 * 
 * NOTE that you can register a listener and you don't have to define any listeners for specific events. If an event
 * listener doesn't exist it is not an error.
 * 
 * NOTES ON ExtJS Events
 * - ExtJs broadcasts these and you simply need to create listeners for them in the appropriate class.
 * 
 * NOTES ON ARGS
 * - They should be associative arrays.  
 * - The only exception is that there can be ONE arg for both the addListener and broadcast but not both.  If you need
 *   to have an arg each for these then put them into an associative array
 * - associative arrays will be combined into one (undefined behaviour if same key used in both)
 * - If one is a scalar and another an Assoc Array then the scalar will become 'other' in the Assoc Array
 */

Ext.define('portal.events.AppEvents', {
    singleton : true,
    alternateClassName: ['AppEvents'],
//    //listeners : null,
    listeners : {},
    constructor : function(config) {
        this.initConfig(config);
    },
    clear : function() {
        this.listeners = {};
    },
    /**
     * Add listener to call back (all are called) when an event is broadcast.  All registered listeners will receive the event
     * but only those that declare a listener of the event name in its "listners" object will actually receive it.
     * 
     * listener: listener is the object listener callback
     * args: args - either an associative array (such as if multiple items) or a single item 
     */
    addListener : function (listener, args) {
//      var theArgs
//      if (args && typeof(args) != "object") {
//          // must be a number or string or similar - wrap in an array and when broadcasting if there are no broadcasted args
//          // then return this single item (not in an array).  If broadcasted args then concatenate into the array
//          theArgs = [args];
//      }
      var id = listener.uniqueId; // Defined in js/admin/global.js
      var entry = {listener:listener,args:args};
      this.listeners[id]=entry;

  },
  removeListener : function (listener) {
      var id = listener.uniqueId; // Defined in js/admin/global.js
      if (this.listeners[id]) {
          delete this.listeners[id];
          //console.log("AppEvents - removeListener - id: ", id)
      } else {
          //console.log("AppEvents - NOT removeListener as doesnt exist - id: ", id)
      }
  },
  broadcast : function (event, args) {
      var me = this;
      //for (var listener in this.listeners) {
      Object.keys(this.listeners).forEach(function(id, index) {
          var listener=me.listeners[id].listener;
          var listenerArgs=me.listeners[id].args;
          var theArgs = me._combineArgs(args, listenerArgs);
          
//          var theseArgs = (Array.isArray(args) || ! args) ? args : [args];
//          var allArgs = listenerArgs ? (theseArgs ? listenerArgs.concat(theseArgs) : listenerArgs) : theseArgs ? theseArgs : [];

          listener.fireEvent(event, theArgs);
      },this.listeners);
  },
  /**
   * Combine Assoc Arrays into one.
   */
  _combineArgs : function (leftArgs, rightArgs) {
      if (! leftArgs) {
          leftArgs = {};
      }
      if (! rightArgs) {
          rightArgs = {};
      }
      if (typeof(leftArgs) != "object" && typeof(rightArgs) != "object") {
          Ext.Error.raise("Cannot have both scalar args for listener and broadcaster")
      }
      if (typeof(leftArgs) != "object") {
          leftArgs = {other:leftArgs};
      }
      if (typeof(rightArgs) != "object") {
          rightArgs = {other:rightArgs};
      }
      
      theCombinedArgs = Object.extend(leftArgs, rightArgs);
      return theCombinedArgs;
  },
  
  getListeners : function() {
      return this.listeners;
  }
});