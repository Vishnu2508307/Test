package com.smartsparrow.courseware.pathway;

import static com.smartsparrow.iam.IamDataStub.STUDENT_A_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LinearLearnerPathwayTest {

    @InjectMocks
    private LinearLearnerPathway linearLearnerPathway;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ProgressService progressService;

    private WalkableChild walkableChild1;
    private WalkableChild walkableChild2;
    private WalkableChild walkableChild3;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        // TODO: mock this data out in a data service stub
        walkableChild1 = new WalkableChild().setElementId(UUIDs.timeBased());
        walkableChild2 = new WalkableChild().setElementId(UUIDs.timeBased());
        walkableChild3 = new WalkableChild().setElementId(UUIDs.timeBased());
    }

    @Test
    @DisplayName("Should have no results when everything is empty")
    void supplyRelevantWalkables_emptyProgress() {
        when(learnerPathwayService.findWalkables(any(), any())).thenReturn(Flux.empty());
        when(progressService.findLatestLinearPathway(any(), any(), eq(STUDENT_A_ID))).thenReturn(Mono.empty());

        Flux<WalkableChild> walkableChildFlux = linearLearnerPathway.supplyRelevantWalkables(STUDENT_A_ID);

        //
        List<WalkableChild> items = walkableChildFlux.collectList().block();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should have first result when progress not found")
    void supplyRelevantWalkables_noProgress_firstResult() {
        when(learnerPathwayService.findWalkables(any(), any())) //
                .thenReturn(Flux.just(walkableChild1, walkableChild2, walkableChild3));
        when(progressService.findLatestLinearPathway(any(), any(), eq(STUDENT_A_ID))).thenReturn(Mono.empty());

        Flux<WalkableChild> walkableChildFlux = linearLearnerPathway.supplyRelevantWalkables(STUDENT_A_ID);

        //
        List<WalkableChild> items = walkableChildFlux.collectList().block();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), walkableChild1);
    }

    @Test
    @DisplayName("Should have first result when progress empty")
    void supplyRelevantWalkables_firstElement() {
        when(learnerPathwayService.findWalkables(any(), any())) //
                .thenReturn(Flux.just(walkableChild1, walkableChild2, walkableChild3));
        LinearPathwayProgress progress = new LinearPathwayProgress();
        when(progressService.findLatestLinearPathway(any(), any(), eq(STUDENT_A_ID))).thenReturn(Mono.just(progress));

        Flux<WalkableChild> walkableChildFlux = linearLearnerPathway.supplyRelevantWalkables(STUDENT_A_ID);

        //
        List<WalkableChild> items = walkableChildFlux.collectList().block();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), walkableChild1);
    }

    @Test
    @DisplayName("Should have the second result when progress exists")
    void supplyRelevantWalkables_secondElement() {
        when(learnerPathwayService.findWalkables(any(), any())) //
                .thenReturn(Flux.just(walkableChild1, walkableChild2, walkableChild3));
        LinearPathwayProgress progress = new LinearPathwayProgress() //
                .setId(UUID.randomUUID())
                .setCompletedWalkables(Lists.newArrayList(walkableChild1.getElementId())); // add the first element.
        when(progressService.findLatestLinearPathway(any(), any(), eq(STUDENT_A_ID))).thenReturn(Mono.just(progress));

        Flux<WalkableChild> walkableChildFlux = linearLearnerPathway.supplyRelevantWalkables(STUDENT_A_ID);

        //
        List<WalkableChild> items = walkableChildFlux.collectList().block();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), walkableChild2);
    }
}
