package com.smartsparrow.sso.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.smartsparrow.iam.service.AccountRole;

public class LTIRolesConverter {

    private static final Logger log = LoggerFactory.getLogger(LTIRolesConverter.class);

    private static final String SYSTEM_PREFIX = "urn:lti:sysrole:ims/lis/";
    private static final String INSTITUTION_PREFIX = "urn:lti:instrole:ims/lis/";
    private static final String CONTEXT_PREFIX = "urn:lti:role:ims/lis/";

    private static final HashMap<String, Set<AccountRole>> mapping = Maps.newHashMap();

    static {
        map(SYSTEM_PREFIX, "SysAdmin", AccountRole.ADMIN);
        map(SYSTEM_PREFIX, "SysSupport", AccountRole.ADMIN);
        map(SYSTEM_PREFIX, "Creator", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(SYSTEM_PREFIX, "AccountAdmin", AccountRole.ADMIN);
        map(SYSTEM_PREFIX, "Administrator", AccountRole.ADMIN);

        map(INSTITUTION_PREFIX, "Faculty", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(INSTITUTION_PREFIX, "Member", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(INSTITUTION_PREFIX, "Instructor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(INSTITUTION_PREFIX, "Mentor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(INSTITUTION_PREFIX, "Staff", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(INSTITUTION_PREFIX, "Administrator", AccountRole.ADMIN);

        map(CONTEXT_PREFIX, "Instructor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "ContentDeveloper", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "Member", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "Manager", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "Mentor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "Administrator", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map(CONTEXT_PREFIX, "TeachingAssistant", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);

        //context roles without prefix
        map("Instructor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("ContentDeveloper", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("Member", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("Manager", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("Mentor", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("Administrator", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
        map("TeachingAssistant", AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR);
    }

    /**
     * Converts LTI user roles to IAM roles.
     * {@link AccountRole#STUDENT} (or {@link AccountRole#STUDENT_GUEST}) is always added by default.
     * @param ltiRoles a list of supplied LTI roles
     * @return set of {@link AccountRole}. It always contain at least one role.
     */
    public Set<AccountRole> convertToIamRoles(String... ltiRoles) {
        //
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.STUDENT);
        if (ltiRoles == null || ltiRoles.length == 0) {
            log.warn("No roles were provided in LTI request");
            return roles;
        }

        Arrays.stream(ltiRoles)
                .map(this::mapLtiRole)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(() -> roles));

        return roles;
    }

    Set<AccountRole> mapLtiRole(String fullUrn) {
        //remove sub-role for Context Roles
        if (fullUrn.startsWith(CONTEXT_PREFIX) && fullUrn.substring(CONTEXT_PREFIX.length()).contains("/")) {
            fullUrn = fullUrn.substring(0, fullUrn.lastIndexOf("/"));
        }
        return mapping.get(fullUrn);
    }

    private static void map(String prefix, String handle, AccountRole... roles) {
        mapping.put(prefix + handle, Sets.newHashSet(roles));
    }

    private static void map(String handle, AccountRole... roles) {
        mapping.put(handle, Sets.newHashSet(roles));
    }

}
