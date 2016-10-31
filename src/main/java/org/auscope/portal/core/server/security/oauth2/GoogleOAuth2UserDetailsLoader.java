package org.auscope.portal.core.server.security.oauth2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

/**
 * A class for loading user details from google OAuth2 authentication.
 *
 * This class does NOT persist any user details. They are created on demand (update is identical to createUser). For a true persistence layer, extend this class
 * and override methods.
 *
 * Additional roles can be configured for select users based on user ID
 *
 * @author Josh Vote
 *
 */
public class GoogleOAuth2UserDetailsLoader implements
OAuth2UserDetailsLoader<PortalUser> {

    protected String defaultRole;
    protected Map<String, List<SimpleGrantedAuthority>> rolesByUser;

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user as a granted authority.
     *
     * @param defaultRole
     */
    public GoogleOAuth2UserDetailsLoader(String defaultRole) {
        this(defaultRole, null);
    }

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user AND any authorities found in rolesByUser if the ID matches the
     * current user ID
     *
     * @param defaultRole
     * @param rolesByUser
     */
    public GoogleOAuth2UserDetailsLoader(String defaultRole, Map<String, List<String>> rolesByUser) {
        this.defaultRole = defaultRole;
        this.rolesByUser = new HashMap<>();

        if (rolesByUser != null) {
            for (Entry<String, List<String>> entry : rolesByUser.entrySet()) {
                List<String> authorityStrings = entry.getValue();
                List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                        authorityStrings.size());
                for (String authority : authorityStrings) {
                    authorities.add(new SimpleGrantedAuthority(authority));
                }

                this.rolesByUser.put(entry.getKey(), authorities);
            }
        }
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
     *
     * @param user
     * @param userInfo
     */
    protected void applyInfoToUser(PortalUser user, Map<String, Object> userInfo) {
        user.setEmail(userInfo.get("email").toString());
        user.setFullName(userInfo.get("name").toString());
    }

    @Override
    public UserDetails createUser(String id, Map<String, Object> userInfo) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(defaultRole));
        if (rolesByUser != null) {
            List<SimpleGrantedAuthority> additionalAuthorities = rolesByUser.get(id);
            if (additionalAuthorities != null) {
                authorities.addAll(additionalAuthorities);
            }
        }

        PortalUser newUser = new PortalUser(id, "", authorities);
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
