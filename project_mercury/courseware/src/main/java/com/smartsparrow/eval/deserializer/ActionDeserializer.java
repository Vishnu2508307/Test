package com.smartsparrow.eval.deserializer;

import static com.smartsparrow.eval.action.Action.Type.CHANGE_COMPETENCY;
import static com.smartsparrow.eval.action.Action.Type.CHANGE_PROGRESS;
import static com.smartsparrow.eval.action.Action.Type.CHANGE_SCOPE;
import static com.smartsparrow.eval.action.Action.Type.SEND_FEEDBACK;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.lang.ActionConditionParserFault;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.action.feedback.SendFeedbackAction;
import com.smartsparrow.eval.action.feedback.SendFeedbackActionContext;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.action.score.ChangeScoreActionContext;
import com.smartsparrow.eval.action.unsupported.UnsupportedAction;
import com.smartsparrow.eval.action.unsupported.UnsupportedActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.util.Enums;

import reactor.core.publisher.Mono;

public class ActionDeserializer extends JsonDeserializer<Action> {

    private final ObjectMapper mapper;

    public ActionDeserializer() {
        this.mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Action.class, this);
        module.addDeserializer(ResolverContext.class, new ResolverDeserializer(ResolverContext.class));
        mapper.registerModule(module);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    @Override
    public Action deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        JsonNode root = mapper.readTree(p);
        return parseAction(root);
    }

    /**
     * Deserialize a stringified json array of actions into a list of {@link Action}
     *
     * @param actions the string representation of the kj
     * @return a mono containing a list of deserialized actions
     */
    public Mono<List<Action>> reactiveDeserialize(String actions) {
        return Mono.just(1)
                .map(ignored -> deserialize(actions));
    }

    /**
     * Deserialize a stringified json array of actions into a list of {@link Action}
     *
     * @param actions the string representation of the kj
     * @return a list of deserialized actions
     */
    @Trace(async = true)
    public List<Action> deserialize(String actions) {
        try {
            return mapper.readValue(actions, new TypeReference<List<Action>>() {
            });
        } catch (Exception e) {
            throw new ActionConditionParserFault(e);
        }
    }

    Action parseAction(final JsonNode node) throws IOException {

        JsonNode actionNode = node.get("action");
        JsonNode contextNode = node.get("context");

        affirmArgument(actionNode != null, "`action` node is required for Action");
        affirmArgument(contextNode != null, "`action` node is required for Action");
        Action.Type type;

        try {
            type = Enums.of(Action.Type.class, actionNode.asText());
        } catch (IllegalArgumentException e) {
            return new UnsupportedAction()
                    .setContext(new UnsupportedActionContext()
                            .setValue(actionNode.asText()));
        }

        switch (type) {
        case CHANGE_PROGRESS:
            return new ProgressAction()
                    .setType(CHANGE_PROGRESS)
                    .setContext(mapper.readValue(contextNode.toString(), ProgressActionContext.class))
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
        case SEND_FEEDBACK:
            return new SendFeedbackAction()
                    .setType(SEND_FEEDBACK)
                    .setContext(mapper.readValue(contextNode.toString(), SendFeedbackActionContext.class))
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
        case SET_COMPETENCY:
        case CHANGE_COMPETENCY:
            ChangeCompetencyMetAction changeCompetencyMetAction = new ChangeCompetencyMetAction()
                    .setType(CHANGE_COMPETENCY)
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
            ChangeCompetencyMetActionContext context = mapper.readValue(contextNode.toString(), ChangeCompetencyMetActionContext.class);
            // set a default SET operator if field is missing
            if(context.getOperator() == null) {
                context.setOperator(MutationOperator.SET);
            }
            changeCompetencyMetAction.setContext(context);
            return changeCompetencyMetAction;

        case CHANGE_SCOPE:
            return new ChangeScopeAction()
                    .setType(CHANGE_SCOPE)
                    .setContext(mapper.readValue(contextNode.toString(), ChangeScopeActionContext.class))
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
        case CHANGE_SCORE:
            return new ChangeScoreAction()
                    .setContext(mapper.readValue(contextNode.toString(), ChangeScoreActionContext.class))
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));
        /* todo GRADE_PASSBACK case will be enabled in BRNT-7735*/
        case GRADE_PASSBACK:
            return new GradePassbackAction()
                    .setContext(mapper.readValue(contextNode.toString(), GradePassbackActionContext.class))
                    .setResolver(mapper.readValue(node.toString(), ResolverContext.class));

        default:
            return new UnsupportedAction()
                    .setContext(new UnsupportedActionContext()
                            .setValue(actionNode.asText()));
        }
    }


}
