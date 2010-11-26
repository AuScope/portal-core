/*
 * Represents Google Map tile that encloses iLatLng point coordinates
 * 
 * Notice that you cannot modify the coordinates of a Tile. If you 
 * want to compute another point, you have to create a new one. 
 */
Tile = function( iMap,iLatLng) {
   
   // The GM tiles are assumed to be quadratic. 
   // All tiles have the same tile size.
   this.tileSize = iMap.getCurrentMapType().getTileSize();
   this.width = this.tileSize;
   this.height = this.tileSize;
   
   this.map = iMap;
   this.zoom = iMap.getZoom();
   this.latlng = iLatLng;
   this.tileCoordinates = null;
   this.currentProjection = iMap.getCurrentMapType().getProjection();
   
   var tile = new GPoint();
   var point = new GPoint();
   
   point = this.currentProjection.fromLatLngToPixel( this.latlng,this.zoom);
   tile.x = Math.floor( point.x / this.tileSize);
   tile.y = Math.floor( point.y / this.tileSize);

   var boundspixels = tile.x * this.tileSize + " " +
                      tile.y * this.tileSize + " " +
                     (tile.x+1) * this.tileSize + " " +
                     (tile.y+1) * this.tileSize;

   latlng1 = this.currentProjection.fromPixelToLatLng(
      new GPoint(tile.x*this.tileSize, (tile.y+1)*this.tileSize), this.zoom);
   latlng2 = this.currentProjection.fromPixelToLatLng(
      new GPoint((tile.x+1)*this.tileSize, tile.y*this.tileSize), this.zoom);

   this.tileCoordinates = latlng1.lng() + "," + latlng1.lat() + "," +
                          latlng2.lng() + "," + latlng2.lat();

   this.tilePoint = new GPoint((point.x % this.tileSize),(point.y % this.tileSize) );
};


Tile.prototype = {
		
   // Returns geographical coordinates as: 'minX,minY,maxX,maxY'
   'getTileCoordinates': function() {
      return this.tileCoordinates;
   },
   
   'getTileWidth': function() {
      return this.width;
   },
   
   'getTileHeight': function() {
      return this.height;
   },
   // Point on the tile by its pixel coordinates.
   'getTilePoint': function() {
      return this.tilePoint;
   }
};