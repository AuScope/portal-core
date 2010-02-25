

// Create user extensions namespace (Ext.ux)
Ext.namespace('Ext.ux.grid');
 
/**
  * Ext.ux.EventCheckColumn Extension Class
  *
  * Similar to CheckColumn but with a public event handler for onMouseDown
  */
Ext.ux.grid.EventCheckColumn = function(config){
    Ext.apply(this, config);
    if(!this.id){
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
    this.handler = config.handler;
};

Ext.ux.grid.EventCheckColumn.prototype ={
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },

    onMouseDown : function(e, t){
        if(Ext.fly(t).hasClass(this.createId())){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            
            this.handler(record, record.data[this.dataIndex]);
        }
    },

    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td'; 
        return String.format('<div class="x-grid3-check-col{0} {1}">&#160;</div>', v ? '-on' : '', this.createId());
    },
    
    createId : function(){
        return 'x-grid3-cc-' + this.id;
    }
};

// register ptype
Ext.preg('checkcolumn', Ext.ux.grid.EventCheckColumn);
