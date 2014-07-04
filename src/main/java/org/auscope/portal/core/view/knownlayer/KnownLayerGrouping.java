package org.auscope.portal.core.view.knownlayer;

import java.util.ArrayList;
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
        this.cleanUpWMS(this.unmappedRecords);
        this.originalRecordSet = originalRecordSet;
    }

    /**
     * Gets the list of known layers. Will contain known layers
     * which have drawn their CSWRecord set from originalRecordSet
     * @return
     */
    public List<KnownLayerAndRecords> getKnownLayers() {
        return knownLayers;
    }

    /**
     * Gets the list of CSWRecords that didn't map or end up
     * belonging to the list of known layers
     * @return
     */
    public List<CSWRecord> getUnmappedRecords() {
        return unmappedRecords;
    }

    /**
     * The original set of CSWRecords that were used to populate/seed
     * the knownlayers list
     * @return
     */
    public List<CSWRecord> getOriginalRecordSet() {
        return originalRecordSet;
    }

    private void cleanUpWMS(List<CSWRecord> unmappedRecords){

        //set the version by looking at the first resource protocol
        for(CSWRecord rec:unmappedRecords){
            if(rec.getOnlineResources()!= null && rec.getOnlineResources().length>0 ){
                //VT: keep looping till we find a wms protocol then we "guess" its version
                for(int i=0;i <rec.getOnlineResources().length;i++ ){
                    if(rec.getOnlineResources()[i].getProtocol().toLowerCase().contains("wms")){
                        if(rec.getOnlineResources()[0].getProtocol().contains("1.3.0")){
                            rec.setVersion("1.3.0");
                        }else{
                            rec.setVersion("1.1.1");
                        }
                        break;
                    }
                }
            }

        }

    }

}
