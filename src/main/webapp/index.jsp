
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Auscope Portal</title>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=abcdefg" type="text/javascript"></script>

    <!-- Bring in the ExtJs Libraries and CSS -->
    <link rel="stylesheet" type="text/css" href="js/ext-2.2/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="js/column-tree.css" />
    <script type="text/javascript" src="js/ext-2.2/adapter/ext/ext-base.js"> </script>
    <script type="text/javascript" src="js/ext-2.2/ext-all.js"> </script>
    <script type="text/javascript" src="js/ColumnNodeUI.js"> </script>

    <!-- Page specific javascript -->
    <script type="text/javascript">
        //this runs on DOM load - you can access all the good stuff now.
        Ext.onReady(function() {

            /*var tree = new Ext.tree.ColumnTree({
                region: 'west',
                split: true,
                collapsible: true,
                margins:'100 0 5 5',
                cmargins:'100 5 5 5',
                width: 300,
                //height: 300,
                rootVisible:false,
                autoScroll:true,
                title: 'Data Sources',
                //renderTo: Ext.getBody(),

                columns:[{
                    header:'Layers',
                    width:80,
                    dataIndex:'task'
                },{
                    header:'Visible',
                    width:20,
                    dataIndex:'duration'
                }],

                loader: new Ext.tree.TreeLoader({
                    dataUrl:'column-data.json',
                    uiProviders:{
                        'col': Ext.tree.ColumnNodeUI
                    }
                }),

                root: new Ext.tree.AsyncTreeNode({
                    text:'Tasks'
                })
            });*/
            
            var myLoader = new Ext.tree.TreeLoader(); 

            myLoader.on('load', function(node, callback){
                    alert('load for ' + node);
                     // create and add new nodes to node from any source you want,
                     // then call the callback to let it know you're done
                }, this);

            myLoader.on("beforeload", function(treeLoader, node) {
                alert('load for ' + node);
            }, this);
            
            var tree = new Ext.tree.TreePanel({
                title : 'Data Sources',
                region: 'west',
                split: true,
                collapsible: true,
                margins:'100 0 5 5',
                cmargins:'100 5 5 5',
                width: 200,
                useArrows:true,
                autoScroll:true,
                animate:true,
                //enableDD:true,
                containerScroll: true,
                rootVisible: true,

                // auto create TreeLoader
                //dataUrl: 'get-nodes.php',
                dataUrl: 'dataSources.json',
                //dataUrl: 'tree-data.jsone',
                //loader : myLoader,

                root: {
                    nodeType: 'async',
                    text: 'Ext JS',
                    draggable:false,
                    id:'task'
                }
            });

            tree.on('checkchange', function(node, isChecked) {
                //var clickedNode = tree.getSelectionModel().getSelectedNode();
                alert(node + " " + isChecked);                
            });

            tree.on('expandnode', function(node) {
                //var clickedNode = tree.getSelectionModel().getSelectedNode();
                alert(node + " expanded");
            });
            
            /*var westPanel = {
                    region:'west',
                    id:'west-div',
                    title:'Data Sources',
                    split:true,
                    //width: 200,
                    //minSize: 175,
                    maxSize: 400,
                    collapsible: true,
                    margins:'100 0 5 5',
                    cmargins:'100 5 5 5'
                };*/

            var centerPanel = new Ext.Panel({region:"center", margins:'100 5 5 0'});

            var viewport = new Ext.Viewport({
                layout:'border',
                items:[tree, centerPanel]
            });


    



        //<![CDATA[

        //function load() {
          //alert(window.location.host);
          //alert(${2+2});

          // Is user's browser suppported by Google Maps?
          if (GBrowserIsCompatible()) {
            //var map = new GMap2(document.getElementById("map-div"));
              var map = new GMap2(centerPanel.body.dom);
            // Large pan and zoom control
            map.addControl(new GLargeMapControl());
            // Toggle between Map, Satellite, and Hybrid types
            map.addControl(new GMapTypeControl());

            var startZoom = 4;
            //map.setCenter(new GLatLng(${centerLat},${centerLon}), 4);
            map.setCenter(new google.maps.LatLng(-18.604601, 138.493652), 5);
            map.setMapType(G_SATELLITE_MAP);

            //Thumbnail map
            var Tsize = new GSize(150, 150);
            map.addControl(new GOverviewMapControl(Tsize));

          }
        });

        // Create a base icon for all of our markers that specifies the
        // shadow, icon dimensions, etc.
      /*  var baseIcon = new GIcon();
        baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
        baseIcon.iconSize = new GSize(20, 34);
        baseIcon.shadowSize = new GSize(37, 34);
        baseIcon.iconAnchor = new GPoint(9, 34);
        baseIcon.infoWindowAnchor = new GPoint(9, 2);
        baseIcon.infoShadowAnchor = new GPoint(18, 25);

        // To Do: Checkboxes
        var CAT_ICONS = [];
        CAT_ICONS["DEFAULT_ICON"] = tinyIcon("green");
        CAT_ICONS["Hyperspectral"] = tinyIcon("red");
        CAT_ICONS["Mineral Occurences"] = tinyIcon("green");
        CAT_ICONS["Geological Units"] = tinyIcon("gray");
        CAT_ICONS["Geochemistry"] = tinyIcon("blue");
        CAT_ICONS["Bore holes"] = tinyIcon("yellow");
        CAT_ICONS["GNNS / GPS"] = tinyIcon("purple");
        CAT_ICONS["Seismic Imaging"] = tinyIcon("purple");
        */
        //]]>
    </script>

    <style>
        body {
            margin: 0px;
            padding: 0px;
        }

        #header-container {
            width: 100%;
            height: 100px;
            background-color: #ffffff;
        }

        #header {
            background-image: url( img/img-auscope-banner.gif );
            background-repeat: no-repeat;
            width: 100%;
            height: 100px;
            #margin: auto;
        }

        #logo{
            float: left;
            padding-top: 40px;
            padding-left: 0px;
        }

        #login {
            float: right;
            padding-right: 30px;
            padding-top: 10px;
        }

        img {
            border: none;
        }

        #nav {
            float: right;
            
            padding-top: 0px;
            padding-right: 40px;
        }

        #nav ul
        {
            text-align: left;
            padding: 0px;
            margin: 0px;
            #width: 1024px;
        }

        #nav ul li
        {
            display: inline;
            padding: 0px;
            margin: 0px;
        }
    </style>

</head>
<!--<body onload="load()" onunload="GUnload()">-->
<body onunload="GUnload()">

    <div id="header-container">
        <div id="header">
            <div id="logo">
                <a href="index.index.jsp#"></a>
            </div>
            <div id="nav">
                <ul>
                    <li><a href="index.index.jsp#"><img src="img/mapnav.gif" alt=""/></a></li>
                    <li><a href="index.index.jsp#"><img src="img/dataservice.gif" alt=""/></a></li>
                    <li><a href="index.index.jsp#"><img src="img/loginnav.gif" alt=""/></a></li>
                </ul>
            </div>
        </div>
    </div>


    <!--<div id="west-div">Some stuff here</div>
    <div id="center-div">Some other stuff here</div>
    <div id="map-div"></div>-->
</body>
</html>