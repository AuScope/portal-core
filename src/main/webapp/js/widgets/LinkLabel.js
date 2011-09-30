/**
 * An Label (Similar to Ext.form.Label) that provides an active link/tooltip for the user
 * to select
 */
LinkLabel = Ext.extend(Ext.BoxComponent, {

    cswRecord : null,

    /**
     * Constructor for this class, accepts all configuration options that can be
     * specified for a Ext.BoxComponent as well as the following values
     * {
     *  qtip : [Optional] String - Displayed when the user mouse overs the link portion of this label
     *  href : String - Where the user should be redirected to on selection
     *  text : String - The text to be displayed in the label
     * }
     */
    constructor : function(cfg) {
        Ext.apply(cfg, {
            isFormField : true,
            autoEl : {
                tag : 'div',
                children : [{
                    tag : 'a',
                    target : '_blank',
                    qtip : cfg.qtip,
                    href : cfg.href,
                    html : cfg.text
                }]
            }
        });

        LinkLabel.superclass.constructor.call(this, cfg);
    }
});

Ext.reg('linklabel', LinkLabel);