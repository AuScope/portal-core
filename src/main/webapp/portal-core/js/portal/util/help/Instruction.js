/**
 * Represents a single instruction for the purposes of helping a user.
 *
 */
Ext.define('portal.util.help.Instruction', {
    extend : 'Ext.data.Model',

    fields: [
        { name: 'highlightEl', type: 'auto' }, //The page element which will be highlighted by this particular instruction. This can be a ExtJS element, DOM element or a string id of a DOM element
        { name: 'anchor', defaultValue: 'left', type: 'string'}, //Should the description be anchored to the left/right/top/bottom of the hightlightEl?. Defaults to 'left'
        { name: 'title', type: 'string' }, //The title of this instruction (should be short)
        { name: 'description', type: 'string' } //The longer description of this instruction
    ]
});