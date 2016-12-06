/**
 * An instruction manager takes a set of portal.util.help.Instruction objects and creates a step by step
 * wizard for highlighting each instruction in sequence.
 */

Ext.require([  
    'Ext.ux.Spotlight'
]);

Ext.define('portal.util.help.InstructionManager',{ 
    
    spot : null, //Ext.ux.Spotlight,
    tip : null, //Ext.tip.ToolTip
    currentInstruction : 0,

    /**
     * Accepts the following {
     *  spotCfg : Applied to the internal Ext.ux.Spotlight. If not specified, Spotlight default values will be used
     * }
     */
    constructor : function(cfg) {
        var spotCfg = cfg.spotCfg ? cfg.spotCfg : {};

        this.spot = Ext.create('Ext.ux.Spotlight', spotCfg);
        this.callParent(arguments);
    },

    /**
     * Shows a single instruction, closes any existing instructions. If index is invalid,
     * any current instruction will be hidden and nothing will be shown.
     */
    _showInstruction : function(index, instructions) {
        
        if(index < instructions.length && index >= 0){
            portal.util.GoogleAnalytic.trackevent('HelpHandlerClick', 'Step:' + index,'HelpTitle:' + instructions[index].get('title'));
        }
        
        //Move the spotlight
        var instr = (index >= instructions.length || index < 0) ? null : instructions[index];

        //Close any existing tips
        if (this.tip) {
            this.tip.hide();
        }

        //Remove or move spotlight
        if (instr) {
            this.spot.show(instr.get('highlightEl'));
        } else {
            this.spot.hide();
        }

        // Show info popup alongside highlighted element
        if (instr) {
            var btnText = 'Next >>';
            if (index === (instructions.length - 1)) {
                btnText = 'Finish';
            }

            this.tip = Ext.create('Ext.tip.ToolTip', {
                target : instr.get('highlightEl'),
                anchor : instr.get('anchor'),
                closable : true,
                autoHide : false,
                title : instr.get('title'),
                html : instr.get('description'),
                buttons : [{
                    text : btnText,
                    scope : this,
                    index : index,
                    instructions : instructions,
                    handler : function(btn) {
                        this._showInstruction(btn.index + 1, btn.instructions);
                    }
                }],
                listeners : {
                    scope : this,
                    destroy : function() {
                        this.tip = null;
                    },
                    close : function() {
                        //If this is closed and not progressed by hitting next - abort the instruction set
                        this.spot.hide();
                    },
                    hide : function() {
                        //We don't want this popping up again
                        this.tip.destroy();
                    }
                }
            });
            this.tip.show();
        }
    },

    /**
     * Shows the series of instruction starting at instruction 0
     *
     * @param instructions portal.util.help.Instruction[] - the set of instructions to manage
     */
    showInstructions : function(instructions) {
        this.currentInstruction = 0;
        this._showInstruction(this.currentInstruction, instructions);
    }
});