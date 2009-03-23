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
        //String testRunner = "file:///" + basePath + "/src/test/resources/jsmock/test/testRunner.html";
        String testRunner = "file:///" + basePath + "/src/test/js/testRunner.html";
        String testSuite  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/test/js/portalTestSuiteJSUnit.html")).getAbsolutePath();
        //String testSuite  = new File(new URI("file:///" + basePath.replace("\\", "/") + "/src/test/resources/jsmock/test/portalTestSuiteJSUnit.html")).getAbsolutePath();
        return testRunner+"?testPage="+testSuite+"&autoRun=true&submitresults=true";
    }

    private String getBrowserPaths() {
        //if on linux look for firefox
        if(System.getProperty("os.name").equals("Linux"))
            return getLinuxBrowsers();
        //if on windows look for ie
        else
            return getWindowsBrowsers();
    }

    private String getWindowsBrowsers() {
        return "C:\\Program Files\\Mozilla Firefox\\firefox.exe,C:\\Program Files\\Internet Explorer\\iexplore.exe";
    }

    private String getLinuxBrowsers() {
        return "/usr/bin/firefox";
    }
}
