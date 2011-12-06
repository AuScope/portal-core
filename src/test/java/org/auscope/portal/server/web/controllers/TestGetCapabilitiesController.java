package org.auscope.portal.server.web.controllers;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.web.service.GetCapabilitiesService;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestGetCapabilitiesController extends PortalTestClass {

    private GetCapabilitiesService getCapabilitiesService;
    private GetCapabilitiesController controller;

    @Before
    public void setUp(){
        getCapabilitiesService=context.mock(GetCapabilitiesService.class);
        ViewCSWRecordFactory viewFactory=new ViewCSWRecordFactory();
        controller=new GetCapabilitiesController(getCapabilitiesService,viewFactory);
    }

    @Test
    public void testGetCustomLayers() throws Exception{
        //GetCapabilititesControllerWMSResponse.xml
        final String serviceUrl="http://example.com";
        InputStream is=this.getClass().getResourceAsStream("/GetCapabilitiesControllerWMSResponse_1_1_1.xml");
        try{
            final GetCapabilitiesRecord record=new GetCapabilitiesRecord(is);

            context.checking(new Expectations() {{
                oneOf(getCapabilitiesService).getWmsCapabilities(serviceUrl);will(returnValue(record));
            }});

            Assert.assertNotNull(is);
            ModelAndView mv=controller.getCustomLayers(serviceUrl);
            Assert.assertNotNull(mv);
            List ls=(List) mv.getModelMap().get("data");
            Assert.assertEquals(21,ls.size());
            ModelMap mm=(ModelMap) ls.get(0);
            Assert.assertTrue(mm.get("contactOrganisation").equals("Test organization"));
        }finally{
            try{
                is.close();
            }catch(IOException e){
                //Not important if the stream can't be closed in unit test
                e.printStackTrace();
            }
        }
    }
}
