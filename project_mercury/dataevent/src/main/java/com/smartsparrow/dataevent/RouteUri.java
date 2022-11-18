package com.smartsparrow.dataevent;

/**
 * Constants repository for Endpoint URI centralization.
 * The purpose is facilitate refactoring by having all endpoint mentions in route definitions or producers originating
 * from this point.
 *
 * Constant name should always follow COMPONENT + ENDPOINT_NAME pattern so it is obvious at a glance what kind of
 * integration is happening, eg: 2 similar intra vm routes with endpoints called: DIRECT_MY_ENDPOINT and
 * SEDA_MY_ENDPOINT have different  behaviour expectations because "seda:my_endpoint" is async while "direct:my_endpoint"
 * is blocking, each with it's own route definition
 *
 * The exception is reactive-streams, which are declared with the full uri name and a version without the "
 * reactive:streams" component, to be used by {@link org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService}
 * methods that require just the name part
 *
 *
 */
public class RouteUri {

    // Route name constants
    public static final String RS = "reactive-streams:";
    public static final String DIRECT = "direct:";

    // IAM
    public static final String IES_TOKEN_VALIDATE = "ies.token.validate";
    public static final String IES_PROFILE_GET = "ies.profile.get";
    public static final String IES_BATCH_PROFILE_GET = "ies.batch.profiles.get";
    public static final String MYCLOUD_TOKEN_VALIDATE = "mycloud.token.validate";
    public static final String MYCLOUD_PROFILE_GET = "mycloud.profile.get";
    public static final String REGISTRAR_SECTION_ROLE_GET = "registrar.section.role.get";

    // COURSEWARE
    public static final String AUTHOR_ACTIVITY_EVENT = "author.activity.event";

    // COHORT
    public static final String PASSPORT_ENTITLEMENT_CHECK = "passport.entitlement.check";

    // CHANGELOG
    public static final String PROJECT_CHANGELOG_EVENT = "project.changelog.event";
    public static final String ACTIVITY_CHANGELOG_EVENT = "project.activity.changelog.event";

    // LEARNER
    public static final String LEARNER_EVALUATE_COMPLETE = "learner.evaluate.complete";
    public static final String LEARNER_PROGRESS_UPDATE = "learner.progress.update";
    public static final String LEARNER_EVALUATE_ERROR = "learner.evaluate.error";

    public static final String LEARNER_EVALUATION_RESULT_ENRICHER = "learner.evaluation.result.enricher";
    public static final String LEARNER_SCENARIO_ACTION_ENRICHER = "learner.scenario.action.enricher";
    public static final String LEARNER_SCENARIO_ACTION_PARSER = "learner.scenario.action.parser";

    public static final String LEARNER_PROGRESS_UPDATE_ACTIVITY = LEARNER_PROGRESS_UPDATE + "/ACTIVITY";
    public static final String LEARNER_PROGRESS_UPDATE_INTERACTIVE = LEARNER_PROGRESS_UPDATE + "/INTERACTIVE";

    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY = LEARNER_PROGRESS_UPDATE + "/PATHWAY";
    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY_LINEAR = LEARNER_PROGRESS_UPDATE_PATHWAY + "/LINEAR";
    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY_FREE = LEARNER_PROGRESS_UPDATE_PATHWAY + "/FREE";
    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY_GRAPH = LEARNER_PROGRESS_UPDATE_PATHWAY + "/GRAPH";
    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY_RANDOM = LEARNER_PROGRESS_UPDATE_PATHWAY + "/RANDOM";
    public static final String LEARNER_PROGRESS_UPDATE_PATHWAY_BKT = LEARNER_PROGRESS_UPDATE_PATHWAY + "/ALGO_BKT";

    public static final String COMPETENCY_DOCUMENT_UPDATE = "competency.document.update";

    public static final String LEARNER_GRADE_PASSBACK_RESULT_HANDLER = "learner.grade.passback.result.handler";
    public static final String LEARNER_GRADE_PASSBACK_ERROR_HANDLER = "learner.grade.passback.error.handler";

    // ANALYTICS
    public static final String CONTENT_SEEDING_EVENT = "la.content.seed.event.message";

    //SQS queue
    public static final String FIREHOSE = "firehose";

    // Alfresco
    public static final String ALFRESCO_NODE_CHILDREN = "alfresco.node.children";
    public static final String ALFRESCO_NODE_CONTENT = "alfresco.node.content";
    public static final String ALFRESCO_NODE_INFO = "alfresco.node.info";
    public static final String ALFRESCO_NODE_PULL = "alfresco.node.pull";

    public static final String ALFRESCO_NODE_PUSH_RESULT_HANDLER = "alfresco.node.push.result.handler";
    public static final String ALFRESCO_NODE_PULL_RESULT_HANDLER = "alfresco.node.pull.result.handler";
    public static final String ALFRESCO_NODE_PUSH_ERROR_HANDLER = "alfresco.node.push.error.handler";
    public static final String ALFRESCO_NODE_PULL_ERROR_HANDLER = "alfresco.node.pull.error.handler";

    //Publication
    public static final String PUBLICATION_JOB_EVENT = "publication.job.event";
    public static final String PUBLICATION_OCULUS_STATUS_EVENT = "publication.oculus.status.event";

    //Math
    public static final String MATH_ASSET_GET = "math.asset.get";
}
