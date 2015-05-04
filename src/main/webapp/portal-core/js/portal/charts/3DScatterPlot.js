/**
 * Panel extension for graphing points with a 1D attribute in 3D space
 *
 * 3D display is dependent on Three JS. Point scaling requires D3
 */
Ext.define('portal.charts.3DScatterPlot', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.threedscatterplot',

    data : null,
    d3 : null, //D3 elements (graphs, lines etc).
    threeJs : null, //Three JS elements
    innerId: null, //Internal ID for rendering three js
    useCanvasRenderer: false, 


    /**
     * Adds the following config
     * {
     *   data - Object[] - Optional - Data to intially plot in this widget. No data will be plotted if this is missing.
     *   pointSize - Number - Optional - Size of the data points in pixels (Unscaled each axis is 50 pixels wide) - Default - 10
     *   allowSelection - Booelan - Optional - True if points can be selected by clicking with the mouse. Default - false
     *
     *   xAttr - String - Optional - The name of the attribute to plot on the x axis (default - 'x')
     *   xLabel - String - Optional - The name of the x axis to show on the plot (default - 'X')
     *   xDomain - Number[] - Optional - The fixed range of values [min, max] to plot on the x axis. Defaults to data extents
     *   xHideValueLabel - Boolean - Optional - If set, hide the numerical min/max values displayed on the x axis (default - false)
     *
     *   yAttr - String - Optional - The name of the attribute to plot on the y axis (default - 'y')
     *   yLabel - String - Optional - The name of the y axis to show on the plot (default - 'Y')
     *   yDomain - Number[] - Optional - The fixed range of values [min, max] to plot on the y axis. Defaults to data extents
     *   yHideValueLabel - Boolean - Optional - If set, hide the numerical min/max values displayed on the y axis (default - false)
     *
     *   zAttr - String - Optional - The name of the attribute to plot on the z axis (default - 'z')
     *   zLabel - String - Optional - The name of the z axis to show on the plot (default - 'Z')
     *   zDomain - Number[] - Optional - The fixed range of values [min, max] to plot on the z axis. Defaults to data extents
     *   zHideValueLabel - Boolean - Optional - If set, hide the numerical min/max values displayed on the z axis (default - false)
     *
     *   valueAttr - String - Optional - The name of the attribute that controls the color value (default - 'value')
     *   valueLabel - String - Optional - The label of the attribute that controls the color value (default - 'Value')
     *   valueDomain - Number[] - Optional - The fixed range of values [min, max] to control color scale. Defaults to data extents
     *   valueScale - String - Optional - (not compatible with valueRenderer) How will the color scale be defined for the default rainbow plot - choose from ['linear', 'log'] (default - 'linear')
     *   valueRenderer - function(value) - Optional - (not compatible with valueScale) Given a value, return a 16 bit integer representing an RGB value in the form 0xffffff
     * }
     *
     * Adds the following events
     * {
     *  select : function(this, dataItem) - Fired when a scatter point is clicked
     *  deselect : function(this) - Fired when a scatter point deselected
     * }
     *
     */
    constructor : function(config) {
        this.d3 = null;
        this.threeJs = null;
        this.innerId = Ext.id();
        this.data = config.data ? config.data : null;
        this.pointSize = config.pointSize ? config.pointSize : 10;
        this.allowSelection = config.allowSelection ? true : false;

        this.xAttr = config.xAttr ? config.xAttr : 'x';
        this.xLabel = config.xLabel ? config.xLabel : 'X';
        this.xDomain = config.xDomain ? config.xDomain : null;
        this.xHideValueLabel = config.xHideValueLabel ? true : false;
        this.yAttr = config.yAttr ? config.yAttr : 'y';
        this.yLabel = config.yLabel ? config.yLabel : 'Y';
        this.yDomain = config.yDomain ? config.yDomain : null;
        this.yHideValueLabel = config.xHideValueLabel ? true : false;
        this.zAttr = config.zAttr ? config.zAttr : 'z';
        this.zLabel = config.zLabel ? config.zLabel : 'Z';
        this.zDomain = config.zDomain ? config.zDomain : null;
        this.zHideValueLabel = config.xHideValueLabel ? true : false;
        this.valueAttr = config.valueAttr ? config.valueAttr : 'value';
        this.valueLabel = config.valueLabel ? config.valueLabel : 'Value';
        this.valueDomain = config.valueDomain ? config.valueDomain : null;
        this.valueScale = config.valueScale ? config.valueScale : 'linear';
        this.valueRenderer = config.valueRenderer ? config.valueRenderer : null;

        Ext.apply(config, {
            html : Ext.util.Format.format('<div id="{0}" style="width:100%;height:100%;"></div>', this.innerId)
        });

        this.callParent(arguments);

        //Lower IE version require a different renderer entirely
        //Force the loading of these new dependencies if we need to...
        if (Ext.isIE10m) {
            this.useCanvasRenderer = true;
            this.loadingDeps = false;
            this.loadingDepsCallback = null;
            
            if (!THREE.CanvasRenderer) {
                this.loadingDeps = true;
                Ext.Loader.loadScript({
                    url:'portal-core/js/threejs/renderers/CanvasRenderer.js',
                    scope: this,
                    onLoad:function() {
                        Ext.Loader.loadScript({
                            url:'portal-core/js/threejs/renderers/Projector.js',
                            scope: this,
                            onLoad:function() {
                                this.loadingDeps = false;
                                if (Ext.isFunction(this.loadingDepsCallback)) {
                                    this.loadingDepsCallback();
                                } else {
                                }
                            }
                        });
                    } 
                });
            }
        }

        this.on('render', this._afterRender, this);
        this.on('resize', this._onResize, this);
    },

    /**
     * Initialise three JS elements.
     */
    _afterRender : function() {
        if (this.loadingDeps) {
            this.loadingDepsCallback = this._afterRender;
            return;
        }
        
        this.threeJs = {
            camera : null,
            controls : null,
            scene : null,
            renderer : null,
            width : null,
            height : null
        };

        this.threeJs.scene = new THREE.Scene();

        var el = this.getEl();
        this.threeJs.width = el.getWidth();
        this.threeJs.height = el.getHeight();

        if (this.allowSelection) {
            el.on('mousedown', this._handleMouseDown, this);
            el.on('mouseup', this._handleMouseUp, this);

            this.threeJs.raycaster = new THREE.Raycaster();
            this.threeJs.raycaster.params.PointCloud.threshold = this.pointSize / 3;
        }

        var container = document.getElementById(this.innerId);
        
        this.threeJs.camera = new THREE.PerspectiveCamera(60, this.threeJs.width / this.threeJs.height, 1, 10000);
        this.threeJs.camera.position.z = 180;
        this.threeJs.camera.position.y = 18;
        this.threeJs.scene.add(this.threeJs.camera);

        this.threeJs.controls = new THREE.OrbitControls( this.threeJs.camera, container);
        this.threeJs.controls.damping = 0.2;
        this.threeJs.controls.target = new THREE.Vector3(0, 0, 0);
        this.threeJs.controls.addEventListener('change', Ext.bind(this._renderThreeJs, this));

        // renderer
        if (Ext.isIE10m) { 
            //IE 10 and lower don't have WebGL support
            this.threeJs.renderer = new THREE.CanvasRenderer({antialias : false});
        } else {
            this.threeJs.renderer = new THREE.WebGLRenderer({antialias : false});
        }
        this.threeJs.renderer.setClearColor(0xffffff, 1);
        this.threeJs.renderer.setSize(this.threeJs.width, this.threeJs.height);

        container.appendChild(this.threeJs.renderer.domElement);

        // Need a perpetual animation loop for updating the user controls
        var me = this;
        var animate = function() {
            requestAnimationFrame(animate);
            me.threeJs.controls.update();
        };
        animate();

        if (this.data) {
            this.plot(this.data);
        }

        this._renderThreeJs();
    },

    /**
     * Renders the current state of the three JS camera/scene
     */
    _renderThreeJs : function() {
        this.threeJs.renderer.render(this.threeJs.scene, this.threeJs.camera);
    },

    /**
     * Update camera aspect ratio and renderer size
     */
    _onResize : function(me, width, height) {
        if (!this.threeJs) {
            return;
        }

        var el = this.getEl();
        this.threeJs.width = el.getWidth();
        this.threeJs.height = el.getHeight();
        this.threeJs.camera.aspect = this.threeJs.width / this.threeJs.height;
        this.threeJs.camera.updateProjectionMatrix();

        this.threeJs.renderer.setSize(this.threeJs.width, this.threeJs.height);

        this._renderThreeJs();
    },

    /**
     * Utility for turning a click event on dom element target
     * into an X/Y offset relative to that element
     *
     * From:
     * http://stackoverflow.com/questions/55677/how-do-i-get-the-coordinates-of-a-mouse-click-on-a-canvas-element
     */
    _relMouseCoords : function(event, target) {
        var totalOffsetX = 0;
        var totalOffsetY = 0;
        var canvasX = 0;
        var canvasY = 0;
        var currentElement = target;

        do {
            totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
            totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
        } while (currentElement = currentElement.offsetParent)

        canvasX = event.pageX - totalOffsetX;
        canvasY = event.pageY - totalOffsetY;

        return {
            x : canvasX,
            y : canvasY
        };
    },

    _handleMouseDown : function(e, t) {
        this._md = this._relMouseCoords(e.browserEvent, t);
    },

    _handleMouseUp : function(e, t) {
        var xy = this._relMouseCoords(e.browserEvent, t);
        var rawX = xy.x;
        var rawY = xy.y;

        // If the mouse has moved too far, dont count this as a click
        if (Math.abs(this._md.x - rawX) + Math.abs(this._md.y - rawY) > 10) {
            return;
        }

        // The X/Y needs to be scale independent
        var x = ( rawX / this.threeJs.width ) * 2 - 1;
        var y = - ( rawY / this.threeJs.height ) * 2 + 1;

        // Otherwise cast a ray and see what we intersect
        var mouse3D = new THREE.Vector3(x, y, 0.5).unproject(this.threeJs.camera);
        var direction = mouse3D.clone()
            .sub(this.threeJs.camera.position)
            .normalize();

        this.threeJs.raycaster.ray.set(this.threeJs.camera.position, direction);
        var intersections = this.threeJs.raycaster.intersectObject(this.threeJs.pointCloud);

        if (intersections.length > 0) {
            this._handlePointSelect(intersections[0].index, intersections[0].point);
        } else {
            this._clearPointSelect();
        }
    },

    _handlePointSelect : function(index, point) {
        var dataItem = this.data[index];
        var color = this.threeJs.pointCloud.geometry.colors[index];

        if (!this.threeJs.selectionMesh) {
            var selectionBox = new THREE.SphereGeometry(this.pointSize * 0.8, 8, 8);
            var selectionMaterial = new THREE.MeshBasicMaterial( { color: color, opacity: 1.0, transparent: false } );
            this.threeJs.selectionMesh = new THREE.Mesh( selectionBox, selectionMaterial );
        } else {
            this.threeJs.selectionMesh.material.color = color;
        }

        this.threeJs.selectionMesh.position.set(
                this.d3.xScale(dataItem[this.xAttr]),
                this.d3.yScale(dataItem[this.yAttr]),
                this.d3.zScale(dataItem[this.zAttr]));
        this.threeJs.scene.add(this.threeJs.selectionMesh);
        this._renderThreeJs();
        this.fireEvent('select', this, dataItem);
    },

    _clearPointSelect : function() {
        if (this.threeJs.selectionMesh) {
            this.threeJs.scene.remove(this.threeJs.selectionMesh);
            this.threeJs.selectionMesh = null;
            this._renderThreeJs();
        }

        this.fireEvent('deselect', this);
    },

    /**
     * Clear the entire contents of the scatter plot
     */
    clearPlot : function() {
        if (!this.threeJs) {
            return;
        }

        for (var i = this.threeJs.scene.children.length - 1; i >= 0; i--) {
            this.threeJs.scene.remove(this.threeJs.scene.children[i]);
        }
        this.d3 = {};
        this.data = null;
    },

    /**
     * Update the scatter plot with the specified data
     *
     * Adapted from http://bl.ocks.org/phil-pedruco/9852362
     *
     * @param data Object[] of objects containing x,y,z attributes and a "plot" attribute
     */
    plot : function(data) {
        var me = this;

        function v(x, y, z) {
            return new THREE.Vector3(x, y, z);
        }

        function createTextCanvas(text, color, font, size) {
            size = size || 16;
            var canvas = document.createElement('canvas');
            var ctx = canvas.getContext('2d');
            var fontStr = (size + 'px ') + (font || 'Arial');
            ctx.font = fontStr;
            var w = ctx.measureText(text).width;
            var h = Math.ceil(size);
            canvas.width = w;
            canvas.height = h;
            ctx.font = fontStr;
            ctx.fillStyle = color || 'black';
            ctx.fillText(text, 0, Math.ceil(size * 0.8));
            return canvas;
        }

        function createText2D(text, color, font, size, segW,
                segH) {
            var canvas = createTextCanvas(text, color, font, size);
            var plane = new THREE.PlaneGeometry(canvas.width, canvas.height, segW, segH);
            var tex = new THREE.Texture(canvas);
            tex.needsUpdate = true;
            var planeMat = new THREE.MeshBasicMaterial({
                map : tex,
                color : 0xffffff,
                transparent : true
            });

            // This is how we view the reversed text from behind
            // see:
            // http://stackoverflow.com/questions/20406729/three-js-double-sided-plane-one-side-reversed
            var backPlane = plane.clone();
            plane.merge(backPlane, new THREE.Matrix4().makeRotationY(Math.PI), 1);

            var mesh = new THREE.Mesh(plane, planeMat);
            mesh.scale.set(0.5, 0.5, 0.5);
            mesh.material.side = THREE.FrontSide;
            return mesh;
        }

        this.clearPlot();
        this.data = data;

        this.d3.xExtent = this.xDomain ? this.xDomain : d3.extent(data, function(d) {return d[me.xAttr];});
        this.d3.yExtent = this.yDomain ? this.yDomain : d3.extent(data, function(d) {return d[me.yAttr];});
        this.d3.zExtent = this.zDomain ? this.zDomain : d3.extent(data, function(d) {return d[me.zAttr];});
        this.d3.valueExtent = this.valueDomain ? this.valueDomain : d3.extent(data, function(d) {return d[me.valueAttr];});

        var format = d3.format("+.3f");
        var vpts = {
            xMax : this.d3.xExtent[1],
            xCen : (this.d3.xExtent[1] + this.d3.xExtent[0]) / 2,
            xMin : this.d3.xExtent[0],
            yMax : this.d3.yExtent[1],
            yCen : (this.d3.yExtent[1] + this.d3.yExtent[0]) / 2,
            yMin : this.d3.yExtent[0],
            zMax : this.d3.zExtent[1],
            zCen : (this.d3.zExtent[1] + this.d3.zExtent[0]) / 2,
            zMin : this.d3.zExtent[0]
        };

        var xScale, yScale, zScale, valueScale;

        xScale = this.d3.xScale = d3.scale.linear()
            .domain(this.d3.xExtent)
            .range([ -50, 50 ]);
        yScale = this.d3.yScale = d3.scale.linear()
            .domain(this.d3.yExtent)
            .range([ -50, 50 ]);
        zScale = this.d3.zScale = d3.scale.linear()
            .domain(this.d3.zExtent)
            .range([ -50, 50 ]);

        if (this.valueScale === 'linear') {
            valueScale = this.d3.valueScale = d3.scale.linear()
        } else if (this.valueScale === 'log') {
            valueScale = this.d3.valueScale = d3.scale.log()
        } else {
            throw 'Invalid valueScale: ' + this.valueScale;
        }
        valueScale.domain(this.d3.valueExtent).range([ 0, 1]);

        // Build our axes
        var lineGeo = new THREE.Geometry();
        lineGeo.vertices.push(
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMin)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zCen)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zCen)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zCen)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zCen)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zCen)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMax)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMin), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMin)),
            v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yMin), zScale(vpts.zMax)),

            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMax)), v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMin)),
            v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zMin)), v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zMax)),
            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zMax)),

            v(xScale(vpts.xCen), yScale(vpts.yCen), zScale(vpts.zMax)), v(xScale(vpts.xCen), yScale(vpts.yCen), zScale(vpts.zMin)),
            v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zMin)), v(xScale(vpts.xCen), yScale(vpts.yMin), zScale(vpts.zCen)),
            v(xScale(vpts.xCen), yScale(vpts.yMax), zScale(vpts.zCen)), v(xScale(vpts.xMax), yScale(vpts.yMax), zScale(vpts.zCen)),
            v(xScale(vpts.xMax), yScale(vpts.yCen), zScale(vpts.zCen)), v(xScale(vpts.xMin), yScale(vpts.yCen), zScale(vpts.zCen))
        );
        var lineMat = new THREE.LineBasicMaterial({
            color : 0x000000,
            lineWidth : 1
        });
        var line = new THREE.Line(lineGeo, lineMat);
        line.type = THREE.Lines;
        this.threeJs.scene.add(line);

        var titleX = createText2D('-' + this.xLabel);
        titleX.position.x = xScale(vpts.xMin) - (this.xHideValueLabel ? 12 : 12);
        titleX.position.y = 5;
        this.threeJs.scene.add(titleX);

        if (!this.xHideValueLabel) {
            var valueX = createText2D(format(this.d3.xExtent[0]));
            valueX.position.x = xScale(vpts.xMin) - 12;
            valueX.position.y = -5;
            this.threeJs.scene.add(valueX);
        }

        var titleX = createText2D((this.xHideValueLabel ? '+' : '') + this.xLabel);
        titleX.position.x = xScale(vpts.xMax) + (this.xHideValueLabel ? 12 : 12);
        titleX.position.y = 5;
        this.threeJs.scene.add(titleX);

        if (!this.xHideValueLabel) {
            var valueX = createText2D(format(this.d3.xExtent[1]));
            valueX.position.x = xScale(vpts.xMax) + 12;
            valueX.position.y = -5;
            this.threeJs.scene.add(valueX);
        }

        var titleY = createText2D('-' + this.yLabel);
        titleY.position.y = yScale(vpts.yMin) - (this.yHideValueLabel ? 5 : 5);
        this.threeJs.scene.add(titleY);

        if (!this.yHideValueLabel) {
            var valueY = createText2D(format(this.d3.yExtent[0]));
            valueY.position.y = yScale(vpts.yMin) - 15;
            this.threeJs.scene.add(valueY);
        }

        var titleY = createText2D((this.yHideValueLabel ? '+' : '') + this.yLabel);
        titleY.position.y = yScale(vpts.yMax) + (this.yHideValueLabel ? 7 : 15);
        this.threeJs.scene.add(titleY);

        if (!this.yHideValueLabel) {
            var valueY = createText2D(format(this.d3.yExtent[1]));
            valueY.position.y = yScale(vpts.yMax) + 5;
            this.threeJs.scene.add(valueY);
        }

        var titleZ = createText2D('-' + this.zLabel + (this.zHideValueLabel ? '' : ' ' + format(this.d3.zExtent[0])));
        titleZ.position.z = zScale(vpts.zMin) + 2;
        this.threeJs.scene.add(titleZ);

        var titleZ = createText2D((this.zHideValueLabel ? '+' : '') + this.zLabel + ' ' + (this.zHideValueLabel ? '' : ' ' + format(this.d3.zExtent[1])));
        titleZ.position.z = zScale(vpts.zMax) + 2;
        this.threeJs.scene.add(titleZ);

        //THREEJS currently does not support canvas renderer with PointCloud's. The vertices will not render
        //Rather than workaround this - we just print a warning message.
        if (this.useCanvasRenderer) {
            var warningText = createText2D('IE 10 and below currently not supported...', '#ff0000');
            warningText.position.z = 60;
            warningText.position.y = 20;
            this.threeJs.scene.add(warningText);
        }
        
        // Build our scatter plot points
        var mat = new THREE.PointCloudMaterial({
            vertexColors : true,
            size : this.pointSize
        });

        var pointCount = data.length;
        var pointGeo = new THREE.Geometry();
        for (var i = 0; i < pointCount; i++) {
            var x = xScale(data[i][this.xAttr]);
            var y = yScale(data[i][this.yAttr]);
            var z = zScale(data[i][this.zAttr]);
            var rawValue = data[i][this.valueAttr];
            var color;

            if (this.valueRenderer) {
                color = new THREE.Color(this.valueRenderer(rawValue));
            } else {
                var scaledValue = valueScale(rawValue); //Scale to HSL rainbow from 0 - 240
                var hue = (1 - scaledValue) * 180 / 255;
                color = new THREE.Color().setHSL(hue, 1.0, 0.5);
            }

            pointGeo.vertices.push(v(x, y, z));
            pointGeo.colors.push(color);
        }

        this.threeJs.pointCloud = new THREE.PointCloud(pointGeo, mat);
        this.threeJs.scene.add(this.threeJs.pointCloud);

        this._renderThreeJs();
    }
});