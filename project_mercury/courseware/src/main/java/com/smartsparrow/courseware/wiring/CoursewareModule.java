package com.smartsparrow.courseware.wiring;

import com.smartsparrow.cohort.route.CohortRoute;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.BKTPathway;
import com.smartsparrow.courseware.pathway.FreeLearnerPathway;
import com.smartsparrow.courseware.pathway.FreePathway;
import com.smartsparrow.courseware.pathway.GraphPathway;
import com.smartsparrow.courseware.pathway.LearnerBKTPathway;
import com.smartsparrow.courseware.pathway.LearnerGraphPathway;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;
import com.smartsparrow.courseware.pathway.LinearLearnerPathway;
import com.smartsparrow.courseware.pathway.LinearPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.RandomPathway;
import com.smartsparrow.courseware.route.CoursewareRoutes;
import com.smartsparrow.courseware.route.EvaluationRoutes;
import com.smartsparrow.courseware.service.DiffSyncActivityService;
import com.smartsparrow.courseware.service.DiffSyncInteractiveService;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.data.LearnerChangeCompetencyMetActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeProgressActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeScopeActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeScoreActionConsumer;
import com.smartsparrow.eval.data.LearnerEmptyActionConsumer;
import com.smartsparrow.eval.data.LearnerGradePassbackActionConsumer;
import com.smartsparrow.eval.service.ActivityProgressUpdateService;
import com.smartsparrow.eval.service.AlgoBKTPathwayProgressUpdateService;
import com.smartsparrow.eval.service.FreePathwayProgressUpdateService;
import com.smartsparrow.eval.service.GenericPathwayProgressUpdateService;
import com.smartsparrow.eval.service.GraphPathwayProgressUpdateService;
import com.smartsparrow.eval.service.InteractiveProgressUpdateService;
import com.smartsparrow.eval.service.LearnerEvaluationService;
import com.smartsparrow.eval.service.LinearPathwayProgressUpdateService;
import com.smartsparrow.eval.service.RandomPathwayProgressUpdateService;
import com.smartsparrow.eval.service.TestEvaluationService;
import com.smartsparrow.learner.route.CSGIndexRoute;
import com.smartsparrow.user_content.route.UserContentRoute;
import com.smartsparrow.workspace.route.AlfrescoCoursewareRoute;

import data.EntityType;

public class CoursewareModule extends AbstractCoursewareModule {

    @Override
    protected void configure() {
        super.configure();
     }

    @Override
    public void decorate() {
        bind(CoursewareRoutes.class);
        bind(EvaluationRoutes.class);
        bind(CohortRoute.class);
        bind(UserContentRoute.class);
        bind(AlfrescoCoursewareRoute.class);
        bind(CSGIndexRoute.class);

        // add in the bindings
        pathwayTypes.addBinding(PathwayType.LINEAR).to(LinearPathway.class);
        pathwayTypes.addBinding(PathwayType.FREE).to(FreePathway.class);
        pathwayTypes.addBinding(PathwayType.GRAPH).to(GraphPathway.class);
        pathwayTypes.addBinding(PathwayType.RANDOM).to(RandomPathway.class);
        pathwayTypes.addBinding(PathwayType.ALGO_BKT).to(BKTPathway.class);

        learnerPathwayTypes.addBinding(PathwayType.LINEAR).to(LinearLearnerPathway.class);
        learnerPathwayTypes.addBinding(PathwayType.FREE).to(FreeLearnerPathway.class);
        learnerPathwayTypes.addBinding(PathwayType.GRAPH).to(LearnerGraphPathway.class);
        learnerPathwayTypes.addBinding(PathwayType.RANDOM).to(LearnerRandomPathway.class);
        learnerPathwayTypes.addBinding(PathwayType.ALGO_BKT).to(LearnerBKTPathway.class);

        // setup binding of evaluation implementations
        evaluationTypes.addBinding(EvaluationRequest.Type.TEST).to(TestEvaluationService.class);
        evaluationTypes.addBinding(EvaluationRequest.Type.LEARNER).to(LearnerEvaluationService.class);

        // setup binding for action consumers
        actionConsumers.addBinding(Action.Type.CHANGE_COMPETENCY).to(LearnerChangeCompetencyMetActionConsumer.class);
        actionConsumers.addBinding(Action.Type.CHANGE_SCOPE).to(LearnerChangeScopeActionConsumer.class);
        actionConsumers.addBinding(Action.Type.CHANGE_SCORE).to(LearnerChangeScoreActionConsumer.class);
        actionConsumers.addBinding(Action.Type.UNSUPPORTED_ACTION).to(LearnerEmptyActionConsumer.class);
        actionConsumers.addBinding(Action.Type.GRADE).to(LearnerEmptyActionConsumer.class);
        actionConsumers.addBinding(Action.Type.SEND_FEEDBACK).to(LearnerEmptyActionConsumer.class);
        actionConsumers.addBinding(Action.Type.SET_COMPETENCY).to(LearnerEmptyActionConsumer.class);
        actionConsumers.addBinding(Action.Type.CHANGE_PROGRESS).to(LearnerChangeProgressActionConsumer.class);
        actionConsumers.addBinding(Action.Type.GRADE_PASSBACK).to(LearnerGradePassbackActionConsumer.class);

        // setup binding for progress update service implementations
        progressUpdateImplementations.addBinding(CoursewareElementType.INTERACTIVE).to(InteractiveProgressUpdateService.class);
        progressUpdateImplementations.addBinding(CoursewareElementType.ACTIVITY).to(ActivityProgressUpdateService.class);
        progressUpdateImplementations.addBinding(CoursewareElementType.PATHWAY).to(GenericPathwayProgressUpdateService.class);

        pathwayProgressUpdateImplementations.addBinding(PathwayType.LINEAR).to(LinearPathwayProgressUpdateService.class);
        pathwayProgressUpdateImplementations.addBinding(PathwayType.FREE).to(FreePathwayProgressUpdateService.class);
        pathwayProgressUpdateImplementations.addBinding(PathwayType.GRAPH).to(GraphPathwayProgressUpdateService.class);
        pathwayProgressUpdateImplementations.addBinding(PathwayType.ALGO_BKT).to(AlgoBKTPathwayProgressUpdateService.class);
        pathwayProgressUpdateImplementations.addBinding(PathwayType.RANDOM).to(RandomPathwayProgressUpdateService.class);

        // setup binding for synchronizable service implementations for diff sync
        synchronizableServiceImplementations.addBinding(EntityType.ACTIVITY_CONFIG).to(DiffSyncActivityService.class);
        synchronizableServiceImplementations.addBinding(EntityType.INTERACTIVE_CONFIG).to(DiffSyncInteractiveService.class);
    }
}
