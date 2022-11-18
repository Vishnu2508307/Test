package com.smartsparrow.eval.action.resolver;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.Json;

import reactor.core.publisher.Mono;

/**
 * Evaluation is moving out of camel. This resolver takes in an {@link EvaluationEventMessage} which is no longer
 * required and has been deprecated
 */
@Deprecated
public class DeprecatedActionScopeResolver implements Resolver<Action, DataType, EvaluationEventMessage> {

    private static final Logger log = LoggerFactory.getLogger(DeprecatedActionScopeResolver.class);

    private final StudentScopeService studentScopeService;

    @Inject
    public DeprecatedActionScopeResolver(StudentScopeService studentScopeService) {
        this.studentScopeService = studentScopeService;
    }

    @Override
    public Mono<Action> resolve(final Action resolvable, final EvaluationEventMessage context) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Action> resolve(final Action resolvable, final DataType dataType, final EvaluationEventMessage eventMessage) {

        final ScopeContext scopeContext = (ScopeContext) resolvable.getResolver();
        final UUID deploymentId = eventMessage.getDeploymentId();
        final UUID studentId = eventMessage.getStudentId();
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
