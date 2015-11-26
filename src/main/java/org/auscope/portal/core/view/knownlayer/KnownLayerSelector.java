package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * A known layer selector is a way of selecting records that match certain criteria
 *
 * @author Josh Vote
 */
public interface KnownLayerSelector {

    /**
     * The different ways in which a CSWRecord can 'relate' to a KnownLayer
     */
    public enum RelationType {
        /**
         * There is no relation between known layer and csw record
         */
        NotRelated,
        /**
         * A weak relation indicating that the record isn't completely independent from a known layer, but it's certainly not going to be used directly for
         * interactions with the known layer
         */
        Related,
        /**
         * A strong relation indicating that the record is explicitly grouped by this known layer and will be used directly when querying/rendering the known
         * layer.
         */
        Belongs
    }

    /**
     * Tests the type of relation that a particular CSWRecord has to this KnownLayer. There are a variety of relations documented at RelationType
     *
     * @see RelationType
     * @param record
     * @return whether this record is related
     */
    public abstract RelationType isRelatedRecord(CSWRecord record);

}
