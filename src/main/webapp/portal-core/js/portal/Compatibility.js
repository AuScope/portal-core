// This file is for any global level workarounds for browser (in)compatibility
// issues. Ideally it should be limited to avoiding JS errors, not re-implementing
// functionality.
//

//The portal uses some console logging which is not defined in some browsers
//Workaround it by providing an empty logging interface
if (!window.console) {
    window.console = {
        log : function() {},
        warn : function() {}
    };
}

//Sourced from http://stackoverflow.com/questions/280634/endswith-in-javascript
if (!String.prototype.endsWith) {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}
