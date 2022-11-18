package com.smartsparrow.courseware.service;

import static com.smartsparrow.courseware.service.ScopeReferenceStub.buildScopeReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.courseware.lang.CoursewareElementDuplicationFault;

class DuplicationContextTest {

    private DuplicationContext duplicationContext;

    private static final UUID currentStudentScopeURN = UUID.randomUUID();
    private static final UUID newStudentScopeURN = UUID.randomUUID();
    private static final UUID currentElementId = UUID.randomUUID();
    private static final UUID newElementId = UUID.randomUUID();
    private static final UUID currentSourceIdOne = UUID.randomUUID();
    private static final UUID newSourceIdOne = UUID.randomUUID();
    private static final UUID currentSourceIdTwo = UUID.randomUUID();
    private static final UUID newSourceIdTwo = UUID.randomUUID();
    private static final List<ScopeReference> scopeReferences = Lists.newArrayList(
            buildScopeReference(currentStudentScopeURN, currentSourceIdOne),
            buildScopeReference(currentStudentScopeURN, currentSourceIdTwo)
    );


    @BeforeEach
    void setUp() {
        duplicationContext = new DuplicationContext();
    }

    @Test
    void duplicateScopeReferences() {
        // prepare the context
        duplicationContext.addAllScopeReferences(scopeReferences);
        setIdsMap();

        List<ScopeReference> duplicated = duplicationContext.duplicateScopeReferences();

        assertNotNull(duplicated);
        assertEquals(2, duplicated.size());

        ScopeReference one = duplicated.get(0);

        assertNotNull(one);
        assertEquals(newStudentScopeURN, one.getScopeURN());
        assertEquals(newSourceIdOne, one.getElementId());

        ScopeReference two = duplicated.get(1);

        assertNotNull(two);
        assertEquals(newStudentScopeURN, two.getScopeURN());
        assertEquals(newSourceIdTwo, two.getElementId());
    }

    @Test
    void duplicateScopeReferences_missingIds() {
        // prepare the context
        duplicationContext.addAllScopeReferences(scopeReferences);
        // not setting ids map
        CoursewareElementDuplicationFault e = assertThrows(CoursewareElementDuplicationFault.class,
                () -> duplicationContext.duplicateScopeReferences());

        assertNotNull(e);
        assertEquals("some ids are missing: newStudentScopeUrn -> `null` newElementId -> `null`", e.getMessage());
    }

    @Test
    void duplicateScopeReferences_emptyReferences() {
        List<ScopeReference> duplicated = duplicationContext.duplicateScopeReferences();
        assertNotNull(duplicated);
        assertTrue(duplicated.isEmpty());
    }

    private void setIdsMap() {
        duplicationContext.putIds(currentStudentScopeURN, newStudentScopeURN);
        duplicationContext.putIds(currentElementId, newElementId);
        duplicationContext.putIds(currentSourceIdOne, newSourceIdOne);
        duplicationContext.putIds(currentSourceIdTwo, newSourceIdTwo);
    }

}