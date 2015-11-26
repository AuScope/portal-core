/**
 * Utility functions for downloading files
 */
Ext.define('portal.util.misc.BrowserWindowWithWarning', {

    statics : {
        windowsId : []
    },


    message : '',


    constructor : function(cfg) {
        if(typeof portal.util.misc.BrowserWindowWithWarning.windowsId[cfg.id]=='undefined'){
            portal.util.misc.BrowserWindowWithWarning.windowsId[cfg.id]=true;
        };
        this.id = cfg.id;
        this.message = cfg.message;
    },

    open : function(callback){
        this._warningMessageBox(this.id,callback);

    },

    _warningMessageBox : function(id,callback){
        var checkBoxid="BrowserWindowWithWarning_" + id;
        if(portal.util.misc.BrowserWindowWithWarning.windowsId[id]==true){
            Ext.MessageBox.show({
                title:    'Browse Catalogue',
                msg:      this.message + '<br><br><input type="checkbox" id="' + checkBoxid + '" value="true" checked/>Do not show this message again',
                buttons:  Ext.MessageBox.OK,
                fn: function(btn) {
                    if( btn == 'ok') {
                        if (Ext.get(checkBoxid).dom.checked == true){
                            portal.util.misc.BrowserWindowWithWarning.windowsId[id]=false;
                        }
                        callback()
                    }
                }
            });
        }else{
            callback();
        }
    }



});