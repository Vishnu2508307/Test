package com.smartsparrow.courseware.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScopeReference;

public class ScopeReferenceStub {

    public static ScopeReference buildScopeReference(UUID scopeURN, UUID elementId) {
        return new ScopeReference()
                .setScopeURN(scopeURN)
                .setElementId(elementId)
                .setElementType(CoursewareElementType.COMPONENT)
                .setPluginId(UUID.randomUUID())
                .setPluginVersion("1.2.1");
    }
}
