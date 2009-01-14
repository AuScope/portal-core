package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

/**
 * User: Mathew Wyatt
 * Date: 12/01/2009
 * Time: 2:01:42 PM
 */
public class GetDataSourcesJSONController extends AbstractController {

   protected final Log logger = LogFactory.getLog(getClass());
    private Enumeration params;

    @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        logger.info("started!");

        params = request.getParameterNames();

        while(params.hasMoreElements()) {
            String element = (String)params.nextElement();
            System.out.println( element + " " + request.getParameter(element));
        }

        JSONArray jsonArray = new JSONArray();

        Map node = new HashMap();
        node.put("id", "something" + System.currentTimeMillis());
        node.put("text", "somehitng");
        node.put("checked", Boolean.FALSE);
        node.put("leaf", Boolean.FALSE);

        jsonArray.add(node);

        Map model = new HashMap();
        model.put("JSON_OBJECT", jsonArray);

        //return new ModelAndView("jsonView", model);
        return new ModelAndView(new Something(), model);
   }
}


class Something extends AbstractView {

    public Something() {
        super();
        setContentType("application/json");    
    }

    protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        response.getWriter().write(JSONSerializer.toJSON(model.get("JSON_OBJECT")).toString());
    }

}