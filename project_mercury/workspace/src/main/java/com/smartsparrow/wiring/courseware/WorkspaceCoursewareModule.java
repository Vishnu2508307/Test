package com.smartsparrow.wiring.courseware;

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
import com.smartsparrow.courseware.wiring.AbstractCoursewareModule;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.service.TestEvaluationService;
import com.smartsparrow.learner.route.CSGIndexRoute;
import com.smartsparrow.user_content.route.UserContentRoute;
import com.smartsparrow.workspace.route.AlfrescoCoursewareRoute;

public class WorkspaceCoursewareModule extends AbstractCoursewareModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        bind(CoursewareRoutes.class);
        bind(AlfrescoCoursewareRoute.class);
        bind(CSGIndexRoute.class);
        bind(UserContentRoute.class);

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
    }
}
