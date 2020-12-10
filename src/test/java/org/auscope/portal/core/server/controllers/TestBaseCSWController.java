package org.auscope.portal.core.server.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.auscope.portal.core.services.GoogleCloudMonitoringCachedService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for BaseCSWController
 *
 * @author Josh Vote
 *
 */
public class TestBaseCSWController extends PortalTestClass {

    private class KnownLayerControllerImpl extends KnownLayerController {
        public KnownLayerControllerImpl(ViewCSWRecordFactory viewCSWRecordFactory,
                KnownLayerService viewKnownLayerFactory) {
            super(viewKnownLayerFactory, viewCSWRecordFactory);
        }
    }

    private KnownLayerControllerImpl baseController = new KnownLayerControllerImpl(new ViewCSWRecordFactory(), new KnownLayerService(null, null, null, null));


}
