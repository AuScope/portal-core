<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<!-- Credits for some icons from http://www.fatcow.com/free-icons/ under http://creativecommons.org/licenses/by/3.0/us/-->
<html xmlns:v="urn:schemas-microsoft-com:vml">
    <head>
        <title>Administration Diagnostics</title>

        <link rel="stylesheet" type="text/css" href="js/external/extjs/resources/css/ext-all.css">

        <script type="text/javascript" src="js/external/extjs/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="js/external/extjs/ext-all.js"></script>
        <script type="text/javascript" src="js/widgets/RowExpander.js"></script>

        <script type="text/javascript" src="js/AbstractRecordWrapper.js"></script>
        <script type="text/javascript" src="js/CSWRecord.js"></script>
        <script type="text/javascript" src="js/CSWRecordStore.js"></script>
        <script type="text/javascript" src="js/KnownLayerRecord.js"></script>
        <script type="text/javascript" src="js/KnownLayerStore.js"></script>
        <script type="text/javascript" src="js/BBox.js"></script>

        <script type="text/javascript" src="js/admin/tests/TestStatus.js"></script>
        <script type="text/javascript" src="js/admin/tests/BaseTest.js"></script>
        <script type="text/javascript" src="js/admin/tests/SingleAJAXTest.js"></script>
        <script type="text/javascript" src="js/admin/tests/ExternalConnectivity.js"></script>
        <script type="text/javascript" src="js/admin/tests/RegistryConnectivity.js"></script>
        <script type="text/javascript" src="js/admin/tests/Vocabulary.js"></script>
        <script type="text/javascript" src="js/admin/tests/KnownLayerWFS.js"></script>
        <script type="text/javascript" src="js/admin/tests/KnownLayerWMS.js"></script>
        <script type="text/javascript" src="js/admin/tests/RegisteredLayerWFS.js"></script>
        <script type="text/javascript" src="js/admin/tests/RegisteredLayerWMS.js"></script>
        <script type="text/javascript" src="js/admin/TestResultsPanel.js"></script>
        <script type="text/javascript" src="js/admin/BuildInfoFieldSet.js"></script>
        <script type="text/javascript" src="js/admin/RuntimeInfoFieldSet.js"></script>
   </head>

   <body>
        <script type="text/javascript">
        var manifest = {
            specificationTitle : '${specificationTitle}',
            implementationVersion : '${implementationVersion}',
            implementationBuild : '${implementationBuild}',
            buildDate : '${buildDate}',
            buildJdk : '${buildJdk}',
            javaVendor : '${javaVendor}',
            builtBy : '${builtBy}',
            osName : '${osName}',
            osVersion : '${osVersion}',
            serverName : '${serverName}',
            serverInfo : '${serverInfo}',
            serverJavaVersion : '${serverJavaVersion}',
            serverJavaVendor : '${serverJavaVendor}',
            javaHome : '${javaHome}',
            serverOsArch : '${serverOsArch}',
            serverOsName : '${serverOsName}',
            serverOsVersion : '${serverOsVersion}'
        };

        Ext.onReady(function() {
            var viewport = new Ext.Viewport({
                layout:'border',
                margins: '5 5 5 5',
                items:[{
                      xtype : 'form',
                      region : 'west',
                      title : String.format('Manifest Details for \'{0}\'', manifest.specificationTitle),
                      collapsible : true,
                      autoScroll: true,
                      width : 350,
                      split: true,
                      minSize: 200,
                      maxSize: 500,
                      items : [{
                          xtype : 'buildinfofieldset',
                          manifest : manifest,
                          height: 245
                      },{
                          xtype : 'runtimeinfofieldset',
                          manifest : manifest,
                          height: 300
                      }]
                },{
                    region : 'center',
                    title : 'Diagnostic Tests',
                    autoScroll : true,
                    items : [{
                        xtype : 'testresultspanel',
                        autoHeight : true,
                    }]
                }]
            });
        });
        </script>
   </body>

</html>