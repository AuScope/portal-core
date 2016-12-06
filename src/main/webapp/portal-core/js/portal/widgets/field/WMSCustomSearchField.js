/**
 * The new upgraded Searchfield doesn't work for us. It broke in multiple places
 * such as custom layer or even searches in known layer registered layer.
 * We have resorted to use the old SearchField and override the 4.1.1 version.
 */
Ext.define('portal.widgets.field.WMSCustomSearchField', {
    extend: 'Ext.form.field.Text',

    alias: 'widget.wmscustomsearchfield',


    hasSearch : false,
    paramName : 'query',

    initComponent: function(){       
        
        this.setTriggers({
            clear:{
                cls: Ext.baseCSSPrefix + 'form-clear-trigger',
                handler: function() {
                    this.clearClick();
                }
            },
            search: {
                cls: Ext.baseCSSPrefix + 'form-search-trigger',
                handler: function() {
                    this.searchClick(false);
                }
            }            
        });
        
        this.callParent(arguments);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.searchClick(false);
            }
        }, this);
    },

    afterRender: function(){
        this.callParent();
        this.triggerCell.item(0).setDisplayed(false);
    },

    clearClick : function(){
        var me = this,
            store = me.store,
            proxy = store.getProxy(),
            val;

        if (me.hasSearch) {
            me.setValue('');
            proxy.extraParams[me.paramName] = '';
            this._clearLayerStore(store);            
            me.hasSearch = false;
            me.triggerCell.item(0).setDisplayed(false);
            me.updateLayout();
        }
    },
    
    _clearLayerStore : function(store){
        store.query("active",true).each(function(record){
            record.get('layer').removeDataFromMap();
        })
        store.removeAll();
    },

    searchClick : function(weakCheck){
        var me = this,
            store = me.store,
            proxy = store.getProxy(),
            value = me.getValue();
        this._clearLayerStore(store); 
        proxy.extraParams[me.paramName] = value;
        // Forces website to avoid extra checking
        if (weakCheck==true) {
          proxy.extraParams['weakCheck'] = 'Y';
        }
        store.loadPage(1);
        store.on('load',function(store, records, successful, eOpts){
            //VT:tracking            
            portal.util.GoogleAnalytic.trackevent('Custom WMS Query','URL:' + value,'ResultCount:'+ store.count(), store.count());
        },this)
        
     
        
        me.hasSearch = true;
        me.triggerCell.item(0).setDisplayed(true);
        me.updateLayout();
    }
});