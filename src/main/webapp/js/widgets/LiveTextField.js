/**
 * An extension to textfield to ensure that keypress events are called as the field is updated (less a specific delay)
 *
 * Additional Options
 * endTypingDelay - delay in ms that a change event will fire after the user stops typing. Set to 0 to disable this feature
 */
Ext.ns('Ext.ux.form');
Ext.ux.form.LiveTextField = Ext.extend(Ext.form.TextField, {


    constructor : function(config) {

        Ext.apply(config, {
            //Overrides
            endTypingDelayedTask : new Ext.util.DelayedTask(),
            enableKeyEvents : config.endTypingDelay !== 0
        },{
            //Defaults
            //endTypingDelay : 0
        });

        Ext.ux.form.LiveTextField.superclass.constructor.call(this, config);

        this.on('keypress', function(field, e) {
            if (field.endTypingDelay) {
                field.endTypingDelayedTask.delay(field.endTypingDelay, field.onEndOfTyping, field);
            }
        });
    },

    setValue : function(newValue) {
        var oldValue = this.getValue();
        Ext.ux.form.LiveTextField.superclass.setValue.call(this, newValue);

        this.fireEvent('change', this, oldValue, newValue);
    },

    onEndOfTyping : function() {
        this.fireEvent('change', this, '', this.getValue());
    }

});


Ext.reg('livetextfield', Ext.ux.form.LiveTextField);
