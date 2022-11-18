package com.smartsparrow.learner.searchable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.HttpMethod;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.learner.data.LearnerSearchableDocument;
import com.smartsparrow.util.UUIDs;

class CsgIndexRequestBuilderTest {

    private final UUID deploymentId = UUIDs.timeBased();
    private final UUID searchableFieldId1  = UUIDs.timeBased();
    private final UUID searchableFieldId2 = UUIDs.timeBased();
    private final UUID changeId = UUIDs.timeBased();
    private final UUID cohortId = UUIDs.timeBased();
    private final UUID elementId = UUIDs.timeBased();
    private final List<UUID> elementPath = Lists.newArrayList(UUIDs.timeBased(), UUIDs.timeBased());
    private final static String uri = "some domain";
    private final static String applicationId = "bronte";
    private final static String piToken = "token";
    private final static String productId = "productId";

    @Test
    void testBuildRequest() {

        LearnerSearchableDocument ls1 = new LearnerSearchableDocument()
                .setDeploymentId(deploymentId)
                .setSearchableFieldId(searchableFieldId1)
                .setChangeId(changeId)
                .setProductId(productId)
                .setCohortId(cohortId)
                .setElementId(elementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setElementPath(elementPath)
                .setContentType("type1")
                .setSummary("First service call")
                .setBody("Well, hello there, Mr World.")
                .setTag("tag")
                .setSource("source")
                .setPreview("preview");

        LearnerSearchableDocument ls2 = new LearnerSearchableDocument()
                .setDeploymentId(UUIDs.timeBased())
                .setSearchableFieldId(searchableFieldId2)
                .setChangeId(UUIDs.timeBased())
                .setProductId("productId")
                .setCohortId(UUIDs.timeBased())
                .setElementId(UUIDs.timeBased())
                .setElementType(CoursewareElementType.INTERACTIVE)
                .setElementPath(Lists.newArrayList(UUIDs.timeBased(), UUIDs.timeBased()))
                .setContentType("type2")
                .setSummary("Second service call")
                .setBody("Well, hello there, Mr World.")
                .setTag("tag")
                .setSource("source")
                .setPreview("preview");

        CsgIndexRequestBuilder builder = new CsgIndexRequestBuilder()
                .sethUri(uri)
                .setApplicationId(applicationId)
                .setPiToken(piToken);

        assertTrue(builder.isEmpty());

        builder.addLearnerDocument(ls1)
                .addLearnerDocument(ls2);

        assertFalse(builder.isEmpty());
        Request request = builder.build();

        Request expected = new Request()
                .setUri(uri)
                .setMethod(HttpMethod.POST)  //
                .setJson(true)
                .addField("headers", ImmutableMap.of( //
                        "application-id", applicationId, //
                        "x-authorization", piToken //
                )) //
                .addField("json", true) //
                .addField("body", Lists.newArrayList(
                        new LearnerSearchableDocumentIndexBodyWrapper().setFields(ls1),
                        new LearnerSearchableDocumentIndexBodyWrapper().setFields(ls2))
                );

        assertEquals(expected, request);


    }


}