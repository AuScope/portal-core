package org.auscope.portal.server.web.view;

import net.sf.json.JSONArray;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * A spring ModelAndView for json objects
 *
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 4, 2009
 * Time: 12:06:14 PM
 */
public class JSONModelAndView extends ModelAndView {

    public JSONModelAndView(ModelMap model) {
        super(new JSONView(), model);
    }

    public JSONModelAndView(JSONArray jsonArray) {
        super(new JSONView(jsonArray), new ModelMap());
    }



}
