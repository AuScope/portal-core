package org.auscope.portal.core.services.cloud.monitor;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestJobStatusException extends PortalTestClass {

    /**
     * Ensures that getMessage correctly concatenates messages
     */
    @Test
    public void testGetMessage() {
        final Throwable ex1 = new Exception("msg1");
        final Throwable ex2 = new FileNotFoundException("msg2");
        final Throwable ex3 = new NullPointerException("msg3");

        final CloudJob cj1 = new CloudJob(1);
        final CloudJob cj2 = new CloudJob(2);
        final CloudJob cj3 = new CloudJob(3);

        JobStatusException jse = new JobStatusException(Arrays.asList(ex1, ex2, ex3), Arrays.asList(cj1, cj2, cj3));

        String msg = jse.getMessage();
        Assert.assertNotNull(msg);
        Assert.assertTrue(msg.contains("Exception"));
        Assert.assertTrue(msg.contains("FileNotFoundException"));
        Assert.assertTrue(msg.contains("NullPointerException"));

        Assert.assertTrue(msg.contains("msg1"));
        Assert.assertTrue(msg.contains("msg2"));
        Assert.assertTrue(msg.contains("msg3"));
    }

    /**
     * Ensures that toString correctly concatenates messages
     */
    @Test
    public void testToString() {
        final Throwable ex1 = new Exception("msg1");
        final Throwable ex2 = new FileNotFoundException("msg2");
        final Throwable ex3 = new NullPointerException("msg3");

        final CloudJob cj1 = new CloudJob(1);
        final CloudJob cj2 = new CloudJob(2);
        final CloudJob cj3 = new CloudJob(3);

        JobStatusException jse = new JobStatusException(Arrays.asList(ex1, ex2, ex3), Arrays.asList(cj1, cj2, cj3));

        String s = jse.toString();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("Exception"));
        Assert.assertTrue(s.contains("FileNotFoundException"));
        Assert.assertTrue(s.contains("NullPointerException"));

        Assert.assertTrue(s.contains("msg1"));
        Assert.assertTrue(s.contains("msg2"));
        Assert.assertTrue(s.contains("msg3"));
    }
}
