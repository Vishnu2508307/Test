package com.smartsparrow.rest.util;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.apache.commons.lang3.StringUtils;

import com.smartsparrow.iam.lang.AuthenticationNotSupportedFault;
import com.smartsparrow.iam.service.BronteWebToken;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.iam.service.WebTokenType;

public class Cookies {

    private static final String name = "bearerToken";
    private static final String path = "/";
    private static final String domain = ".phx-spr.com";
    private static final String comment = null;
    private static final boolean secure = true;

    /**
     * Map a WebSessionToken to a cookie.
     *
     * @param webSessionToken the web session token
     * @return a cookie which represents the supplied web session token
     */
    public static NewCookie map(final WebSessionToken webSessionToken) {
        affirmNotNull(webSessionToken, "missing required session token");
        //
        return new NewCookie(name, //
                             webSessionToken.getToken(), //
                             path, //
                             domain, //
                             comment, //
                             (int) ((webSessionToken.getValidUntilTs() - System.currentTimeMillis()) / 1000), //
                             secure,true);
    }

    /**
     * Map the existing cookie to one which will remove it from the browser
     *
     * @param existingBearerTokenCookie the existing cookie
     * @return a cookie which will remove the supplied one from the browser
     */
    public static NewCookie mapToRemove(final Cookie existingBearerTokenCookie) {
        affirmNotNull(existingBearerTokenCookie, "missing required existing cookie");
        // Remove the cookie by:
        //  - setting the value to empty string
        //  - setting the max-age of it to 0.
        return new NewCookie(existingBearerTokenCookie.getName(), //
                             StringUtils.EMPTY, //
                             path, //
                             domain, //
                             comment, //
                             0, //
                             secure,true);
    }

    /**
     * Map a BronteWebToken to a cookie
     *
     * @param bronteWebToken the Bronte web token to map
     * @return a cookie which represents the supplied Bronte web token
     */
    public static NewCookie map(final BronteWebToken bronteWebToken) {
        affirmNotNull(bronteWebToken, "missing required session token");
        //
        return new NewCookie(name, //
                bronteWebToken.getToken(), //
                path, //
                domain, //
                comment, //
                (int) ((bronteWebToken.getValidUntilTs() - System.currentTimeMillis()) / 1000), //
                secure,true);
    }

    /**
     * Map a WebToken to a cookie.
     *
     * @param webToken the web token to map
     * @return a cookie which represents the supplied web token
     * @throws AuthenticationNotSupportedFault when the WebToken supplied is not a {@link BronteWebToken} type
     */
    public static NewCookie map(final WebToken webToken) {
        if (webToken.getWebTokenType() == WebTokenType.BRONTE) {
            return map((BronteWebToken) webToken);
        }
        throw new AuthenticationNotSupportedFault(String.format("cannot map %s to cookie", webToken.getWebTokenType()));
    }
}
