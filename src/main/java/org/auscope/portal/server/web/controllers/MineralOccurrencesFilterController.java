package org.auscope.portal.server.web.controllers;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.auscope.portal.server.web.view.JSONView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

/**
 * User: Mathew Wyatt
 * Date: 20/03/2009
 * Time: 2:26:21 PM
 */

@Controller
public class MineralOccurrencesFilterController {

    @RequestMapping("/populateFilterPanel.do")
    public void populateFilterPanel(ModelMap model) {
        System.out.println("made it");
    }

    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            HashMap model,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        System.out.println("service: " + serviceUrl);
        System.out.println("mine: " + mineName);
        
        model.put("success", true);

        JSONArray dataArray = new JSONArray();
        Map<String, Serializable> data = new HashMap<String, Serializable>();
        data.put("kmlblob", "somekmlhere");
        dataArray.add(data);

        model.put("data", dataArray);

        Map<String, HashMap> jsonViewModel = new HashMap<String, HashMap>();
        jsonViewModel.put("JSON_OBJECT", model);

        return new ModelAndView(new JSONView(), jsonViewModel);
    }

    private String getMineQueryFilter() {
        return null;
    }

    public String convertToKML(String mineResponse, String miningActivityResponse) {

        return null;
    }

}
