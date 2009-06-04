package org.auscope.portal.server.web.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.PrintWriter;

import net.sf.json.JSONSerializer;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 12:26:29 PM
 *
 * This class is a JSON spring MVC View class which takes a JSONArray and sends the actual json structure down the
 * wire on the httpResponse
 * 
 */
public class JSONView extends AbstractView {

    public JSONView() {
        super();
        setContentType("application/json");
    }

    protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());

        //send it off
        response.getWriter().write(JSONSerializer.toJSON(model).toString());
    }

}
