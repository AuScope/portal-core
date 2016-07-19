package org.auscope.portal.core.server.security.oauth2;

import java.util.HashMap;
import java.util.Map;

import com.racquettrack.security.oauth.OAuth2ServiceProperties;

/**
 * A specialization of {@link OAuth2ServiceProperties} that prefills its configuration to work with Google OAuth2 for requesting Authentication with Names,
 * Email and ID's
 *
 * This class is otherwise identical to {@link OAuth2ServiceProperties} and is provided as a convenience only.
 *
 * @author Josh Vote
 *
 */
public class GoogleOAuth2ServiceProperties extends OAuth2ServiceProperties {
    /**
     * Create a new properties object with the following deployment specific params
     *
     * @param clientId
     *            The google OAuth2 client ID
     * @param clientSecret
     *            The google OAuth2 client secret
     * @param redirectUri
     *            The URI to receive the access_token from Google. eg: http://localhost:8080/portal/oauth/callback
     */
    public GoogleOAuth2ServiceProperties(String clientId, String clientSecret, String redirectUri) {
        super();

        this.setUserAuthorisationUri("https://accounts.google.com/o/oauth2/auth");
        this.setAccessTokenUri("https://accounts.google.com/o/oauth2/token");

        Map<String, String> additionalAuthParams = new HashMap<>();
        additionalAuthParams.put("scope", "https://www.googleapis.com/auth/userinfo.email");
        this.setAdditionalAuthParams(additionalAuthParams);

        this.setRedirectUri(redirectUri);
        this.setClientId(clientId);
        this.setClientSecret(clientSecret);
        this.setUserInfoUri("https://www.googleapis.com/oauth2/v2/userinfo");
    }
}
