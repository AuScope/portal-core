import net.jsunit.StandaloneTest;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: Mathew Wyatt
 * Date: 18/03/2009
 * Time: 1:03:03 PM
 *
 * This class is used to run the JSunit tests when the test classes are invoked
 *
 */
public class JSUnitTestRunner extends StandaloneTest {

    public JSUnitTestRunner(String name) throws URISyntaxException {
        super(name);

        System.setProperty("browserFileNames", getBrowserPaths());
        System.setProperty("url", getUrl());
    }

    private String getUrl() throws URISyntaxException {
        String basePath = new File("").getAbsolutePath();
        String testRunner = "file:///" + basePath + "/src/test/resources/jsmock/test/testRunner.html";
        String testSuite  = new File(new URI("file:///" + basePath + "/src/test/resources/jsmock/test/AllTests.html")).getAbsolutePath();
        return testRunner+"?testPage="+testSuite+"&autoRun=true&submitresults=true";
    }

    private String getBrowserPaths() {
        //if on linux look for firefox
        return getLinuxBrowsers();

        //TODO: if on windows look for ie
    }

    //TODO: implement
    private String getWindowsBrowsers() {
        return null;
    }

    private String getLinuxBrowsers() {
        return "/usr/bin/firefox";
    }
}
