package org.auscope.portal.core.view.knownlayer;

import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * A tuple of a KnownLayer and a list of related CSW records
 * 
 * @author Josh Vote
 */
public class KnownLayerAndRecords {
    /** The known layer which 'owns' the specified records */
    private KnownLayer knownLayer;
    /** The list of CSWRecords that belong directly to this known layer */
    private List<CSWRecord> belongingRecords;
    /** The list of CSWRecords that have a weak relationship to this known layer */
    private List<CSWRecord> relatedRecords;

    /**
     * Creates a new instance
     * 
     * @param knownLayer
     *            The known layer which 'owns' the specified records
     * @param belongingRecords
     *            The list of CSWRecords that belong directly to this known layer
     * @param relatedRecords
     *            The list of CSWRecords that have a weak relationship to this known layer
     */
    public KnownLayerAndRecords(KnownLayer knownLayer,
            List<CSWRecord> belongingRecords, List<CSWRecord> relatedRecords) {
        this.knownLayer = knownLayer;
        this.belongingRecords = belongingRecords;
        this.relatedRecords = relatedRecords;
    }

    /**
     * The known layer that owns the records
     * 
     * @return
     */
    public KnownLayer getKnownLayer() {
        return knownLayer;
    }

    /**
     * Gets all records that belong directly to knownLayer
     * 
     * @return
     */
    public List<CSWRecord> getBelongingRecords() {
        return belongingRecords;
    }

    /**
     * Gets all records that have a weak relationship to knownLayer
     * 
     * @return
     */
    public List<CSWRecord> getRelatedRecords() {
        return relatedRecords;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "KnownLayerAndRecords [knownLayer=" + knownLayer + ", belongingRecords=" + belongingRecords
                + ", relatedRecords=" + relatedRecords + "]";
    }
}
