package org.auscope.portal.core.test.jmock;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.mail.SimpleMailMessage;

/**
 * A JUnit matcher for matching instances of SimpleMailMessage.
 * 
 * @author Richard Goh
 */
public class SimpleMailMessageMatcher extends TypeSafeMatcher<SimpleMailMessage> {
    private String from;
    private String to;
    private String subject;
    private String text;

    /**
     * Matches a SimpleMailMessage based on one or more its properties.
     * 
     * @param from
     *            If not null, the comparison email sender.
     * @param to
     *            If not null, the comparison email recipient.
     * @param subject
     *            If not null, the comparison email subject.
     * @param text
     *            If not null, the comparison email text.
     */
    public SimpleMailMessageMatcher(String from, String to,
            String subject, String text) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.text = text;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format(
                "a SimpleMailMessage with from='%1$s' to='%2$s' subject='%3$s' text='%4$s'", from, to, subject, text));
    }

    @Override
    public boolean matchesSafely(SimpleMailMessage msg) {
        boolean matches = true;

        if (from != null) {
            matches &= from.equals(msg.getFrom());
        }

        if (to != null) {
            matches &= to.equals(msg.getTo());
        }

        if (subject != null) {
            matches &= subject.equals(msg.getSubject());
        }

        if (text != null) {
            matches &= text.equals(msg.getText());
        }

        return matches;
    }
}