//VGLOPS-23 Fixes unsized TextFields from oversizing the height of the text field.
Ext.define('Auscope.plugin.FieldHelpText', {
    override : 'Ext.ux.form.plugin.FieldHelpText',
    
    init: function(field)
    {
        if (!field.isFieldLabelable)
            Ext.Error.raise('FieldHelpText plugin has to be used on form fields that implement Ext.form.Labelable.');

        // only apply if text available
        if (!Ext.isEmpty(this.text))
        {
            var markup = '<div class="' + (this.cls || '') + '">' + this.text + '</div>';
            if (this.align == 'top')
                field.beforeSubTpl = this.getSubTpl(field.beforeSubTpl || '', markup, '');
            else
                field.afterSubTpl = this.getSubTpl(field.afterSubTpl || '', '', markup);
            
            //VGLOPS-23 - Begin Changes 
            if (field.rendered) {
                field.getEl().down('.x-form-item-body').dom.style.height = 'inherit';
            } else {
                field.on('afterrender', function() {
                    this.getEl().down('.x-form-item-body').dom.style.height = 'inherit';
                }, field, {single: true});
            }
            //VGLOPS-23 - End Changes
        }
    }
});
