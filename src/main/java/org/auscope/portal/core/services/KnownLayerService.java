package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelectors;

/**
 * A service class performing that groups CSWRecord objects (from a CSWCacheService) according to a configured list of KnownLayers
 *
 * @author Josh Vote
 *
 */
public class KnownLayerService {
    private final Log logger = LogFactory.getLog(getClass());
    private List<KnownLayer> knownLayers;
    private CSWCacheService cswCacheService;

    /**
     * Creates a new instance of this class from an untyped list. All objects in knownTypes that can be cast into a KnownLayer will be included in the internal
     * known layer list
     *
     * @param knownTypes
     *            A list of objects, only KnownLayer subclasses will be used
     * @param cswCacheService
     *            An instance of CSWCacheService
     */
    public KnownLayerService(@SuppressWarnings("rawtypes") List knownTypes,
            CSWCacheService cswCacheService) {
        this.knownLayers = new ArrayList<>();
        for (Object obj : knownTypes) {
            if (obj instanceof KnownLayer) {
                this.knownLayers.add((KnownLayer) obj);
            }
        }

        this.cswCacheService = cswCacheService;
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service record set and applying each of those records to one or more Known Layer objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will then be returned.
     *
     * This overload does not impose a filter for the type of class that is allowed and therefore won't exclude anything from the list.
     *
     * @return An instance of KnownLayerGrouping that encapsulates all the known layers, as well as any unmapped records and the original record list.
     */
    public KnownLayerGrouping groupKnownLayerRecords() {
        return groupKnownLayerRecords((Class<?>[])null);
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service record set and applying each of those records to one or more Known Layer objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will then be returned.
     *
     * This overload allows you explicitly state which classes should be included.
     *
     * @param classFilters
     *            An array of classes that should be included. You can use this to restrict the output to only include items that are of a particular subclass
     *            of KnownLayer.
     * @return An instance of KnownLayerGrouping that encapsulates the known layers you've selected, as well as any unmapped records and the original record
     *         list.
     */
    public <T extends KnownLayer> KnownLayerGrouping groupKnownLayerRecords(Class<?>... classFilters) {
        List<CSWRecord> originalRecordList = this.cswCacheService.getRecordCache();
        List<KnownLayerAndRecords> knownLayerAndRecords = new ArrayList<>();
        Map<String, Object> mappedRecordIDs = new HashMap<>();

        // Figure out what records belong to which known layers (could be multiple)
        for (KnownLayer knownLayer : knownLayers) {
            // We have to do this part regardless of the classFilters because
            // if not, the results for unmappedRecords will be incorrect.
            // (I.e.: they'll include related features from things that have
            // been put in Research Data tab).
            // GPT-103 - the problem is in the creation of the belonging and related records - the order is not the same as input
            KnownLayerSelector selector = knownLayer.getKnownLayerSelector();
            List<CSWRecord> relatedRecords = new ArrayList<>();
            List<CSWRecord> belongingRecords = new ArrayList<>();

            // For each record, mark it as being added to a known layer (if appropriate)
            // We also need to mark the record as being mapped using mappedRecordIDs
            for (CSWRecord record : originalRecordList) {
                // System.out.println(" test this record: " + record.getLayerName());
                try {
                    switch (selector.isRelatedRecord(record)) {
                    case Related:
                        addToListConsiderWMSSelectors(relatedRecords, record, knownLayer);

                        // relatedRecords.add(indexInWMSSelectorsList, record);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);
                        break;
                    case Belongs:
                        addToListConsiderWMSSelectors(belongingRecords, record, knownLayer);
                        // belongingRecords.add(record);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);
                        break;
                    default:
                        // Ignore metadata CSW records - they have no named online resources and no geographic element BBOXes
                        // To ignore we must add it to the mapped record ids, so it does not get counted as an unmapped record
                        if (!record.hasNamedOnlineResources() && !record.hasGeographicElements()) {
                            mappedRecordIDs.put(record.getFileIdentifier(), null);
                        }
                    }
                } catch (PortalServiceException e) {
                    logger.error("Expecting data to line up", e);
                }
            }

            // The include flag will indicate whether or not this particular layer
            // should be included in the output.
            boolean include = false;

            // If no filters have been set then we just check that the KnownLayer is not a derived type:
            if (classFilters == null) {
                include = knownLayer.getClass().equals(KnownLayer.class);
            } else {
                // Otherwise we have to see if this particular known layer matches
                // any of the filters:
                for (Class<?> classFilter : classFilters) {
                    if (classFilter.isAssignableFrom(knownLayer.getClass())) {
                        include = true;
                        break;
                    }
                }
            }

            // If the include flag got set then we can add this record:
            if (include) {
                knownLayerAndRecords.add(new KnownLayerAndRecords(knownLayer, belongingRecords, relatedRecords));
            }
        }

        // Finally work out which records do NOT belong to a known layer
        List<CSWRecord> unmappedRecords = new ArrayList<>();
        for (CSWRecord record : originalRecordList) {
            if (!mappedRecordIDs.containsKey(record.getFileIdentifier())) {
                unmappedRecords.add(record);
            }
        }

        return new KnownLayerGrouping(knownLayerAndRecords, unmappedRecords, originalRecordList);
    }

    /**
     * GPT-103 - Conjunction Layers (from GPT-40) - the order is all out of wack in relatedRecords and belongsRecords from the Conjunction order given in
     * auscope_known_layers due to the order being from the List<CSWRecords> originalRecordList (which came from the GeoNetwork Server). So have to restore the
     * order.
     *
     * This method will use the order of the layers in the WMSSelectors.wmsSelectors layer IFF WMSSelectors.layersMode == AND
     *
     * @param listToUpdate
     *            - add record to this list at the index specified by the order of record.getName() in WMSSelectors.wmsSelectors list items. The size of the
     *            array if (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) will be the number of layers defined in the WMSSelectors. If not
     *            (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) then the size will be the number of items that have been added over time.
     * @param record
     *            - to add to listToUpdate
     * @param knownLayer
     *            - that the record belonged to
     * @throws PortalServiceException
     *             when knownLayer.getKnownLayerSelector() instanceof WMSSelectors but the record.getLayerName() to be part of WMSSelector list in
     *             WMSSelectors
     */
    void addToListConsiderWMSSelectors(List<CSWRecord> listToUpdate, CSWRecord record, KnownLayer knownLayer)
            throws PortalServiceException {
        KnownLayerSelector selector = knownLayer.getKnownLayerSelector();

        if (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) {

            // WMSSelectors are used to construct conjuncted (AND) and disjuncted (OR) WMSSelector layers
            List<String> layerNames = getWMSelectorsLayerNames(((WMSSelectors) selector).getWmsSelectors());

            // Use Array internally - Lists don't work so well when adding to indexed positions
            CSWRecord[] arrayToUpdate = Arrays.copyOf(listToUpdate.toArray(new CSWRecord[0]), layerNames.size());

            int indexInWMSSelectorsList = layerNames.indexOf(record.getLayerName());
            if (indexInWMSSelectorsList == -1) {
                // listToUpdate.add(record);
                throw new PortalServiceException(
                        "Expecting record.getLayerName() to be part of WMSSelector list in WMSSelectors - gsn(): "+ record.getLayerName() + ", layerNames: "+ layerNames);
            } else {
                // Keep the order in the bean definition list
                arrayToUpdate[indexInWMSSelectorsList] = record;
                listToUpdate.clear();
                listToUpdate.addAll(Arrays.asList(arrayToUpdate));
            }
        } else {
            listToUpdate.add(record);
        }
    }

    /**
     * @param wmsSelectors
     * @return layerNames from the selectors
     */
    private static List<String> getWMSelectorsLayerNames(List<WMSSelector> wmsSelectors) {
        List<String> layerNames = new ArrayList<>();
        for (WMSSelector wmsSelector : wmsSelectors) {
            layerNames.add(wmsSelector.getLayerName());
        }
        return layerNames;
    }
}
