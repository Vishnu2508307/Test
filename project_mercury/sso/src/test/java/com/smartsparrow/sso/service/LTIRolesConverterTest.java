package com.smartsparrow.sso.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;
import com.smartsparrow.iam.service.AccountRole;

class LTIRolesConverterTest {

    private LTIRolesConverter ltiRolesMapper = new LTIRolesConverter();
    private final Set<AccountRole> instructorRoles = Sets.newHashSet(AccountRole.AERO_INSTRUCTOR, AccountRole.INSTRUCTOR);

    @Test
    void mapLtiRole_ContextRole() {
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Instructor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/ContentDeveloper"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Member"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Manager"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Mentor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Administrator"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/TeachingAssistant"));
    }

    @Test
    void mapLtiRole_ContextRoleWithoutPrefix() {
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("Instructor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("ContentDeveloper"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("Member"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("Manager"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("Mentor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("Administrator"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("TeachingAssistant"));
    }

    @Test
    void mapLtiRole_ContextRoleWithSubRole() {
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/Instructor/PrimaryInstructor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantSection"));
    }

    @Test
    void mapLtiRole_SystemRole() {
        assertEquals(Sets.newHashSet(AccountRole.ADMIN), ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/SysAdmin"));
        assertEquals(Sets.newHashSet(AccountRole.ADMIN), ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/SysSupport"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/Creator"));
        assertEquals(Sets.newHashSet(AccountRole.ADMIN), ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/AccountAdmin"));
        assertNull(ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/User"));
        assertEquals(Sets.newHashSet(AccountRole.ADMIN), ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/Administrator"));
        assertNull(ltiRolesMapper.mapLtiRole("urn:lti:sysrole:ims/lis/None"));
    }

    @Test
    void mapLtiRole_InstitutionRole() {
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Faculty"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Member"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Instructor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Mentor"));
        assertEquals(instructorRoles, ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Staff"));
        assertNull(ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Alumni"));
        assertEquals(Sets.newHashSet(AccountRole.ADMIN), ltiRolesMapper.mapLtiRole("urn:lti:instrole:ims/lis/Administrator"));
    }

    @Test
    void mapLtiToIamRoles_noLtiRoles() {
        Set<AccountRole> result = ltiRolesMapper.convertToIamRoles(null);

        assertTrue(result.size() == 1 && result.contains(AccountRole.STUDENT));
    }

    @Test
    void mapLtiToIamRoles_emptyLtiRolesList() {
        Set<AccountRole> result = ltiRolesMapper.convertToIamRoles(new String[0]);

        assertTrue(result.size() == 1 && result.contains(AccountRole.STUDENT));
    }

    @Test
    void mapLtiToIamRoles() {
        String[] actual = new String[]{
                "urn:lti:role:ims/lis/Learner", //
                "urn:lti:sysrole:ims/lis/SysAdmin",  //
                "Student",  //
                "urn:lti:instrole:ims/lis/Mentor"};

        Set<AccountRole> result = ltiRolesMapper.convertToIamRoles(actual);

        assertAll(
                () -> assertEquals(4, result.size(), "Actual " + result.toString()),
                () -> assertTrue(result.contains(AccountRole.STUDENT), "contains student role"),
                () -> assertTrue(result.contains(AccountRole.ADMIN), "contains admin role"),
                () -> assertTrue(result.contains(AccountRole.INSTRUCTOR), "contains instructor role"),
                () -> assertTrue(result.contains(AccountRole.AERO_INSTRUCTOR), "contains aero instructor role")
        );
    }

    @Test
    void mapLtiToIamRoles_addStudentByDefault() {
        String[] roles = {"urn:lti:role:ims/lis/Instructor/PrimaryInstructor"};

        Set<AccountRole> result = ltiRolesMapper.convertToIamRoles(roles);

        assertEquals(3, result.size());
        assertTrue(result.contains(AccountRole.STUDENT));
        assertTrue(result.contains(AccountRole.INSTRUCTOR));
        assertTrue(result.contains(AccountRole.AERO_INSTRUCTOR));
    }

}
