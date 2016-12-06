package org.auscope.portal.core.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.auscope.portal.core.test.jmock.DelayedReturnValueAction;
import org.auscope.portal.core.test.jmock.DelayedThrowAction;
import org.auscope.portal.core.test.jmock.FileWithNameMatcher;
import org.auscope.portal.core.test.jmock.HttpMethodBaseMatcher;
import org.auscope.portal.core.test.jmock.HttpMethodBaseMatcher.HttpMethodType;
import org.auscope.portal.core.test.jmock.MapMatcher;
import org.auscope.portal.core.test.jmock.PortalRuleMockery;
import org.auscope.portal.core.test.jmock.PortalSynchroniser;
import org.auscope.portal.core.test.jmock.PropertiesMatcher;
import org.auscope.portal.core.util.DOMUtil;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
//import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Base class for all unit test classes to inherit from
 *
 * Contains references to the appropriate JMock Mockery instance to utilise
 *
 * @author Josh Vote
 *
 */
//@RunWith(JMock.class)
public abstract class PortalTestClass implements Thread.UncaughtExceptionHandler {

    /** A list of errors arising through the thread uncaught exception handler */
    private List<ExpectationError> expectationErrors;
    /** For use with the timer utility methods */
    private Calendar timerCalendar;

    /**
     * Handles any uncaught ExceptionException's on a seperate thread. Any ExpectationException's are loaded into an internal list and checked during
     * teardownUncaughtExceptionHandler
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof ExpectationError) {
            expectationErrors.add((ExpectationError) e);
        }
    }

    /**
     * Initialises this class as the default uncaught exception handler
     */
    @Before
    public void initialiseUncaughtExceptionHandler() {
        expectationErrors = new ArrayList<>();
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
    @Rule public PortalRuleMockery context = new PortalRuleMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
            setThreadingPolicy(new PortalSynchroniser());
        }
    };

    /**
     * A JMock action similar to returnValue but that only returns AFTER a specified delay
     *
     * It can provide a neat workaround for testing multiple competing threads
     *
     * @param msDelay
     *            The delay in milli seconds to wait
     * @param returnValue
     *            The value to actually return
     * @return
     */
    protected Action delayReturnValue(long msDelay, Object returnValue) {
        return new DelayedReturnValueAction(msDelay, returnValue, context);
    }

    /**
     * A JMock action similar to throwException but that only throws the exception AFTER a specified delay
     *
     * It can provide a neat workaround for testing multiple competing threads
     *
     * @param msDelay
     *            The delay in milli seconds to wait
     * @param throwable
     *            The object to throw
     * @return
     */
    protected Action delayThrowException(long msDelay, Throwable throwable) {
        return new DelayedThrowAction(throwable, msDelay);
    }

    /**
     * A JMock Matcher for testing for a HttpMethodBase matching a few simplified terms
     *
     * @param type
     *            If not null, the type of method to match for
     * @param url
     *            If not null the URL to match for (exact match required)
     * @param postBody
     *            If not null (and a PostMethod) the body of the post to match for (exact match required)
     * @return
     */
    protected HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, String url, String postBody) {
        return new HttpMethodBaseMatcher(type, url, postBody);
    }

    /**
     * A JMock Matcher for testing for a HttpMethodBase matching a few simplified terms
     *
     * @param type
     *            If not null, the type of method to match for
     * @param urlPattern
     *            If not null the URL pattern to match for
     * @param postBodyPattern
     *            If not null (and a PostMethod) the pattern to match against the body of the post.
     * @return
     */
    protected HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, Pattern urlPattern, Pattern postBodyPattern) {
        return new HttpMethodBaseMatcher(type, urlPattern, postBodyPattern);
    }

    /**
     * A JMock Matcher for testing for a java.util.Properties object with a single matching property
     *
     * @param property
     *            The property name
     * @param value
     *            The property value
     * @return
     */
    protected PropertiesMatcher aProperty(String property, String value) {
        return aProperty(property, value, true);
    }

    /**
     * A JMock Matcher for testing for a java.util.Properties object with a matching property
     *
     * @param property
     *            The property name
     * @param value
     *            The property value
     * @param matchExactly
     *            if true, the compared properties must contain ONLY the specified property=value, if false it may also contain other property names
     * @return
     */
    protected PropertiesMatcher aProperty(String property, String value, boolean matchExactly) {
        Properties prop = new Properties();
        prop.setProperty(property, value);
        return new PropertiesMatcher(prop, matchExactly);
    }

    /**
     * A JMock Matcher for testing a Map has every specified value
     *
     * @param map
     *            The values to test for
     * @return
     */
    protected <K, V> MapMatcher<K, V> aMap(Map<K, V> map) {
        return new MapMatcher<>(map);
    }

    /**
     * A JMock Matcher for testing a Map has every specified value
     *
     * @param keys
     *            The Keys to match for (must correspond 1:1 with values
     * @param values
     *            The Values to match for (must correspond 1:1 with keys
     * @return
     */
    protected <K, V> MapMatcher<K, V> aMap(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException();
        }

        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return aMap(map);
    }

    /**
     * Starts an inbuilt timer - to get the elapsed time call endTimer(). Subsequent calls to this function will reset the timer
     */
    protected void startTimer() {
        timerCalendar = Calendar.getInstance();
    }

    /**
     * Gets the elapsed time since startTimer() was called (in milli seconds).
     *
     * @return
     */
    protected long endTimer() {
        if (timerCalendar == null) {
            return -1;
        }
        return (Calendar.getInstance().getTimeInMillis() - timerCalendar.getTimeInMillis());
    }

    /**
     * Utility function for opening a system resource and parsing it into a string.
     *
     * Returns null if the resource cannot be opened
     *
     * @param resource
     */
    protected String getSystemResourceAsString(String resource) {
        InputStream is = ClassLoader.getSystemResourceAsStream(resource);
        if (is == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer contents = new StringBuffer();

        try {
            String str;
            while ((str = reader.readLine()) != null) {
                contents.append(str);
            }
            reader.close();
        } catch (IOException ex) {
            return null;
        }

        return contents.toString();
    }

    /**
     * Tests equality of two objects based on their 'equals' comparison AND a comparison between their hashcodes. True is only returned IFF both parameters are
     * null or have matching equals + hashCode results.
     *
     * Quote from equals Javadoc: Note that it is generally necessary to override the hashCode method whenever this method is overridden, so as to maintain the
     * general contract for the hashCode method, which states that equal objects must have equal hash codes.
     *
     * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Object.html#equals%28java.lang.Object%29
     * @param o1
     *            The first object to compare
     * @param o2
     *            The second object to compare
     */
    protected boolean equalsWithHashcode(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return o1 == o2;
        }

        if (!o1.equals(o2)) {
            return false;
        }

        return o1.hashCode() == o2.hashCode();
    }

    /**
     * A JMock matcher for matching a File with a specific name
     *
     * @param fileName
     *            The name of the file to match
     * @return
     */
    protected FileWithNameMatcher aFileWithName(String fileName) {
        return new FileWithNameMatcher(fileName);
    }

    /**
     * Compares two strings by parsing them into XML and ensuring all elements/attributes match.
     * @param xml1
     * @param xml2
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    protected boolean xmlStringEquals(String xml1, String xml2, boolean namespaceAware) throws ParserConfigurationException, IOException, SAXException {
        Document d1 = DOMUtil.buildDomFromString(xml1, namespaceAware);
        Document d2 = DOMUtil.buildDomFromString(xml2, namespaceAware);

        Diff diff = XMLUnit.compareXML(d1, d2);
        return diff.identical();
    }
}
