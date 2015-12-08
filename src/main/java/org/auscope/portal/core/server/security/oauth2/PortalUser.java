package org.auscope.portal.core.server.security.oauth2;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * An extension of UserDetails that also adds fields for full name and email addresses
 * 
 * @author Josh Vote
 *
 */
public class PortalUser extends User {

    private String fullName;
    private String email;

    /**
     * Calls the more complex constructor with all boolean arguments set to {@code true}.
     */
    public PortalUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, true, authorities);
    }

    /**
     * Construct the <code>User</code> with the details required by {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
     *
     * @param username
     *            the username presented to the <code>DaoAuthenticationProvider</code>
     * @param password
     *            the password that should be presented to the <code>DaoAuthenticationProvider</code>
     * @param enabled
     *            set to <code>true</code> if the user is enabled
     * @param accountNonExpired
     *            set to <code>true</code> if the account has not expired
     * @param credentialsNonExpired
     *            set to <code>true</code> if the credentials have not expired
     * @param accountNonLocked
     *            set to <code>true</code> if the account is not locked
     * @param authorities
     *            the authorities that should be granted to the caller if they presented the correct username and password and the user is enabled. Not null.
     *
     * @throws IllegalArgumentException
     *             if a <code>null</code> value was passed either as a parameter or as an element in the <code>GrantedAuthority</code> collection
     */
    public PortalUser(String username, String password, boolean enabled, boolean accountNonExpired,
            boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    /**
     * Gets a string representing the full name of the user
     * 
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets a string representing the full name of the user
     * 
     * @param fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets a contact email for this user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets a contact email for this user
     * 
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "PortalUser [fullName=" + fullName + ", email=" + email + "]";
    }
}
