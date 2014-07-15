package org.auscope.portal.core.server.security.oauth2;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

/**
 * A class for loading user details from google OAuth2 authentication.
 *
 * This class does NOT persist any user details. They are created on demand (update
 * is identical to createUser). For a true persistence layer, extend this class
 * and override methods.
 *
 * @author Josh Vote
 *
 */
public class GoogleOAuth2UserDetailsLoader implements
        OAuth2UserDetailsLoader<PortalUser> {

    private String defaultRole;

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user
     * as a granted authority.
     * @param defaultRole
     */
    public GoogleOAuth2UserDetailsLoader(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    /**
     * Always returns null - users will always need to be created
     */
    @Override
    public PortalUser getUserByUserId(String id) {
        return null;
    }

    @Override
    public boolean isCreatable(Map<String, Object> userInfo) {
        return userInfo.containsKey("id");
    }

    /**
     * Extracts keys from userInfo and applies them to appropriate properties in user
     * @param user
     * @param userInfo
     */
    protected void applyInfoToUser(PortalUser user,  Map<String, Object> userInfo) {
        user.setEmail(userInfo.get("email").toString());
        user.setFullName(userInfo.get("name").toString());
    }

    @Override
    public UserDetails createUser(String id, Map<String, Object> userInfo) {
        PortalUser newUser = new PortalUser(id, "", Collections.singleton(new SimpleGrantedAuthority(defaultRole)));

        applyInfoToUser(newUser, userInfo);

        return newUser;
    }

    @Override
    public UserDetails updateUser(UserDetails userDetails,
            Map<String, Object> userInfo) {

        if (userDetails instanceof PortalUser) {
            applyInfoToUser((PortalUser) userDetails, userInfo);
        }

        return userDetails;
    }

}
