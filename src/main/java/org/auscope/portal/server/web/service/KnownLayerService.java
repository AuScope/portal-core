package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.domain.auscope.KnownLayerAndRecords;
import org.auscope.portal.server.domain.auscope.KnownLayerGrouping;
import org.auscope.portal.server.web.view.KnownLayer;
import org.auscope.portal.server.web.view.knownlayer.KnownLayerSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A service class performing that groups CSWRecord objects (from a CSWCacheService) according
 * to a configured list of KnownLayers
 *
 * @author Josh Vote
 *
 */
@Service
public class KnownLayerService {
    private List<KnownLayer> knownLayers;
    private CSWCacheService cswCacheService;

    /**
     * Creates a new instance of this class from an untyped list. All objects in knownTypes that
     * can be cast into a KnownLayer will be included in the internal known layer list
     * @param knownTypes A list of objects, only KnownLayer subclasses will be used
     * @param cswCacheService An instance of CSWCacheService
     */
    @Autowired
    public KnownLayerService(@SuppressWarnings("rawtypes") @Qualifier("knownTypes") ArrayList knownTypes,
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
     * then be returned
     * @return
     */
    public KnownLayerGrouping groupKnownLayerRecords() {
        List<CSWRecord> originalRecordList = this.cswCacheService.getRecordCache();
        List<KnownLayerAndRecords> knownLayerAndRecords = new ArrayList<KnownLayerAndRecords>();
        Map<String, Object> mappedRecordIDs = new HashMap<String, Object>();

        //Figure out what records belong to which known layers (could be multiple)
        for (KnownLayer knownLayer : knownLayers) {
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

        //Finally workout which records do NOT belong to a known layer
        List<CSWRecord> unmappedRecords = new ArrayList<CSWRecord>();
        for (CSWRecord record : originalRecordList) {
            if (!mappedRecordIDs.containsKey(record.getFileIdentifier())) {
                unmappedRecords.add(record);
            }
        }

        return new KnownLayerGrouping(knownLayerAndRecords, unmappedRecords, originalRecordList);
    }
}
