/**
 * A search field that performs local filtering on a store
 * instead of proxying external requests
 */
Ext.define('portal.widgets.field.ClientSearchField', {
    extend : 'Ext.ux.form.SearchField',
    alias : 'widget.clientsearchfield',

    wordListSplitString : ' ', //Will be used to split our searches into a series of words

    initComponent : function() {
        this.callParent(arguments);
        this.store.setRemoteFilter(false);
    },

    /**
     * Disables the text field, leaves any trigger buttons enabled
     */
    _setTextFieldDisabled : function(disabled) {
        var inputFieldEl = Ext.get(this.getInputId());
        inputFieldEl.dom.disabled = disabled;

        //Manual styling because we cannot Ext.Element.mask an input field (it accepts
        //no child nodes)
        if (disabled) {
            inputFieldEl.setStyle('background', '#E5E5E5');
            inputFieldEl.setStyle('color', '#666666');
        } else {
            inputFieldEl.setStyle('background', '#FFFFFF');
            inputFieldEl.setStyle('color', '#000000');
        }
    },

    onClearClick : function(){
        var me = this,
            store = me.store,
            proxy = store.getProxy(),
            val;

        if (this.hasSearch) {
            this.setValue('');

            this.store.clearFilter(false);

            this.hasSearch = false;
            me.triggerCell.item(0).setDisplayed(false);
            me.triggerCell.item(1).setDisplayed(true);

            this._setTextFieldDisabled(false);

            this.updateLayout();
        }
    },

    onSearchClick : function(){
        var v = this.getRawValue();
        if(v.length < 1){
            this.onClearClick();
            return;
        }

        this.store.clearFilter(false);
        //VT: http://www.sencha.com/forum/showthread.php?297797-Ext-5.1.107-Store.filterBy-does-not-pass-record-id-down-to-filterFn
        this.store.filterBy(Ext.bind(this.filterByWord, this, [v.split(this.wordListSplitString)], true));     
        
        //VT:Tracking
        portal.util.PiwikAnalytic.siteSearch(v,this.getId(),this.store.count());

        this.hasSearch = true;
        this.triggerCell.item(0).setDisplayed(true);
        this.updateLayout();
    },

    filterByWord : function(record, wordsToFind) {
        var wordList = record.get(this.fieldName).split(this.wordListSplitString);

        //Function for testing if a list of words contains a particular word (or prefix)
        var containsMatch = function(wordList, word) {
            for (var i = 0; i < wordList.length; i++) {
                var lowerMatchWord = Ext.String.trim(wordList[i].toLowerCase());
                var lowerWord = Ext.String.trim(word.toLowerCase());

                if (lowerMatchWord === lowerWord || lowerMatchWord.indexOf(lowerWord) === 0) {
                    return true;
                }
            }
            return false;
        };

        for (var i = 0; i < wordsToFind.length; i++) {
            if (!containsMatch(wordList, wordsToFind[i])) {
                return false;
            }
        }
        return true;
    },

    /**
     * text : The text to include in the box (to indicate that a custom filter has been run)
     * func : function(record, id) that should return true/false for each record it receives
     */
    runCustomFilter : function(text, func) {
        //Clear any existing filter
        this.onClearClick();

        this.hasSearch = true;
        this.setValue(text);

        this.store.filterBy(func);
        this.triggerCell.item(0).setDisplayed(true);
        this.triggerCell.item(1).setDisplayed(false);

        this._setTextFieldDisabled(true);
        //inputFieldEl.mask();

        //VT:Tracking
        portal.util.PiwikAnalytic.siteSearch(text,this.getId(),this.store.count());

        this.updateLayout();
    },
    
    /**
     * text : The text to include in the box (to indicate that a custom filter has been run)
     * func : function(record, id) that should return true/false for each record it receives
     */
    clearCustomFilter : function() {
        //Clear any existing filter
        this.onClearClick();

        this.hasSearch = false;
        this.setValue("");
      
        this.triggerCell.item(0).setDisplayed(false);
        this.triggerCell.item(1).setDisplayed(true);

        this._setTextFieldDisabled(false);
        //inputFieldEl.mask();


        this.updateLayout();
    }

});