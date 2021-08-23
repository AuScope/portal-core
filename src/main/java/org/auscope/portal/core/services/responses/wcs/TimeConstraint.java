package org.auscope.portal.core.services.responses.wcs;

import java.time.Instant;

/**
 * Represents the time constraint on a WCS GetCoverage request
 *
 * @author Josh Vote
 *
 */
public class TimeConstraint {

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
     * Parses a TimeConstraint for a set of discrete times into ISO8601 UTC strings
     * 
     * @param timePositions
     *            A list of time positions to query for. 
     * @return
     * @throws ParseException
     */
    public static TimeConstraint parseTimeConstraint(final Instant[] timePositions) {
        StringBuilder sb = new StringBuilder();

        for (Instant d : timePositions) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(d.toString());
        }

        return new TimeConstraint(sb.toString());
    }

    /**
     * Parses a TimeConstraint for a range of times (at an optional resolution) into an ISO8601 UTC string
     * 
     * @param from
     *            Inclusive start time
     * @param to
     *            Inclusive end time
     * @param timePeriodResolution
     *            [Optional] A time range resolution (see WCS spec)
     * @return
     */
    public static TimeConstraint parseTimeConstraint(final Instant from, final Instant to, final String timePeriodResolution) {
        // Outputs strings in ISO8601 UTC format
        String timeString = String.format("%1$s/%2$s", from.toString(), to.toString());
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
