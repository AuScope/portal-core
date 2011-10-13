package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.server.web.KnownLayer;
import org.auscope.portal.server.web.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Contains methods for requesting the list of known feature types
 * @author vot002
 *
 */
@Controller
public class KnownLayerController extends BasePortalController {

    private List knownTypes;
    private ViewKnownLayerFactory viewFactory;

    @Autowired
    public KnownLayerController(@Qualifier("knownTypes") ArrayList knownTypes,
            ViewKnownLayerFactory viewFactory) {
        this.knownTypes = knownTypes;
        this.viewFactory = viewFactory;

    }

    /**
     * Gets a JSON response which contains the representations of each and every "KnownFeatureTypeDefinition".
     *
     * Each KnownFeatureTypeDefinition will map [0, N] CSWRecords with display information.
     * @return
     */
    @RequestMapping("getKnownLayers.do")
    public ModelAndView getKnownLayers() {
        List<ModelMap> viewRepresentations = new ArrayList<ModelMap>();
        for (Object k : knownTypes) {
            viewRepresentations.add(viewFactory.toView((KnownLayer) k));
        }

        return generateJSONResponseMAV(true, viewRepresentations, "");
    }
}
