<!-- This must come BEFORE javascript includes! -->
<link rel="stylesheet" type="text/css" href="portal-core/js/ext-5.1.1/build/packages/ext-theme-neptune/build/resources/ext-theme-neptune-all.css">
<link rel="stylesheet" type="text/css" href="portal-core/js/ext-5.1.1/build/packages/sencha-charts/build/neptune/resources/sencha-charts-all.css">

<!-- link extjs 5 -->
<script type="text/javascript" src="portal-core/js/ext-5.1.1/build/ext-all-debug.js?v=${buildTimestamp}"></script>
<script type="text/javascript" src="portal-core/js/ext-5.1.1/build/packages/ext-ux/build/ext-ux-debug.js?v=${buildTimestamp}"></script>
<script type="text/javascript" src="portal-core/js/ext-5.1.1/build/examples/ux/Spotlight.js?v=${buildTimestamp}"></script>
<script type="text/javascript" src="portal-core/js/ext-5.1.1/build/examples/ux/form/SearchField.js?v=${buildTimestamp}"></script>
<script type="text/javascript" src="portal-core/js/ext-5.1.1/build/packages/sencha-charts/build/sencha-charts.js?v=${buildTimestamp}"></script>

<!-- patches -->
<!-- script type="text/javascript" src="portal-core/js/extjs-patches/5.1.0/ExtPatch.js?v=${buildTimestamp}"></script-->
<script type="text/javascript" src="portal-core/js/openlayers-patches/2.13.1/OpenLayers_patch.js?v=${buildTimestamp}"></script>
<script src="portal-core/js/extjs-patches/5.1.1/ExtPatch.js?v=${buildTimestamp}" type="text/javascript"></script>

<link rel="stylesheet" href="portal-core/js/extjs-ux-externals/form/plugin/FieldHelpText.css">
<script src="portal-core/js/extjs-ux-externals/form/plugin/FieldHelpText.js?v=${buildTimestamp}" type="text/javascript"></script>

<!-- Link other external libraries -->
<script type="text/javascript" src="portal-core/js/javeline/javeline_xpath.js?v=${buildTimestamp}"></script>

<script src="portal-core/js/portal/Compatibility.js?v=${buildTimestamp}" type="text/javascript"></script>

<!-- load the portal javascript -->
<script src="portal-core/js/admin/global.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/help/Instruction.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/help/InstructionManager.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/xml/SimpleDOM.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/xml/SimpleXPath.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/Ajax.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/Base64.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/BBoxType.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/BBox.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/GoogleAnalytic.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/PiwikAnalytic.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/misc/BrowserWindowWithWarning.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/FileDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/ObservableMap.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/ProviderNameTransformer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/UnimplementedFunction.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/URL.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/BaseSerializer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/SerializerV0.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/SerializerV1.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/SerializerV2.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/SerializerV3.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/serializers/SerializerV4.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/DeserializationHandler.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/util/permalink/MapStateSerializer.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/map/BaseMap.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/BasePrimitiveManager.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/Icon.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/Point.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/Size.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/TileInformation.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/primitives/BasePrimitive.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/primitives/BaseWMSPrimitive.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/primitives/Marker.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/primitives/Polygon.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/map/primitives/Polyline.js?v=${buildTimestamp}" type="text/javascript"></script>
<!-- Provider specific mapping API's are excluded from this file. They can be found in seperate jsimports-*.htm files. -->

<script src="portal-core/js/portal/map/openlayers/ActiveLayerManager.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/csw/OnlineResourceType.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/csw/OnlineResource.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/csw/CSWRecordType.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/csw/CSWRecord.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/knownlayer/KnownLayer.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/layer/downloader/Downloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/DownloaderFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/coverage/WCSDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/coverage/OPeNDAPDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/wfs/WFSDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/wfs/KLWFSDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/downloader/wms/WMSDownloader.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/layer/filterer/BaseFilterForm.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/filterer/forms/EmptyFilterForm.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/filterer/forms/WMSLayerFilterForm.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/filterer/Filterer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/filterer/FormFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/BaseComponent.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/Legend.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/wfs/WFSLegend.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/wms/WMSLegend.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/csw/CSWLegend.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/legend/wms/WMSLegendForm.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/BaseComponent.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/Querier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/QuerierFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/QueryTarget.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/QueryTargetHandler.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/csw/CSWQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/factories/BaseFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/factories/SimpleFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/knownlayerfactories/BaseFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/FeatureSource.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/featuresources/WFSFeatureSource.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/featuresources/WFSFeatureByPropertySource.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/KnownLayerParser.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/Parser.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/WFSQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wfs/WFSWithMapQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wms/WMSQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wms/WMSXMLFormatQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/coverage/WCSQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/querier/wms/WMSMultipleTabDisplayQuerier.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/RenderDebuggerData.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/Renderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/RendererFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/RenderStatus.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wfs/FeatureDownloadManager.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wfs/FeatureRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wfs/FeatureWithMapRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wfs/KMLParser.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wfs/GMLParser.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/csw/CSWRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/kml/KMLRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/layer/renderer/wms/LayerRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/renderer/wms/DisjunctionLayerRenderer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/Layer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/LayerFactory.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/layer/LayerStore.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/widgets/field/ClearableComboBox.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/field/ClientSearchField.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/field/ClearableTextField.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/field/WMSCustomSearchField.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/field/DataDisplayField.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/field/ImageDisplayField.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/column/ClickColumn.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/column/RenderableCheckColumn.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/plugin/CellTips.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/plugin/SelectableGrid.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/plugin/RowContextMenu.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/plugin/RowExpanderContainer.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/grid/plugin/InlineContextMenu.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/layout/AccordianDefault.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/recordpanel/AbstractChild.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/recordpanel/GroupPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/recordpanel/RowPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/recordpanel/RecordPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CommonBaseRecordPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/BaseRecordPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWConstraintsPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWMetadataPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWRecordPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/model/CustomRegistryModel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CustomRegistryTreeGrid.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/FilterPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/KnownLayerPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/LayerPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/OnlineResourcesPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/plugins/ClickableImage.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/plugins/CollapsedAccordianLayout.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/plugins/HeaderIcons.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/tab/ActivePreRenderTabPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/CSWRecordConstraintsWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/CSWSelectionWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/CSWRecordDescriptionWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/PermanentLinkWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/ErrorWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWReportPagingPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWRecordPagingPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/window/CSWFilterWindow.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/model/CSWServices.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/panel/CSWFilterFormPanel.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/widgets/FilterPanelMenuFactory.js?v=${buildTimestamp}" type="text/javascript"></script>

<script src="portal-core/js/portal/charts/BaseD3Chart.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/portal/charts/3DScatterPlot.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/lzma/lzma.js?v=${buildTimestamp}" type="text/javascript"></script>
<script src="portal-core/js/lzma/lzma_worker.js?v=${buildTimestamp}" type="text/javascript"></script>
