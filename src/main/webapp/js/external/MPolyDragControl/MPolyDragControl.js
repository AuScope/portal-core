    var styleStr = '';
    styleStr += '<style>';
    styleStr += '.MDR_labelStyle {background: #000; font: bold 10px verdana; color: #FFF; text-align: left; padding: 2px;}';
    styleStr += '</style>';
    document.write(styleStr);


MPolyDragControl = function(MOptions) {
    MOptions = MOptions ? MOptions : {};
    this.type = MOptions.type ? MOptions.type : 'rectangle';
    this.map = MOptions.map ? MOptions.map : null;
    this.labelText = MOptions.labelText ? MOptions.labelText : '';
    this.ondragend = MOptions.ondragend ? MOptions.ondragend : null;
    this.transMarkerEnabled = false;

    this.unitDivisor = 2589988.11;
    this.initialize();
};

MPolyDragControl.prototype.initialize = function() {
    this.self = this;
    this.polyInitialized = false;
    this.bounds = null;

    this.radius;
    this.circleCenter;

    this.dragMarker0;
    this.dragMarker1;

    var baseIcon = new GIcon();
    baseIcon.iconSize = new GSize(11,11);
    baseIcon.iconAnchor = new GPoint(6,6);
    baseIcon.infoWindowAnchor = new GPoint(1,1);
    baseIcon.dragCrossSize = new GSize(0,0);
    baseIcon.maxHeight = 0.1;

    this.dragImage = "js/external/MPolyDragControl/images/polyEditSquare.png";
    this.transImage = "js/external/MPolyDragControl/images/transparent.png";
    this.polyEditIcon = (new GIcon(baseIcon, this.dragImage));
    this.transIcon = (new GIcon(baseIcon, this.transImage));

    this.floatingLabel = new ELabel(this.map.getCenter(), 'Label text', 'MDR_labelStyle',new GSize(5,16));
    this.floatingLabel.hide();
    this.map.addOverlay(this.floatingLabel);

    this.addMarkers();
};

MPolyDragControl.prototype.addMarkers = function() {
    var self = this.self;

    this.dragMarker0 = new GMarker(this.map.getCenter(),{icon:this.polyEditIcon,draggable:true,bouncy:false,dragCrossMove:true});
    this.map.addOverlay(this.dragMarker0);

    this.mdListener = GEvent.addListener(this.dragMarker0,'mousedown',function(){self.markerMouseDown()});
    GEvent.addListener(this.dragMarker0,'dragstart',function(){self.dragStart(this)});
    GEvent.addListener(this.dragMarker0,'drag',function(){self.drag(this)});
    GEvent.addListener(this.dragMarker0,'dragend',function(){self.dragEnd(this)});

    this.dragMarker1 = new GMarker(this.map.getCenter(),{icon:this.polyEditIcon,draggable:true,bouncy:false,dragCrossMove:true});
    this.map.addOverlay(this.dragMarker1);
    GEvent.addListener(self.dragMarker1,'dragstart',function(){self.dragStart(this)});
    GEvent.addListener(self.dragMarker1,'drag',function(){self.drag(this)});
    GEvent.addListener(self.dragMarker1,'dragend',function(){self.dragEnd(this)});
    this.dragMarker0.hide();
    this.dragMarker1.hide();
};


MPolyDragControl.prototype.markerMouseDown = function() {
    var self = this.self;
    this.dragMarker0.setImage(this.dragImage);
    this.dragMarker1.setLatLng(this.dragMarker0.getLatLng());
    this.dragMarker1.show();
    GEvent.removeListener(this.mdListener);
};

MPolyDragControl.prototype.mapClick = function(latlon) {
    var self = this.self;
    this.poly = new GPolygon([latlon,latlon,latlon,latlon,latlon],'#0000ff',1,1,'#0000ff',0.3);
    this.map.addOverlay(this.poly);

    GEvent.trigger(self.dragMarker1,'dragstart');
};

MPolyDragControl.prototype.enableTransMarker = function() {
    var self = this.self;
    this.dragMarker0.setImage(this.transImage);
    this.dragMarker0.show();
    this.transMarkerEnabled = true;
    this.movelistener = GEvent.addListener(this.map,'mousemove',function(latlon){
        self.dragMarker0.setLatLng(latlon);
    });
};

MPolyDragControl.prototype.disableTransMarker = function() {
    this.transMarkerEnabled = false;
    GEvent.removeListener(this.movelistener);
    GEvent.removeListener(this.mdListener);
};

MPolyDragControl.prototype.reset = function() {
    var self = this.self;
    this.hide();

    this.bounds = null;
    this.mdListener = GEvent.addListener(this.dragMarker0,'mousedown',function(){self.markerMouseDown()});

};

MPolyDragControl.prototype.dragStart = function(marker) {
    var self = this.self;
};

MPolyDragControl.prototype.drag = function() {
    var self = this.self;

    if (self.type == 'circle') {
        self.updateCircle();
    }
    else if (self.type == 'rectangle') {
        self.updateRectangle();
    }
};

MPolyDragControl.prototype.dragEnd = function() {
    var self = this.self;
    if (typeof self.ondragend == 'function') {
        self.ondragend();
    }
    self.disableTransMarker();
};

MPolyDragControl.prototype.updateRectangle = function() {
    var self = this.self;
    var latlon0 = self.dragMarker0.getLatLng();
    var latlon1 = self.dragMarker1.getLatLng();

    self.bounds = null;
    self.bounds = new GLatLngBounds();

    if (latlon0.lat() <= latlon1.lat() && latlon0.lng() <= latlon1.lng()) {
        var p1 = latlon0; // SW
        var p2 = latlon1; // NE
    }
    else if (latlon0.lat() <= latlon1.lat() && latlon0.lng() >= latlon1.lng()) {
        var p1 = latlon0; // SE
        var p2 = latlon1; // NW
    }
    else if (latlon0.lat() >= latlon1.lat() && latlon0.lng() >= latlon1.lng()) {
        var p1 = latlon0; // NE
        var p2 = latlon1; // SW
    }
    else if (latlon0.lat() >= latlon1.lat() && latlon0.lng() <= latlon1.lng()) {
        var p1 = latlon0; // NW
        var p2 = latlon1; // SE
    }

    self.bounds.extend(p1);
    self.bounds.extend(p2);

    var p1 = this.bounds.getSouthWest();
    var p2 = new GLatLng(this.bounds.getNorthEast().lat(),this.bounds.getSouthWest().lng());
    var p3 = this.bounds.getNorthEast();
    var p4 = new GLatLng(this.bounds.getSouthWest().lat(),this.bounds.getNorthEast().lng());
    var points = Array(p1,p2,p3,p4,p1);

    self.drawPoly(points);

};

MPolyDragControl.prototype.drawRectangle = function(neLat, neLng, swLat, swLng) {
    this.enableTransMarker();
    var self = this.self;
    var latlon0 = new GLatLng(neLat, neLng);
    var latlon1 = new GLatLng(swLat, swLng);

    self.bounds = null;
    self.bounds = new GLatLngBounds();

    if (latlon0.lat() <= latlon1.lat() && latlon0.lng() <= latlon1.lng()) {
        var p1 = latlon0; // SW
        var p2 = latlon1; // NE
    }
    else if (latlon0.lat() <= latlon1.lat() && latlon0.lng() >= latlon1.lng()) {
        var p1 = latlon0; // SE
        var p2 = latlon1; // NW
    }
    else if (latlon0.lat() >= latlon1.lat() && latlon0.lng() >= latlon1.lng()) {
        var p1 = latlon0; // NE
        var p2 = latlon1; // SW
    }
    else if (latlon0.lat() >= latlon1.lat() && latlon0.lng() <= latlon1.lng()) {
        var p1 = latlon0; // NW
        var p2 = latlon1; // SE
    }

    self.bounds.extend(p1);
    self.bounds.extend(p2);

    var p1 = this.bounds.getSouthWest();
    var p2 = new GLatLng(this.bounds.getNorthEast().lat(),this.bounds.getSouthWest().lng());
    var p3 = this.bounds.getNorthEast();
    var p4 = new GLatLng(this.bounds.getSouthWest().lat(),this.bounds.getNorthEast().lng());
    var points = Array(p1,p2,p3,p4,p1);

    self.drawPoly(points);

    // set drag marker positions
    this.dragMarker0.setLatLng(p4);
    this.dragMarker0.show();
    this.dragMarker0.setImage(this.dragImage);
    this.dragMarker1.setLatLng(p2);
    this.dragMarker1.show();

    // set label position
    this.floatingLabel.setPoint(p2);
    this.disableTransMarker();
};

MPolyDragControl.prototype.updateCircle = function() {

    this.circleCenter = this.dragMarker0.getLatLng();
    var points = Array();
    this.radius = this.dragMarker0.getLatLng().distanceFrom(this.dragMarker1.getLatLng()); // meters

    with (Math) {
        var d = this.radius/6378800;	// circle radius / meters of Earth radius = radians
        var lat1 = (PI/180)* this.circleCenter.lat(); // radians
        var lng1 = (PI/180)* this.circleCenter.lng(); // radians

        for (var a = 0 ; a < 361 ; a+=10 ) {
            var tc = (PI/180)*a;
            var y = asin(sin(lat1)*cos(d)+cos(lat1)*sin(d)*cos(tc));
            var dlng = atan2(sin(tc)*sin(d)*cos(lat1),cos(d)-sin(lat1)*sin(y));
            var x = ((lng1-dlng+PI) % (2*PI)) - PI ;
            var point = new GLatLng(parseFloat(y*(180/PI)),parseFloat(x*(180/PI)));
            points.push(point);
        }
    }

    this.drawPoly(points);
};

MPolyDragControl.prototype.drawPoly = function(points) {
    if (this.poly) {
        this.map.removeOverlay(this.poly);
        this.poly = null;
    }
    this.poly = new GPolygon(points,'#000',1,1,'#000',0.2);
    this.map.addOverlay(this.poly);

    var html = '';
    if (this.type == 'circle') {
        html += 'Center:&nbsp;' + this.circleCenter.lat().toFixed(5) + ',' + this.circleCenter.lng().toFixed(5) + '<br>';
        html += 'Radius:&nbsp;' + (this.radius / 1609).toFixed(2) + '&nbsp;mi.<br>';
    }
    else {
        html += 'Lat:&nbsp;' + this.bounds.getSouthWest().lat().toFixed(5) + '&nbsp;to&nbsp;' + this.bounds.getNorthEast().lat().toFixed(5) + '<br>';
        html += 'Lon:&nbsp;' + this.bounds.getSouthWest().lng().toFixed(5) + '&nbsp;to&nbsp;' + this.bounds.getNorthEast().lng().toFixed(5) + '<br>';
    }
    html += 'Area:&nbsp;' + (this.poly.getArea()/ this.unitDivisor).toFixed(2) + '&nbsp;sq.mi.';


    this.floatingLabel.setContents(this.labelText);
    this.floatingLabel.setPoint(this.dragMarker1.getLatLng());
    this.floatingLabel.show();
};

MPolyDragControl.prototype.getParams = function() {
    var str = '';
    if (this.type == 'circle') {
        str += 'centerLat=' + this.circleCenter.lat().toFixed(5) + '&centerLon=' + this.circleCenter.lng().toFixed(5);
        str += '&radius=' + (this.radius / 1609).toFixed(2);
    }
    else if (this.bounds == null) {
        return null;
    }
    else {
        //str += 'lat1=' + this.bounds.getSouthWest().lat().toFixed(5) + '&lat2=' + this.bounds.getNorthEast().lat().toFixed(5);
        //str += '&lon1=' + this.bounds.getSouthWest().lng().toFixed(5) + '&lon2=' + this.bounds.getNorthEast().lng().toFixed(5);

        str += this.bounds.getSouthWest().lng().toFixed(5) + ',' + this.bounds.getSouthWest().lat().toFixed(5) + ',';
        str += this.bounds.getNorthEast().lng().toFixed(5) + ',' + this.bounds.getNorthEast().lat().toFixed(5);
    }
    return str;
};

MPolyDragControl.prototype.getSouthWestLat = function() {
    return this.bounds.getSouthWest().lat();
};

MPolyDragControl.prototype.getSouthWestLng = function() {
    return this.bounds.getSouthWest().lng();
};

MPolyDragControl.prototype.getNorthEastLat = function() {
    return this.bounds.getNorthEast().lat();
};

MPolyDragControl.prototype.getNorthEastLng = function() {
    return this.bounds.getNorthEast().lng();
};

MPolyDragControl.prototype.setType = function(type) {
    this.type = type;
    if (this.poly) {
        this.drag();
        this.dragEnd();
    }
};

MPolyDragControl.prototype.show = function() {
    if (this.poly) {
        this.poly.show();
    }

    if (this.dragMarker0) {
        this.dragMarker0.show();
    }
    if (this.dragMarker1) {
        this.dragMarker1.show();
    }
    if (this.floatingLabel) {
        this.floatingLabel.show();
    }
};

MPolyDragControl.prototype.hide = function() {
    if (this.poly) {
        this.poly.hide();
    }

    if (this.dragMarker0) {
        this.dragMarker0.hide();
    }
    if (this.dragMarker1) {
        this.dragMarker1.hide();
    }
    if (this.floatingLabel) {
        this.floatingLabel.hide();
    }
};

MPolyDragControl.prototype.isVisible = function() {
    return !this.poly.isHidden();
};

MPolyDragControl.prototype.getOverlays = function() {
    return [this.poly,this.dragMarker0,this.dragMarker1,this.floatingLabel];
};
