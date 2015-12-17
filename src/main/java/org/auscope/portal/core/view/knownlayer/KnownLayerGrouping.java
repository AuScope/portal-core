package org.auscope.portal.core.view.knownlayer;

import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * A grouping of known layers and their related CSWRecords
 *
 * @author Josh Vote
 *
 */
public class KnownLayerGrouping {
    private List<KnownLayerAndRecords> knownLayers;
    private List<CSWRecord> unmappedRecords;
    private List<CSWRecord> originalRecordSet;

    /**
     * Creates a new immutable instance
     */
    public KnownLayerGrouping(List<KnownLayerAndRecords> knownLayers,
            List<CSWRecord> unmappedRecords, List<CSWRecord> originalRecordSet) {
        this.knownLayers = knownLayers;
        this.unmappedRecords = unmappedRecords;
        this.originalRecordSet = originalRecordSet;
    }

    /**
     * Gets the list of known layers. Will contain known layers which have drawn their CSWRecord set from originalRecordSet
     * 
     * @return
     */
    public List<KnownLayerAndRecords> getKnownLayers() {
        return knownLayers;
    }

    /**
     * Gets the list of CSWRecords that didn't map or end up belonging to the list of known layers
     * 
     * @return
     */
    public List<CSWRecord> getUnmappedRecords() {
        return unmappedRecords;
    }

    /**
     * The original set of CSWRecords that were used to populate/seed the knownlayers list
     * 
     * @return
     */
    public List<CSWRecord> getOriginalRecordSet() {
        return originalRecordSet;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "KnownLayerGrouping [knownLayers=" + knownLayers + ", unmappedRecords=" + unmappedRecords
                + ", originalRecordSet=" + originalRecordSet + "]";
    }
}
