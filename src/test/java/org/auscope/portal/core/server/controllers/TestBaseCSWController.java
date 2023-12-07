package org.auscope.portal.core.server.controllers;

import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;

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

    private KnownLayerControllerImpl baseController = new KnownLayerControllerImpl(new ViewCSWRecordFactory(), new KnownLayerService(null, null, null, null, null, null));

}
