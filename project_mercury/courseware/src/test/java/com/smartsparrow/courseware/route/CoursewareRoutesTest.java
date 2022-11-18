package com.smartsparrow.courseware.route;

import static com.smartsparrow.dataevent.RouteUri.AUTHOR_ACTIVITY_EVENT;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_SCENARIO_ACTION_ENRICHER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_SCENARIO_ACTION_PARSER;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.camel.model.BeanDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.route.process.ScenarioActionEnricherHandler;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.service.ChangeCompetencyMetEventHandler;
import com.smartsparrow.learner.service.ChangeScopeEventHandler;
import com.smartsparrow.learner.service.ScenarioEvaluationResultDataStub;
import com.smartsparrow.learner.service.UpdateActivityProgressHandler;
import com.smartsparrow.learner.service.UpdateFreePathwayProgressHandler;
import com.smartsparrow.learner.service.UpdateInteractiveProgressHandler;
import com.smartsparrow.learner.service.UpdateLinearPathwayProgressHandler;
import com.smartsparrow.learner.service.UpdatePathwayProgressHandler;

import reactor.core.publisher.Mono;

public class CoursewareRoutesTest extends CamelTestSupport {

    private CamelReactiveStreamsService reactiveStreamsService;

    @Mock
    private CacheService cacheService;

    @Mock
    private UpdateActivityProgressHandler updateActivityProgressHandler;

    @Mock
    private UpdatePathwayProgressHandler updatePathwayProgressHandler;

    @Mock
    private UpdateLinearPathwayProgressHandler updateLinearPathwayProgressHandler;

    @Mock
    private UpdateFreePathwayProgressHandler updateFreePathwayProgressHandler;

    @Mock
    private UpdateInteractiveProgressHandler updateInteractiveProgressHandler;

    @Mock
    private ChangeCompetencyMetEventHandler changeCompetencyMetEventHandler;

    @Mock
    private ChangeScopeEventHandler changeScopeEventHandler;

    @Mock
    private ScenarioActionEnricherHandler scenarioActionEnricherHandler;


    @InjectMocks
    private CoursewareRoutes route;

    private UUID evaluationId = UUIDs.timeBased();
    private String producingClientId = "clientId";


    @Before
    public void setUp() throws Exception {
        super.setUp();

        // create
        MockitoAnnotations.initMocks(this);
        route.addRoutesToCamelContext(context);
        reactiveStreamsService = CamelReactiveStreams.get(context);
    }

    @Ignore
    @Test
    public void learnerScenarioActionEnricher_oneTruthFulWithActions() throws Exception {

        ArrayList<ScenarioEvaluationResult> scenarioEvalResults = Lists
                .newArrayList(ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false),
                        ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false),
                        ScenarioEvaluationResultDataStub.scenarioEvaluationResult(true));

        EvaluationResult evaluationResult = new EvaluationResult().setScenarioCorrectness(ScenarioCorrectness.correct)
                .setId(evaluationId)
                .setScenarioEvaluationResults(scenarioEvalResults);

        EvaluationEventMessage evaluationEventMessage = new EvaluationEventMessage()
                .setEvaluationResult(evaluationResult)
                .setProducingClientId(producingClientId);

        context.start();

        @SuppressWarnings("unchecked")
        List<Action> body = template
                .requestBody("direct:" + LEARNER_SCENARIO_ACTION_ENRICHER,evaluationEventMessage, List.class);
        assertEquals(2, body.size());
        assertEquals(Action.Type.CHANGE_PROGRESS, body.get(0).getType());
        assertEquals(Action.Type.SEND_FEEDBACK, body.get(1).getType());

    }

    @Ignore
    @Test
    public void learnerScenarioActionEnricher_noTruthful_defaultActionProgressionRepeat() throws Exception {

        ArrayList<ScenarioEvaluationResult> scenarioEvalResults = Lists
                .newArrayList(ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false),
                        ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false),
                        ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false));

        EvaluationResult evaluationResult = new EvaluationResult()
                .setScenarioCorrectness(ScenarioCorrectness.correct)
                .setId(evaluationId)
                .setScenarioEvaluationResults(scenarioEvalResults);

        EvaluationEventMessage evaluationEventMessage = new EvaluationEventMessage()
                .setEvaluationResult(evaluationResult)
                .setProducingClientId(producingClientId);

        context.start();

        @SuppressWarnings("unchecked")
        List<Action> body = template
                .requestBody("direct:" + LEARNER_SCENARIO_ACTION_ENRICHER, evaluationEventMessage, List.class);

        assertEquals(1, body.size());
        assertEquals(Action.Type.CHANGE_PROGRESS, body.get(0).getType());
        assertEquals(ProgressionType.INTERACTIVE_REPEAT, ((ProgressAction)body.get(0)).getContext().getProgressionType());

    }

    @Test
    public void learnerScenarioActionEnricher_noScenarios() throws Exception {

        ArrayList<ScenarioEvaluationResult> scenarioEvalResults = Lists
                .newArrayList();

        EvaluationResult evaluationResult = new EvaluationResult().setScenarioCorrectness(ScenarioCorrectness.correct)
                .setId(evaluationId)
                .setScenarioEvaluationResults(scenarioEvalResults);

        EvaluationEventMessage evaluationEventMessage = new EvaluationEventMessage()
                .setEvaluationResult(evaluationResult)
                .setProducingClientId(producingClientId);

        context.start();

        @SuppressWarnings("unchecked")
        List<Action> body = template
                .requestBody("direct:" + LEARNER_SCENARIO_ACTION_ENRICHER, evaluationEventMessage, List.class);
        assertEquals(1, body.size());
        assertEquals(Action.Type.CHANGE_PROGRESS, body.get(0).getType());
        assertEquals(ProgressionType.INTERACTIVE_COMPLETE, ((ProgressAction)body.get(0)).getContext().getProgressionType());

    }

    @Test
    public void learnerScenarioActionParser_truthFulScenario() throws Exception {

        ScenarioEvaluationResult trueScenario = ScenarioEvaluationResultDataStub.scenarioEvaluationResult(true);
        context.start();

        // Filter "${body.getEvaluationResult} == true")" matches
        @SuppressWarnings("unchecked")
        List<Action> result = template.requestBody("direct:" + LEARNER_SCENARIO_ACTION_PARSER, trueScenario, List.class);
        assertEquals(2, result.size());
        assertEquals(Action.Type.CHANGE_PROGRESS, result.get(0).getType());
        assertEquals(Action.Type.SEND_FEEDBACK, result.get(1).getType());

    }

    @Test
    public void learnerScenarioActionParser_NoTruthFulScenario() throws Exception {

        ScenarioEvaluationResult falseScenario = ScenarioEvaluationResultDataStub.scenarioEvaluationResult(false);
        context.start();

        // Filter "${body.getEvaluationResult} == true")" does not match
        List result = template.requestBody("direct:" + LEARNER_SCENARIO_ACTION_PARSER, falseScenario, List.class);
        assertEquals(0, result.size());

    }

    @Test
    public void configure_AuthorEvent() throws Exception {

        context.getRouteDefinition("ActivityEvent")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() {
                        // replace the last in the route with a mock endpoint
                        weaveByType(BeanDefinition.class).replace().to("mock:bean");
                    }
                });

        UUID projectId = UUIDs.timeBased();
        UUID accountId = UUIDs.timeBased();
        UUID activityId = UUIDs.timeBased();
        ActivityEventMessage eventMessage = new ActivityEventMessage(activityId) //
                .setContent(new CoursewareElementBroadcastMessage().setAction(CoursewareAction.CREATED)
                .setProjectId(projectId)
                .setAccountId(accountId)
                .setElement(CoursewareElement.from(activityId, CoursewareElementType.ACTIVITY))
                .setParentElement(CoursewareElement.from(UUIDs.timeBased(),CoursewareElementType.ACTIVITY))) //
                .setProducingClientId("camel-test-author-activity-event");

        // setup expectations
        MockEndpoint mockBean = getMockEndpoint("mock:bean");
        mockBean.expectedBodiesReceived(eventMessage);
        mockBean.expectedMessageCount(1);


        // Emit event to named camel stream
        Mono.just(eventMessage) //
                .map(event -> reactiveStreamsService.toStream(AUTHOR_ACTIVITY_EVENT, event)) //
                .subscribe();


        assertMockEndpointsSatisfied();
    }

    @Test
    public void configure_SQSFirehose() throws Exception {

        context.getRouteDefinition("SQSClient")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() {
                        // replace the last in the route with a mock endpoint
                        weaveByType(BeanDefinition.class).replace().to("mock:foo");
                    }
                });

        UUID projectId = UUIDs.timeBased();
        UUID accountId = UUIDs.timeBased();
        UUID activityId = UUIDs.timeBased();
        ActivityEventMessage eventMessage = new ActivityEventMessage(activityId) //
                .setContent(new CoursewareElementBroadcastMessage().setAction(CoursewareAction.CREATED)
                        .setProjectId(projectId)
                        .setAccountId(accountId)
                        .setElement(CoursewareElement.from(activityId, CoursewareElementType.ACTIVITY))
                        .setParentElement(CoursewareElement.from(UUIDs.timeBased(),CoursewareElementType.ACTIVITY))) //
                .setProducingClientId("camel-test-author-activity-event");

        // setup expectations
        MockEndpoint mockBean1 =  getMockEndpoint("mock:foo");
        mockBean1.expectedBodiesReceived(eventMessage);
        mockBean1.expectedMessageCount(1);


        // Emit event to named camel stream
        Mono.just(eventMessage) //
                .map(event -> reactiveStreamsService.toStream(AUTHOR_ACTIVITY_EVENT, event)) //
                .subscribe();


        assertMockEndpointsSatisfied();
    }


    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }
}
