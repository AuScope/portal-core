/**
 * An Ext.data.Types extension for portal.util.BBox
 *
 * See http://docs.sencha.com/ext-js/4-0/#!/api/Ext.data.Types
 */
Ext.define('portal.util.BBoxType', {
    singleton: true,
    requires: ['Ext.data.SortTypes',
               'Ext.data.Types']
}, function() {
    Ext.apply(portal.util.BBoxType, {
        convert: function(v, data) {
            if (Ext.isArray(v)) {
                var newArray = [];
                for (var i = 0; i < v.length; i++) {
                    newArray.push(this.convert(v[i]));
                }
                return newArray;
            } else if (v instanceof portal.util.BBox) {
                return v;
            } else if (Ext.isObject(v)) {
                return Ext.create('portal.util.BBox', v);
            }

            return null;
        },
        sortType: Ext.data.SortTypes.none,
        type: 'portal.util.BBox'
    });
});