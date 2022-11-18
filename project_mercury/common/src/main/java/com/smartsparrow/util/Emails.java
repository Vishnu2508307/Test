package com.smartsparrow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.net.InternetDomainName;

public class Emails {

    private static final Logger log = LoggerFactory.getLogger(Emails.class);

    public static boolean isNotValid(String email) {
        return !isEmailValid(email);
    }

    public static String normalize(String email) {
        return email != null ? email.trim().toLowerCase() : null;

    }

    /**
     * Leniently validate if an email address is valid or not. This is not a VERIFY operation.
     *
     * @param email the email address to validate
     * @return true if email address is valid, false otherwise.
     */
    public static boolean isEmailValid(final String email) {
        String _email = Emails.normalize(email);

        if (Strings.isNullOrEmpty(_email)) {
            if (log.isDebugEnabled()) {
                log.debug("Not an email (null/empty): '" + _email + "'");
            }
            return false;
        }

        int indexOfAt = _email.indexOf('@');
        // no @ ? ignore it, skip.
        if (indexOfAt == -1) {
            if (log.isDebugEnabled()) {
                log.debug("Not an email (missing @): '" + _email + "'");
            }
            return false;
        }

        // ensure no spaces.
        if (_email.contains(" ")) {
            if (log.isDebugEnabled()) {
                log.debug("Not an email (has spaces): '" + _email + "'");
            }
            return false;
        }

        // parse the email to get the FQDN
        String fqdn = _email.substring(indexOfAt + 1);

        // Indicates whether the argument is a syntactically valid domain name using
        // lenient validation. Specifically, validation against http://www.ietf.org/rfc/rfc3490.txt - RFC 3490
        // ("Internationalizing Domain Names in Applications") is skipped.
        // also see InternetDomainName.isValid()
        InternetDomainName.from(fqdn);
        return true;
    }
}
