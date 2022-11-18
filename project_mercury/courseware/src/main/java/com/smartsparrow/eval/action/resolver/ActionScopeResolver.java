package com.smartsparrow.eval.action.resolver;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;

import org.json.JSONException;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class ActionScopeResolver implements Resolver<Action, DataType, LearnerEvaluationResponseContext> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActionScopeResolver.class);

    private final StudentScopeService studentScopeService;

    @Inject
    public ActionScopeResolver(StudentScopeService studentScopeService) {
        this.studentScopeService = studentScopeService;
    }

    @Override
    public Mono<Action> resolve(final Action resolvable, final LearnerEvaluationResponseContext context) {
        return null;
    }

    @Trace(async = true)
    @SuppressWarnings({"unchecked", "Duplicates", "rawtypes"})
    @Override
    public Mono<Action> resolve(final Action resolvable, final DataType dataType, final LearnerEvaluationResponseContext context) {

        final ScopeContext scopeContext = (ScopeContext) resolvable.getResolver();
        final LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();
        final UUID deploymentId = request.getDeployment()
                .getId();
        final UUID studentId = request.getStudentId();
        final UUID sourceId = scopeContext.getSourceId();
        final UUID studentScopeUrn = scopeContext.getStudentScopeURN();
        final List<String> contextPath = scopeContext.getContext();

        if (log.isDebugEnabled()) {
            log.debug(
                    "about to resolve action scope for deployment {}, student {}, sourceId {}, scopeURN {}",
                    deploymentId, studentId, sourceId, studentScopeUrn
            );
        }

        return studentScopeService.findScopeId(deploymentId, studentId, studentScopeUrn)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnableToResolveException("scope id not found");
                })
                .flatMap(scopeId -> studentScopeService.fetchScopeEntry(scopeId, sourceId))
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new UnableToResolveException(String.format(
                            "student scope entry not found for student %s, deployment %s, scopeUrn %s",
                            studentId, deploymentId, studentScopeUrn));
                })
                .map(scopeEntry -> {
                    if (log.isDebugEnabled()) {
                        log.debug("about to find value in json data for scope entry {}", scopeEntry.getId());
                    }
                    return Json.query(scopeEntry.getData(), contextPath);
                })
                .doOnError(JSONException.class, ex -> {
                    throw new UnableToResolveException(ex.getMessage());
                })
                .map(resolvedValue -> {
                    resolvable.setResolvedValue(Json._unwrapValue(resolvedValue, dataType));
                    return resolvable;
                });
    }
}
