package org.auscope.portal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.auscope.portal.HttpMethodBaseMatcher.HttpMethodType;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base class for all unit test classes to inherit from
 *
 * Contains references to the appropriate JMock Mockery instance to utilise
 *
 * @author Josh Vote
 *
 */
@RunWith(JMock.class)
public abstract class PortalTestClass implements Thread.UncaughtExceptionHandler {

    /** A list of errors arising through the thread uncaught exception handler*/
    private List<ExpectationError> expectationErrors;
    /** For use with the timer utility methods*/
    private Calendar timerCalendar;

    /**
     * Handles any uncaught ExceptionException's on a seperate thread. Any ExpectationException's are loaded
     * into an internal list and checked during teardownUncaughtExceptionHandler
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof ExpectationError) {
            expectationErrors.add((ExpectationError)e);
        }
    }

    /**
     * Initialises this class as the default uncaught exception handler
     */
    @Before
    public void initialiseUncaughtExceptionHandler() {
        expectationErrors = new ArrayList<ExpectationError>();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * This is to ensure that no expectation errors on seperate threads go unnoticed
     */
    @After
    public void teardownUncaughtExceptionHandler() {
        if (!expectationErrors.isEmpty()) {
            Assert.fail(expectationErrors.get(0).toString());
        }
    }

    /**
     * used for generating/testing mock objects and their expectations
     */
    protected Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * A JMock action similar to returnValue but that only returns AFTER a specified delay
     *
     * It can provide a neat workaround for testing multiple competing threads
     * @param msDelay The delay in milli seconds to wait
     * @param returnValue The value to actually return
     * @return
     * @throws Exception
     */
    protected Action delayReturnValue(long msDelay, Object returnValue) throws Exception {
        return new DelayedReturnValueAction(msDelay, returnValue);
    }

    /**
     * A JMock Matcher for testing for a HttpMethodBase matching a few simplified terms
     * @param type If not null, the type of method to match for
     * @param url If not null the URL to match for (exact match required)
     * @param postBody If not null (and a PostMethod) the body of the post to match for (exact match required)
     * @return
     */
    protected HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, String url, String postBody) {
        return new HttpMethodBaseMatcher(type, url, postBody);
    }

    /**
     * A JMock Matcher for testing for a HttpMethodBase matching a few simplified terms
     * @param type If not null, the type of method to match for
     * @param urlPattern If not null the URL pattern to match for
     * @param postBodyPattern If not null (and a PostMethod) the pattern to match against the body of the post.
     * @return
     */
    protected HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, Pattern urlPattern, Pattern postBodyPattern) {
        return new HttpMethodBaseMatcher(type, urlPattern, postBodyPattern);
    }

    /**
     * A JMock Matcher for testing for a java.util.Properties object with a single matching property
     * @param property The property name
     * @param value The property value
     * @return
     */
    protected PropertiesMatcher aProperty(String property, String value) {
        Properties prop = new Properties();
        prop.setProperty(property, value);
        return new PropertiesMatcher(prop);
    }

    /**
     * Starts an inbuilt timer - to get the elapsed time call endTimer(). Subsequent calls to this function
     * will reset the timer
     */
    protected void startTimer() {
        timerCalendar = Calendar.getInstance();
    }

    /**
     * Gets the elapsed time since startTimer() was called (in milli seconds).
     * @return
     */
    protected long endTimer() {
        if (timerCalendar == null) {
            return -1;
        }
        return (Calendar.getInstance().getTimeInMillis() - timerCalendar.getTimeInMillis());
    }
}
