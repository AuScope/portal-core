package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents the response from a NVCLDataService mosaic request
 *
 * See https://twiki.auscope.org/wiki/CoreLibrary/WebServicesDevelopment#MosSvc
 *
 * @author Josh Vote
 *
 */
public class MosaicResponse extends AbstractStreamResponse {

    /**
     * Creates a GetMosaicResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect either html or image MIME's).
     */
    public MosaicResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
