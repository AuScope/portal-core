package org.auscope.portal.core.services.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a response from the Admin service
 * 
 * @author Josh Vote
 *
 */
public class AdminDiagnosticResponse implements Serializable {
    /** Any useful pieces of info relating to the operation */
    private List<String> details;
    /** Any (non critical) problems that occurred during the operation */
    private List<String> warnings;
    /** Any critical problems that occurred during the operation */
    private List<String> errors;

    /**
     * Creates a new instance with no values
     */
    public AdminDiagnosticResponse() {
        this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
    }

    /**
     * Creates a new instance with the specified info
     * 
     * @param details
     *            Any useful pieces of info relating to the operation
     * @param warnings
     *            Any (non critical) problems that occurred during the operation
     * @param errors
     *            Any critical problems that occurred during the operation
     */
    public AdminDiagnosticResponse(List<String> details,
            List<String> warnings, List<String> errors) {
        this.details = details;
        this.warnings = warnings;
        this.errors = errors;
    }

    /**
     * Is this operation a success?
     * 
     * @return
     */
    public boolean isSuccess() {
        return this.errors.size() == 0;
    }

    /**
     * Gets an unmodifiable list of all useful pieces of info relating to this operation
     * 
     * @return
     */
    public List<String> getDetails() {
        return Collections.unmodifiableList(details);
    }

    /**
     * Gets an unmodifiable list of all (non critical) problems that occurred during the operation
     * 
     * @return
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Gets an unmodifiable list of all critical problems that occurred during the operation
     * 
     * @return
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Adds a detail to this response
     * 
     * @param detail
     *            useful pieces of info relating to this operation
     */
    public void addDetail(String detail) {
        this.details.add(detail);
    }

    /**
     * Adds a warning to this response
     * 
     * @param warning
     *            a (non critical) problem that occurred during the operation
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Adds a error to this response
     * 
     * @param error
     *            a error that occurred during the operation
     */
    public void addError(String error) {
        this.errors.add(error);
    }

}
