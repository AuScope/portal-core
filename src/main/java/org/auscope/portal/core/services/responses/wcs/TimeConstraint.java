package org.auscope.portal.core.services.responses.wcs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents the time constraint on a WCS GetCoverage request
 *
 * @author Josh Vote
 *
 */
public class TimeConstraint {

    /** The date format used by this constraint */
    private static final DateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        OUTPUT_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** The constraint value */
    private String constraint;

    /**
     * Creates a new constraint (doesn't check for validity)
     *
     * @param constraint
     *            The constraint value
     */
    public TimeConstraint(String constraint) {
        this.constraint = constraint;
    }

    /**
     * Gets the constraint value
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * Parses a TimeConstraint for a set of discrete times
     * 
     * @param timePositions
     *            A list of time positions to query for.
     * @return
     * @throws ParseException
     */
    public static TimeConstraint parseTimeConstraint(final Date[] timePositions) {
        StringBuilder sb = new StringBuilder();

        for (Date d : timePositions) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(OUTPUT_FORMAT.format(d));
        }

        return new TimeConstraint(sb.toString());
    }

    /**
     * Parses a TimeConstraint for a range of time (at an optional resolution)
     * 
     * @param from
     *            Inclusive start time
     * @param to
     *            Inclusive end time
     * @param timePeriodResolution
     *            [Optional] A time range resolution (see WCS spec)
     * @return
     */
    public static TimeConstraint parseTimeConstraint(final Date from, final Date to, final String timePeriodResolution) {
        String timeString = String.format("%1$s/%2$s", OUTPUT_FORMAT.format(from), OUTPUT_FORMAT.format(to));
        if (timePeriodResolution != null && !timePeriodResolution.isEmpty()) {
            timeString += String.format("/%1$s", timePeriodResolution);
        }

        return new TimeConstraint(timeString);
    }

    /**
     * Tests equality based on time constraint string values
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeConstraint) {
            return this.constraint.equals(((TimeConstraint) o).constraint);
        } else {
            return this.constraint.equals(o);
        }
    }

    /**
     * Generates a hashcode from the internal constraint value
     */
    @Override
    public int hashCode() {
        return this.constraint.hashCode();
    }

    /**
     * Prints the contents of this TimeConstraint
     */
    @Override
    public String toString() {
        return "TimeConstraint [constraint=" + constraint + "]";
    }

}
