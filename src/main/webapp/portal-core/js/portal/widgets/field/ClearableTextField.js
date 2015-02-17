Ext.define('portal.widgets.field.clearabletextfield', {
    extend: 'Ext.form.field.Text',
    alias: 'widget.clearabletextfield',
    initComponent: function () {  
        
        this.setTriggers({
            clearKey: {
                cls: 'x-form-clear-trigger',
                handler: function() {
                    this.setRawValue('');
                }
            }     
        });
                                       
        this.callParent();
       
    }       
});