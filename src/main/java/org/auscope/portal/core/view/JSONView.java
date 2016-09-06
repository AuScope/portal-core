package org.auscope.portal.core.view;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

/**
 * User: Mathew Wyatt Date: 23/03/2009 Time: 12:26:29 PM
 *
 * This class is a JSON spring MVC View class which takes a JSONArray and sends the actual json structure down the wire on the httpResponse
 *
 */
public class JSONView extends AbstractView {
    private JSONArray jsonArray;
    private JsonConfig cfg;

    public JSONView() {
        super();
        setContentType("application/json");
        initialiseConfig();
    }

    public JSONView(JSONArray jsonArray) {
        this();
        this.jsonArray = jsonArray;
    }

    private void initialiseConfig() {
        cfg = new JsonConfig();
        cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType(getContentType());
        String content;
        
        if (jsonArray != null) { //convert just the array
            content = JSONSerializer.toJSON(jsonArray, cfg).toString();
        } else { //send of the object
            content = JSONSerializer.toJSON(model, cfg).toString();
        }
        
        response.getWriter().write(content);
    }

}
