/**
 * 
 */
package org.auscope.portal.core.view.knownlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * This class is for handling multiple layers to group them together in some way. Either they are 'AND'ed and all layers show together effectively on-top of
 * each other. This is most relevant where there are complementary layers that show the same features but at different scales - one might show at scales less
 * than 1:x and another might show at scales greater (equal) 1:x. Written as part of GPT-41 for Geoscience Surface Geology layer group.
 * 
 * @author Brooke Smith
 * 
 */
public class WMSSelectors implements KnownLayerSelector {

    private List<WMSSelector> wmsSelectors;
    private SelectorsMode layersMode;

    private WMSSelectors() {
        super();
    }

    public WMSSelectors(SelectorsMode layersMode, List<String> layerNames) {
        this();
        this.layersMode = layersMode;
        wmsSelectors = new ArrayList<>();
        for (String layerName : layerNames) {
            WMSSelector wmsSelector = new WMSSelector(layerName);
            wmsSelectors.add(wmsSelector);
            // Now set the other layers as related
            Set<String> otherLayerNames = new HashSet<>(layerNames);
            otherLayerNames.remove(layerName);
            wmsSelector.setRelatedLayerNames(otherLayerNames.toArray(new String[0]));
        }
    }

    /**
     * @return the layersMode
     */
    public SelectorsMode getLayersMode() {
        return layersMode;
    }

    /**
     * @return the wmsSelectors
     */
    public List<WMSSelector> getWmsSelectors() {
        return wmsSelectors;
    }

    /**
     * @param layersMode
     *            the layersMode to set
     */
    public void setLayersMode(SelectorsMode layersMode) {
        this.layersMode = layersMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.auscope.portal.core.view.knownlayer.KnownLayerSelector#isRelatedRecord(org.auscope.portal.core.services.responses.csw.CSWRecord)
     * 
     * We need to get the RelationType for all wmsSelectors and return the 'greatest' one (Belongs, Related, Not Related is strongest to weakest). And we only
     * want to return one of each type or else we'll get multiply defined records - subsequent ones are 'Not Related'
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        RelationType greatestRelationship = RelationType.NotRelated;
        for (WMSSelector selector : wmsSelectors) {
            if (selector.isRelatedRecord(record).ordinal() > greatestRelationship.ordinal()) {
                greatestRelationship = selector.isRelatedRecord(record);
            }
        }
        return greatestRelationship;
    }
}
