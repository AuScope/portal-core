/**
 * An extension to numberfield to ensure that keypress events are called as the field is updated (less a specific delay)
 *
 * Additional Options
 * endTypingDelay - delay in ms that a change event will fire after the user stops typing. Set to 0 to disable this feature
 */
Ext.ns('Ext.ux.form');
Ext.ux.form.LiveNumberField = Ext.extend(Ext.form.NumberField, {


    constructor : function(config) {

        Ext.apply(config, {
            //Overrides
            endTypingDelayedTask : new Ext.util.DelayedTask(),
            enableKeyEvents : config.endTypingDelay !== 0
        },{
            //Defaults
            //endTypingDelay : 0
        });

        Ext.ux.form.LiveNumberField.superclass.constructor.call(this, config);

        this.on('keypress', function(field, e) {
            if (field.endTypingDelay) {
                field.endTypingDelayedTask.delay(field.endTypingDelay, field.onEndOfTyping, field);
            }
        });
    },

    getValue : function() {
        var value = Ext.ux.form.LiveNumberField.superclass.getValue.call(this);

        if (value === '') {
            return Number.NaN;
        }

        return value;
    },

    setValue : function(newValue) {
        var oldValue = this.getValue();
        Ext.ux.form.LiveNumberField.superclass.setValue.call(this, newValue);

        this.fireEvent('change', this, oldValue, newValue);
    },

    onEndOfTyping : function() {
        this.fireEvent('change', this, '', this.getValue());
    }

});


Ext.reg('livenumberfield', Ext.ux.form.LiveNumberField);
