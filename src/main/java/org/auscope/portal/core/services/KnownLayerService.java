package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;

/**
 * A service class performing that groups CSWRecord objects (from a CSWCacheService) according
 * to a configured list of KnownLayers
 *
 * @author Josh Vote
 *
 */
public class KnownLayerService {
    private List<KnownLayer> knownLayers;
    private CSWCacheService cswCacheService;

    /**
     * Creates a new instance of this class from an untyped list. All objects in knownTypes that
     * can be cast into a KnownLayer will be included in the internal known layer list
     * @param knownTypes A list of objects, only KnownLayer subclasses will be used
     * @param cswCacheService An instance of CSWCacheService
     */
    public KnownLayerService(@SuppressWarnings("rawtypes") ArrayList knownTypes,
            CSWCacheService cswCacheService) {
        this.knownLayers = new ArrayList<KnownLayer>();
        for (Object obj : knownTypes) {
            if (obj instanceof KnownLayer) {
                this.knownLayers.add((KnownLayer) obj);
            }
        }

        this.cswCacheService = cswCacheService;
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service
     * record set and applying each of those records to one or more Known Layer
     * objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will
     * then be returned.
     * 
     * This overload does not impose a filter for the type of class that is 
     * allowed and therefore won't exclude anything from the list.
     * 
     * @return
     *      An instance of KnownLayerGrouping that encapsulates all the known 
     *      layers, as well as any unmapped records and the original record 
     *      list.
     */
    public KnownLayerGrouping groupKnownLayerRecords() {
        return groupKnownLayerRecords(null);
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service
     * record set and applying each of those records to one or more Known Layer
     * objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will
     * then be returned.
     * 
     * This overload allows you explicitly state which classes should be included.
     * 
     * @param classFilters
     *      An array of classes that should be included. You can use this to
     *      restrict the output to only include items that are of a particular
     *      subclass of KnownLayer.
     * @return
     *      An instance of KnownLayerGrouping that encapsulates the known layers
     *      you've selected, as well as any unmapped records and the original
     *      record list.
     */
    public <T extends KnownLayer> KnownLayerGrouping groupKnownLayerRecords(Class<T>... classFilters) {
        List<CSWRecord> originalRecordList = this.cswCacheService.getRecordCache();
        List<KnownLayerAndRecords> knownLayerAndRecords = new ArrayList<KnownLayerAndRecords>();
        Map<String, Object> mappedRecordIDs = new HashMap<String, Object>();

        //Figure out what records belong to which known layers (could be multiple)
        for (KnownLayer knownLayer : knownLayers) {
            // The include flag will indicate whether or not this particular layer 
            // should be included in the output.
            boolean include = false;
        
            // If no filters have been set then we just check that the KnownLayer is not a derived type:
            if (classFilters == null){
                include = knownLayer.getClass().equals(KnownLayer.class);
            }
            else {
                // Otherwise we have to see if this particular known layer matches 
                // any of the filters:
                for (Class<T> classFilter : classFilters) {
                    if (classFilter.isAssignableFrom(knownLayer.getClass())) {
                        include = true;
                        break;
                    }               
                }
            }

            // If the include flag got set then we can add this record:
            if (include) {
                KnownLayerSelector selector = knownLayer.getKnownLayerSelector();
                List<CSWRecord> relatedRecords = new ArrayList<CSWRecord>();
                List<CSWRecord> belongingRecords = new ArrayList<CSWRecord>();
    
                //For each record, mark it as being added to a known layer (if appropriate)
                //We also need to mark the record as being mapped using mappedRecordIDs
                for (CSWRecord record : originalRecordList) {
                    switch (selector.isRelatedRecord(record)) {
                    case Related:
                        relatedRecords.add(record);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);
                        break;
                    case Belongs:
                        belongingRecords.add(record);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);
                        break;
                    }
                }
    
                knownLayerAndRecords.add(new KnownLayerAndRecords(knownLayer, belongingRecords, relatedRecords));
            }
        }

        //Finally work out which records do NOT belong to a known layer
        List<CSWRecord> unmappedRecords = new ArrayList<CSWRecord>();
        for (CSWRecord record : originalRecordList) {
            if (!mappedRecordIDs.containsKey(record.getFileIdentifier())) {
                unmappedRecords.add(record);
            }
        }

        return new KnownLayerGrouping(knownLayerAndRecords, unmappedRecords, originalRecordList);   
    }
}