/**
 * @class Ext.chart.series.Radar
 * @extends Ext.chart.series.Polar
 *
 * Creates a Radar Chart. A Radar Chart is a useful visualization technique for comparing different quantitative values for
 * a constrained number of categories.
 * As with all other series, the Radar series must be appended in the *series* Chart array configuration. See the Chart
 * documentation for more information. A typical configuration object for the radar series could be:
 *
 *     @example
 *     Ext.create('Ext.Container', {
 *         renderTo: Ext.getBody(),
 *         width: 600,
 *         height: 400,
 *         layout: 'fit',
 *         items: {
 *             xtype: 'polar',
 *             interactions: 'rotate',
 *             store: {
 *                 fields: ['name', 'data1', 'data2', 'data3', 'data4', 'data5'],
 *                 data: [
 *                     {'name':'metric one', 'data1':10, 'data2':12, 'data3':14, 'data4':8, 'data5':13},
 *                     {'name':'metric two', 'data1':7, 'data2':8, 'data3':16, 'data4':10, 'data5':3},
 *                     {'name':'metric three', 'data1':5, 'data2':2, 'data3':14, 'data4':12, 'data5':7},
 *                     {'name':'metric four', 'data1':2, 'data2':14, 'data3':6, 'data4':1, 'data5':23},
 *                     {'name':'metric five', 'data1':27, 'data2':38, 'data3':36, 'data4':13, 'data5':33}
 *                 ]
 *             },
 *             series: {
 *                 type: 'radar',
 *                 xField: 'name',
 *                 yField: 'data4',
 *                 style: {
 *                     fillStyle: 'rgba(0, 0, 255, 0.1)',
 *                     strokeStyle: 'rgba(0, 0, 0, 0.8)',
 *                     lineWidth: 1
 *                 }
 *             },
 *             axes: [{
 *                 type: 'numeric',
 *                 position: 'radial',
 *                 fields: 'data4',
 *                 style: {
 *                     estStepSize: 10
 *                 },
 *                 grid: true
 *             }, {
 *                 type: 'category',
 *                 position: 'angular',
 *                 fields: 'name',
 *                 style: {
 *                     estStepSize: 1
 *                 },
 *                 grid: true
 *             }]
 *         }
 *     });
 *
 */
Ext.define('Ext.chart.series.Radar', {
    extend: 'Ext.chart.series.Polar',
    type: 'radar',
    seriesType: 'radar',
    alias: 'series.radar',
    requires: ['Ext.chart.series.sprite.Radar'],
    /**
     * @cfg {Object} style
     * An object containing styles for overriding series styles from theming.
     */

    config: {

    },

    themeColorCount: function() {
        return 1;
    },

    themeMarkerCount: function() {
        return 1;
    },

    updateAngularAxis: function (axis) {
        axis.processData(this);
    },

    updateRadialAxis: function (axis) {
        axis.processData(this);
    },

    coordinateX: function () {
        return this.coordinate('X', 0, 2);
    },

    coordinateY: function () {
        return this.coordinate('Y', 1, 2);
    },

    updateCenter: function (center) {
        this.setStyle({
            translationX: center[0] + this.getOffsetX(),
            translationY: center[1] + this.getOffsetY()
        });
        this.doUpdateStyles();
    },

    updateRadius: function (radius) {
        this.setStyle({
            endRho: radius
        });
        this.doUpdateStyles();
    },

    updateRotation: function (rotation) {
        this.setStyle({
            rotationRads: rotation
        });
        this.doUpdateStyles();
    },

    updateTotalAngle: function (totalAngle) {
        this.processData();
    },

    getItemForPoint: function (x, y) {
        var me = this,
            sprite = me.sprites && me.sprites[0],
            attr = sprite.attr,
            dataX = attr.dataX,
            length = dataX.length,
            store = me.getStore(),
            marker = me.getMarker(),
            threshhold, item, xy, i, bbox, markers;

        if (me.getHidden()) {
            return null;
        }
        if (sprite && marker) {
            markers = sprite.getBoundMarker('markers')[0];
            for (i = 0; i < length; i++) {
                bbox = markers.getBBoxFor(i);
                threshhold = (bbox.width + bbox.height) * .25;
                xy = sprite.getDataPointXY(i);
                if (Math.abs(xy[0] - x) < threshhold &&
                    Math.abs(xy[1] - y) < threshhold) {
                    item = {
                        series: me,
                        sprite: sprite,
                        index: i,
                        category: 'markers',
                        record: store.getData().items[i],
                        field: me.getYField()
                    };
                    return item;
                }
            }
        }
        return me.callParent(arguments);
    },

    getDefaultSpriteConfig: function () {
        var config = this.callParent(),
            fx = {
                customDurations: {
                    translationX: 0,
                    translationY: 0,
                    rotationRads: 0,
                    // Prevent animation of 'dataMinX' and 'dataMaxX' attributes in order
                    // to react instantaniously to changes to the 'hidden' attribute.
                    dataMinX: 0,
                    dataMaxX: 0
                }
            };
        if (config.fx) {
            Ext.apply(config.fx, fx);
        } else {
            config.fx = fx;
        }
        return config;
    },

    getSprites: function () {
        var me = this,
            chart = me.getChart(),
            animation = me.getAnimation() || chart && chart.getAnimation(),
            sprite = me.sprites[0],
            markers;
        
        if (!chart) {
            return [];
        }
        if (!sprite) {
            sprite = me.createSprite();
        }
        if (animation) {
            markers = sprite.getBoundMarker('markers');
            if (markers) {
                markers = markers[0];
                markers.getTemplate().fx.setConfig(animation);
            }
            sprite.fx.setConfig(animation);
        }
        return me.sprites;
    },

    provideLegendInfo: function (target) {
        var me = this,
            style = me.getSubStyleWithTheme(),
            fill = style.fillStyle;

        if (Ext.isArray(fill)) {
            fill = fill[0];
        }
        target.push({
            name: me.getTitle() || me.getYField() || me.getId(),
            mark: (Ext.isObject(fill) ? fill.stops && fill.stops[0].color : fill) || style.strokeStyle || 'black',
            disabled: me.getHidden(),
            series: me.getId(),
            index: 0
        });
    }

});

