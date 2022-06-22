package org.auscope.portal.core.server.controllers;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.services.admin.StateService;


public class TestUIStateController extends PortalTestClass {

    private StateService service;
    private UIStateController controller;

    @Before
    public void setUp() {
        service = context.mock(StateService.class);
        controller = new UIStateController(service);
    }

    @Test
    public void testSaveState() throws Exception {
        final String id = "1";
        final String state = "ABCDEF";

        context.checking(new Expectations() {
            {
                oneOf(service).save(id, state);
                will(returnValue(true));
            }
        });

        ModelAndView modelAndView = controller.saveUIState(id, state);
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
    }

    @Test
    public void testSaveStateFail() throws Exception {
        final String id = "1";
        final String state = "ABCDEF";

        context.checking(new Expectations() {
            {
                oneOf(service).save(id, state);
                will(returnValue(false));
            }
        });

        ModelAndView modelAndView = controller.saveUIState(id, state);
        Assert.assertFalse((Boolean) modelAndView.getModel().get("success"));
    }

    @Test
    public void testSaveStateException() throws Exception {
        final String id = "1";
        final String state = "ABCDEF";

        context.checking(new Expectations() {
            {
                oneOf(service).save(id, state);
                will(throwException(new Exception()));
            }
        });

        ModelAndView modelAndView = controller.saveUIState(id, state);
        Assert.assertFalse((Boolean) modelAndView.getModel().get("success"));
    }

    @Test
    public void testFetchState() throws Exception {
        final String id = "1";
        final String state = "ABCDEF";

        context.checking(new Expectations() {
            {
                oneOf(service).fetch(id);
                will(returnValue(state));
            }
        });
        ModelAndView modelAndView = controller.fetchUIState(id);
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertEquals(modelAndView.getModel().get("data"), state);
        Assert.assertEquals(modelAndView.getModel().get("msg"), "State");
    }

    @Test
    public void testFetchStateEmpty() throws Exception {
        final String id = "1";
        final String state = "";

        context.checking(new Expectations() {
            {
                oneOf(service).fetch(id);
                will(returnValue(state));
            }
        });
        ModelAndView modelAndView = controller.fetchUIState(id);
        Assert.assertTrue((Boolean) modelAndView.getModel().get("success"));
        Assert.assertEquals(modelAndView.getModel().get("data"), state);
        Assert.assertEquals(modelAndView.getModel().get("msg"), "State");
    }

    @Test
    public void testFetchStateException() throws Exception {
        final String id = "1";

        context.checking(new Expectations() {
            {
                oneOf(service).fetch(id);
                will(throwException(new Exception()));
            }
        });
        ModelAndView modelAndView = controller.fetchUIState(id);
        Assert.assertFalse((Boolean) modelAndView.getModel().get("success"));
    }
}