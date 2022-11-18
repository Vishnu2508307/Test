package com.smartsparrow.courseware.route;

import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static com.smartsparrow.dataevent.RouteUri.FIREHOSE;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_EVALUATE_COMPLETE;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_EVALUATE_ERROR;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_EVALUATION_RESULT_ENRICHER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_GRADE_PASSBACK_ERROR_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_GRADE_PASSBACK_RESULT_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_ACTIVITY;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_INTERACTIVE;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY_BKT;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY_FREE;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY_GRAPH;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY_LINEAR;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE_PATHWAY_RANDOM;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_SCENARIO_ACTION_ENRICHER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_SCENARIO_ACTION_PARSER;
import static com.smartsparrow.dataevent.RouteUri.RS;
import static com.smartsparrow.learner.route.GradePassbackErrorHandler.LEARNER_GRADE_PASSBACK_ERROR_BODY;
import static com.smartsparrow.learner.route.GradePassbackResultHandler.LEARNER_GRADE_PASSBACK_RESULT_BODY;

import java.util.ArrayList;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;

import com.smartsparrow.cache.config.RouteConsumersConfig;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.route.aggregation.ActionResultsAggregationStrategy;
import com.smartsparrow.courseware.route.process.EvaluationExceptionHandler;
import com.smartsparrow.courseware.route.process.EvaluationResultEnricherHandler;
import com.smartsparrow.courseware.route.process.PathwayDefaultActionProviderHandler;
import com.smartsparrow.courseware.route.process.PrepareUpdateCoursewareElementProgressProcessor;
import com.smartsparrow.courseware.route.process.ScenarioActionEnricherHandler;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.route.GradePassbackErrorHandler;
import com.smartsparrow.learner.route.GradePassbackResultHandler;
import com.smartsparrow.learner.service.ChangeCompetencyMetEventHandler;
import com.smartsparrow.learner.service.ChangeScopeEventHandler;
import com.smartsparrow.learner.service.ChangeScoreEventHandler;
import com.smartsparrow.learner.service.GradePassbackEventHandler;
import com.smartsparrow.learner.service.UpdateActivityProgressHandler;
import com.smartsparrow.learner.service.UpdateBKTPathwayProgressHandler;
import com.smartsparrow.learner.service.UpdateFreePathwayProgressHandler;
import com.smartsparrow.learner.service.UpdateGraphPathwayProgressHandler;
import com.smartsparrow.learner.service.UpdateInteractiveProgressHandler;
import com.smartsparrow.learner.service.UpdateLinearPathwayProgressHandler;
import com.smartsparrow.learner.service.UpdatePathwayProgressHandler;
import com.smartsparrow.learner.service.UpdateRandomPathwayProgressHandler;

public class EvaluationRoutes extends RouteBuilder {

    // Header/Property constants
    public static final String EVALUATION_EVENT_MESSAGE = "evaluationEventMessage";
    public static final String ACTION_PROGRESS_CONTEXT = "actionProgressContext";

    @Inject
    private CacheService cacheService;

    @Inject
    private UpdateActivityProgressHandler updateActivityProgressHandler;

    @Inject
    private UpdatePathwayProgressHandler updatePathwayProgressHandler;

    @Inject
    private UpdateLinearPathwayProgressHandler updateLinearPathwayProgressHandler;

    @Inject
    private UpdateFreePathwayProgressHandler updateFreePathwayProgressHandler;

    @Inject
    private UpdateGraphPathwayProgressHandler updateGraphPathwayProgressHandler;

    @Inject
    private UpdateInteractiveProgressHandler updateInteractiveProgressHandler;

    @Inject
    private ChangeCompetencyMetEventHandler changeCompetencyMetEventHandler;

    @Inject
    private ChangeScopeEventHandler changeScopeEventHandler;

    @Inject
    private ScenarioActionEnricherHandler scenarioActionEnricherHandler;

    @Inject
    private EvaluationResultEnricherHandler evaluationResultEnricherHandler;

    @Inject
    private PathwayDefaultActionProviderHandler pathwayDefaultActionProviderHandler;

    @Inject
    private UpdateRandomPathwayProgressHandler updateRandomPathwayProgressHandler;

    @Inject
    private UpdateBKTPathwayProgressHandler updateBKTPathwayProgressHandler;

    @Inject
    private ChangeScoreEventHandler changeScoreEventHandler;

    @Inject
    private GradePassbackEventHandler gradePassbackEventHandler;

    @Inject
    private GradePassbackErrorHandler gradePassbackErrorHandler;

    @Inject
    private GradePassbackResultHandler gradePassbackResultHandler;

    @Inject
    private EvaluationExceptionHandler evaluationExceptionHandler;

    @Inject
    private RouteConsumersConfig consumersConfig;

    @Override
    public void configure() {

        /*
         *
         * Learner routes
         *
         */
        from(RS + LEARNER_EVALUATE_COMPLETE + "?concurrentConsumers=" + consumersConfig.getLeanerEvaluateCompleteConsumers()) //
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerEvaluateCompleteEvent") //
                .log("Received learner evaluation completed event ${in.body}") //
                .setHeader("evaluationId", simple("body.getEvaluationId"))
                //
                // store a copy of the original body of evaluation event in a property so it propagates in the exchange
                .setProperty(EVALUATION_EVENT_MESSAGE, body())
                .enrich(DIRECT + LEARNER_SCENARIO_ACTION_ENRICHER)
                .enrich(DIRECT + LEARNER_EVALUATION_RESULT_ENRICHER)
                // send each action to its specific action type route
                .split(body(), new ActionResultsAggregationStrategy())
                .setProperty("actionType", simple("${body.getType}"))
                .toD(DIRECT + "${body.getType}", true)
                .end();

        // evaluation error route
        from(DIRECT + LEARNER_EVALUATE_ERROR)
                .id("errorHandler")
                .wireTap(DIRECT + FIREHOSE)
                .bean(evaluationExceptionHandler);

        from(DIRECT + LEARNER_EVALUATION_RESULT_ENRICHER)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id(LEARNER_EVALUATION_RESULT_ENRICHER)
                .bean(evaluationResultEnricherHandler);

        // grade passback routes
        from(RS + LEARNER_GRADE_PASSBACK_ERROR_HANDLER)
                // set error notification
                .setProperty(LEARNER_GRADE_PASSBACK_ERROR_BODY, body())
                // set the id
                .id(LEARNER_GRADE_PASSBACK_ERROR_HANDLER)
                // handle the route via bean handler
                .bean(gradePassbackErrorHandler);

        from(RS + LEARNER_GRADE_PASSBACK_RESULT_HANDLER)
                // set result notification
                .setProperty(LEARNER_GRADE_PASSBACK_RESULT_BODY, body())
                // set the id
                .id(LEARNER_GRADE_PASSBACK_RESULT_HANDLER)
                // handle the route via bean handler
                .bean(gradePassbackResultHandler);

        //
        //  Action routes
        //

        // Action enricher - turns evaluated scenarios action json field into concrete classes
        from(DIRECT + LEARNER_SCENARIO_ACTION_ENRICHER)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id(LEARNER_SCENARIO_ACTION_ENRICHER)
                // Route the cases for scenario evaluation and progress actions where:
                .choice()
                //
                // when there are no scenarios, default to action progress INTERACTIVE_COMPLETE and exit this route,
                // short circuit the parser route and processor below
                .when(simple("${body.getEvaluationResult().getScenarioEvaluationResults().size()} == 0"))
                .log(LoggingLevel.DEBUG,"Injecting default action for evaluation ${body}")
                .bean(pathwayDefaultActionProviderHandler)
                //
                // Scenarios were evaluated vvv
                .otherwise()
                // reach into evaluation result event message and iterate/aggregate over the scenario evaluation results
                .split(simple("body.getEvaluationResult.getScenarioEvaluationResults"), new GroupedBodyAggregationStrategy())
                .parallelProcessing()
                //
                // for each scenario evaluation result:
                .to(DIRECT + LEARNER_SCENARIO_ACTION_PARSER)
                .end()
                // Flatten all parsed scenarios from the Grouped aggregationStrategy, inject default INTERACTIVE_REPEAT if needed
                .bean(scenarioActionEnricherHandler)
                .endChoice();


        // Actions json unmarshaller: a ScenarioEvaluationResult content enricher
        from(DIRECT + LEARNER_SCENARIO_ACTION_PARSER)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerScenarioActionParser")
                // if individual scenario evaluated correctly (all the conditions were met)
                .choice()
                .when(simple("${body.getEvaluationResult} == true"))
                //
                // deserialize actions json property into a list of strong typed actions
                .setBody(simple("body.getActions"))
                .unmarshal(new ListJacksonDataFormat(Action.class))
                .log(LoggingLevel.DEBUG, "Unmarshalled scenario actions list: ${body}")
                // Empty list if theres no truthful scenario actions to unmarshall
                .otherwise()
                .setBody((Supplier<ArrayList>) ArrayList::new)
                .log(LoggingLevel.TRACE, "${routeId}: discarding scenario action ${body}");

        // Progress action
        from(DIRECT + Action.Type.CHANGE_PROGRESS)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                // set current action context body (actionProgressContext) as a header
                .setHeader(ACTION_PROGRESS_CONTEXT, simple("body.getContext()"))
                // and bring back original event message as exchange body
                .setBody(exchange -> exchange.getProperty(EVALUATION_EVENT_MESSAGE))
                .process(new PrepareUpdateCoursewareElementProgressProcessor())
                .wireTap(DIRECT + FIREHOSE)
                .to(DIRECT + LEARNER_PROGRESS_UPDATE);

        // Set competency met action
        from(DIRECT + Action.Type.CHANGE_COMPETENCY)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("ChangeCompetencyMetActionEvent")
                .log("Received event to award a competency met ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(changeCompetencyMetEventHandler);

        // Set scope met action
        from(DIRECT + Action.Type.CHANGE_SCOPE)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("ChangeScopeActionEvent")
                .log("Received event to change scope ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(changeScopeEventHandler);

        // adding feedback action route so that it does not get lost
        from(DIRECT + Action.Type.SEND_FEEDBACK)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("send feedback")
                .wireTap(DIRECT + FIREHOSE)
                .log("Received event to send a feedback ${in.body}");

        from(DIRECT + Action.Type.UNSUPPORTED_ACTION)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .log("Received event for unsupported action ${in.body}")
                .wireTap(DIRECT + FIREHOSE);

        // Award student score entry
        from(DIRECT + Action.Type.CHANGE_SCORE)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("ChangeScoreActionEvent")
                .log("Received event to change score ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(changeScoreEventHandler);

        // Grade passback
        from(DIRECT + Action.Type.GRADE_PASSBACK)
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("GradePassbackActionEvent")
                .log("Received event for grade passback ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(gradePassbackEventHandler);

        //
        // Learner progress updates
        //

        from(DIRECT + LEARNER_PROGRESS_UPDATE) // route evaluation complete to progress updates
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdateEvent")
                .log("Received event to update learner progress ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .toD(DIRECT + LEARNER_PROGRESS_UPDATE + "/${body.getElement().getElementType()}")
                .choice()
                .when(header("progressDone").isEqualTo(false))
                // recurse!
                .to(DIRECT + LEARNER_PROGRESS_UPDATE)
                .otherwise()
                .log(LoggingLevel.DEBUG, "Done calculating progress up the tree: ${body}");

        from(DIRECT + LEARNER_PROGRESS_UPDATE_ACTIVITY) // update progress on an activity
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdateActivity") //
                .bean(updateActivityProgressHandler);

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY) // update progress on a pathway, add header to pathway type
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathway") //
                .bean(updatePathwayProgressHandler)
                .toD(DIRECT + "${header.pathwayUri}");

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY_LINEAR) // update progress on a LINEAR pathway
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathwayLinear")
                .bean(updateLinearPathwayProgressHandler); //

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY_FREE) // update progress on a FREE pathway
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathwayFree")
                .bean(updateFreePathwayProgressHandler);

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY_GRAPH) // update progress on a GRAPH pathway
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathwayGraph")
                .bean(updateGraphPathwayProgressHandler);

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY_RANDOM) // update progress on a RANDOM pathway
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathwayRandom")
                .bean(updateRandomPathwayProgressHandler);

        from(DIRECT + LEARNER_PROGRESS_UPDATE_PATHWAY_BKT) // update progress on a BKT pathway
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdatePathwayBKT") //
                .bean(updateBKTPathwayProgressHandler);

        from(DIRECT + LEARNER_PROGRESS_UPDATE_INTERACTIVE) // update progress on an interactive
                .onException(Throwable.class)
                .to(DIRECT + LEARNER_EVALUATE_ERROR)
                .stop()
                .end()
                .id("LearnerProgressUpdateInteractive") //
                .bean(updateInteractiveProgressHandler); //

//        // Competency document update
//        from(RS + COMPETENCY_DOCUMENT_UPDATE + "?concurrentConsumers=" + consumersConfig.getCompetencyDocumentUpdateConsumers())
//                .id("CompetencyDocumentUpdate")
//                .wireTap(DIRECT + FIREHOSE)
//                .bean(cacheService);
    }


    // Utility methods ----

    public static ProgressAction generateDefaultProgressAction(ProgressionType progressionType) {
        return new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(progressionType));
    }
}
