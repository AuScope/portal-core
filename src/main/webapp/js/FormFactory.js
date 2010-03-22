//function FormFactory() {

/*Ext.override(Ext.form.Field, {
    hideItem :function(){
        this.formItem.addClass('x-hide-' + this.hideMode);
    },

    showItem: function(){
        this.formItem.removeClass('x-hide-' + this.hideMode);
    },
    setFieldLabel: function(text) {
    var ct = this.el.findParent('div.x-form-item', 3, true);
    var label = ct.first('label.x-form-item-label');
    label.update(text);
  }
});*/

FormFactory = function() {};

FormFactory.prototype.getFilterForm = function(record, map) {

    if (record.get('serviceType') == 'wms') {
        //return new WMSLayerFilterForm(record, map);

        //We have to resort to NOT using an inherited FormPanel as card layout falls apart with multiple inherited instances of the same class
        return generateWMSLayerFilterForm(record, map);
    } else {
        switch (record.get('typeName')) {
            case 'er:Mine': return new MineFilterForm(record.get('id'), record.get('serviceURLs')[0]); break;
            case 'er:MiningActivity': return new MiningActivityFilterForm(record.get('id'), record.get('serviceURLs')[0]); break;
            case 'er:MineralOccurrence': return new MineralOccurrenceFilterForm(record.get('id')); break;
            default: return null; break;
        }
    }
};