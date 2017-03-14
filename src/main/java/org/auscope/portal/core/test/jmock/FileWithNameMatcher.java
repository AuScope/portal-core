package org.auscope.portal.core.test.jmock;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A custom JMock matcher for ensuring a file with a specific name (not including path) is matched.
 * 
 * @author Josh Vote
 *
 */
public class FileWithNameMatcher extends TypeSafeMatcher<File> {

    private String fileName;

    /**
     * Creates a matcher that will match any file with the given fileName (not including path)
     * 
     * @param fileName
     */
    public FileWithNameMatcher(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean matchesSafely(File file) {
        if (file == null) {
            return false;
        }

        return file.getName().equals(fileName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a file with name '%1$s'", fileName));
    }
}