package org.auscope.portal.core.server.controllers;

import java.util.List;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.CSWService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CSWController extends BaseCSWController {

    private HttpServiceCaller serviceCaller;

    @Autowired
    public CSWController(
            HttpServiceCaller serviceCaller,
            ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
        this.serviceCaller = serviceCaller;
    }

    /**
     *
     * @param dateString
     *            in format: 28/02/2013
     * @param endOfDay
     *            false means the time will be 00:00:00:000 true means the time will be 23:59:59:999
     * @return
     */
    private LocalDateTime stringToDateTime(String dateString, boolean endOfDay) {
        String[] date = dateString.split("/");
        return LocalDateTime.of(
                Integer.parseInt(date[2]), // year
                Integer.parseInt(date[1]), // monthOfYear
                Integer.parseInt(date[0]), // dayOfMonth
                endOfDay ? 23 : 0, // hourOfDay
                endOfDay ? 59 : 0, // minuteOfHour
                endOfDay ? 59 : 0, // secondOfMinute
                endOfDay ? 999 : 0); // millisOfSecond
    }

    /**
     * Requests CSW records from the cswServiceUrl provided. The results will be filtered by the CQL Text and the filter options. Records will be returned from
     * the starting point (where 1 is the first record, not 0) and the number of records retrieved will not exceed maxRecords.
     *
     * @param cswServiceUrl
     * @param recordInfoUrl
     * @param cqlText
     * @param start
     * @param limit
     * @param bbox
     * @param anyText
     * @param title
     * @param abstract_
     * @param metadataDateFrom
     * @param metadataDateTo
     * @param temporalExtentFrom
     * @param temporalExtentTo
     * @return Example: "data":[portal.csw.CSWRecord], // These are anonymous objects, you can use them as the config for portal.csw.CSWRecord.
     *         "msg":"No errors", "totalResults":18, // This is not the number of results returned, it is the number of results the query matched.
     *         "success":true
     */
    @RequestMapping("/getUncachedCSWRecords.do")
    public ModelAndView getUncachedCSWRecords(
            @RequestParam(value = "cswServiceUrl", required = false) String cswServiceUrl,
            @RequestParam(value = "recordInfoUrl", required = false) String recordInfoUrl,
            @RequestParam(value = "start", required = false) int start,
            @RequestParam(value = "limit", required = false) int limit,
            @RequestParam(value = "bbox", required = false) String bbox,
            @RequestParam(value = "northBoundLatitude", defaultValue = "NaN", required = false) double northBoundLatitude,
            @RequestParam(value = "eastBoundLongitude", defaultValue = "NaN", required = false) double eastBoundLongitude,
            @RequestParam(value = "southBoundLatitude", defaultValue = "NaN", required = false) double southBoundLatitude,
            @RequestParam(value = "westBoundLongitude", defaultValue = "NaN", required = false) double westBoundLongitude,
            @RequestParam(value = "anyText", required = false) String anyText,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "abstract_", required = false) String abstract_,
            @RequestParam(value = "metadataDateFrom", required = false) String metadataDateFrom,
            @RequestParam(value = "metadataDateTo", required = false) String metadataDateTo,
            @RequestParam(value = "temporalExtentFrom", required = false) String temporalExtentFrom,
            @RequestParam(value = "temporalExtentTo", required = false) String temporalExtentTo) {
        CSWServiceItem endpoint = new CSWServiceItem(
                "", // This ID won't actually be used so we can just leave it blank.
                cswServiceUrl,
                recordInfoUrl);

        CSWService cswService = new CSWService(
                endpoint,
                this.serviceCaller,
                false);

        try {
            FilterBoundingBox spatialBounds;

            // If ALL the explicit bounds have been set we will use them,
            // otherwise we'll just the viewport (i.e. bbox)
            if (Double.isNaN(northBoundLatitude)
                    || Double.isNaN(southBoundLatitude)
                    || Double.isNaN(eastBoundLongitude)
                    || Double.isNaN(westBoundLongitude)) {
                spatialBounds = FilterBoundingBox.attemptParseFromJSON(bbox);
            } else {
                spatialBounds = new FilterBoundingBox(
                        "EPSG:4326",
                        new double[] {eastBoundLongitude, southBoundLatitude},
                        new double[] {westBoundLongitude, northBoundLatitude});
            }

            CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(
                    anyText,
                    spatialBounds);

            filter.setTitle(title);
            filter.setAbstract(abstract_);

            if (!metadataDateFrom.isEmpty()) {
                filter.setMetadataChangeDateFrom(stringToDateTime(metadataDateFrom, false));
            }

            if (!metadataDateTo.isEmpty()) {
                filter.setMetadataChangeDateTo(stringToDateTime(metadataDateTo, true));
            }

            if (!temporalExtentFrom.isEmpty()) {
                filter.setTemporalExtentFrom(stringToDateTime(temporalExtentFrom, false));
            }

            if (!temporalExtentTo.isEmpty()) {
                filter.setTemporalExtentTo(stringToDateTime(temporalExtentTo, true));
            }

            CSWGetRecordResponse response = cswService.queryCSWEndpoint(
                    start,
                    limit,
                    filter);

            List<CSWRecord> records = response.getRecords();

            return generateJSONResponseMAV(
                    records.toArray(new CSWRecord[records.size()]),
                    response.getRecordsMatched());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return generateJSONResponseMAV(false);
    }

}