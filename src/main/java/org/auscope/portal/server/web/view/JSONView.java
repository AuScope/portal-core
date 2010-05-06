package org.auscope.portal.server.web.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import net.sf.json.JSONSerializer;
import net.sf.json.JSONArray;

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
    private JSONArray jsonArray;

    public JSONView() {
        super();
        setContentType("application/json");
    }

    public JSONView(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());

        if(jsonArray != null) { //convert just the array
            response.getWriter().write(JSONSerializer.toJSON(jsonArray).toString());
        } else { //send of the object
            response.getWriter().write(JSONSerializer.toJSON(model).toString());
        }
    }

}
