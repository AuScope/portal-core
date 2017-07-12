package org.auscope.portal.core.server.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BaseCSWController;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.services.SeismicSurveyWMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Scope("session")
// this can't be a singleton as each request by a user may be targeting a
// specific wms version
public class SeismicSurveyWMS extends BaseCSWController {

    // ----------------------------------------------------- Instance variables

    private SeismicSurveyWMSService seismicWMSService;
    private final Log log = LogFactory.getLog(getClass());
    private int BUFFERSIZE = 1024 * 1024;
    HttpServiceCaller serviceCaller;

    // ----------------------------------------------------------- Constructors

    @Autowired
    public SeismicSurveyWMS(SeismicSurveyWMSService seismicWMSService,
            ViewCSWRecordFactory viewCSWRecordFactory,
            ViewKnownLayerFactory knownLayerFact,
            HttpServiceCaller serviceCaller) {
        super(viewCSWRecordFactory, knownLayerFact);
        this.seismicWMSService = seismicWMSService;
        this.serviceCaller = serviceCaller;
    }

    @RequestMapping(value = "/getSeismicCSWRecord.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getSeismicCSWRecord(@RequestParam("service_URL") String serviceUrl,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        CSWRecord[] record = new CSWRecord[1];
        record[0] = this.seismicWMSService.getCSWRecord(serviceUrl);
        record[0].setRecordInfoUrl(serviceUrl.replace("/xml", ""));
        ModelAndView mav = generateJSONResponseMAV(record, record.length);

        return mav;

    }

}
