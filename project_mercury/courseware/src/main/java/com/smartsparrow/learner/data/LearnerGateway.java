package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.LearnerConfigurationFieldMaterializer;
import com.smartsparrow.courseware.data.LearnerConfigurationFieldMutator;
import com.smartsparrow.courseware.data.LearnerElementConfigurationField;
import com.smartsparrow.courseware.data.LearnerManualGradingConfigurationMaterializer;
import com.smartsparrow.courseware.data.LearnerManualGradingConfigurationMutator;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerGateway {

    private final Logger log = LoggerFactory.getLogger(LearnerGateway.class);

    private final Session session;

    private final LearnerStudentScopeRegistryMaterializer learnerStudentScopeRegistryMaterializer;
    private final LearnerStudentScopeRegistryMutator learnerStudentScopeRegistryMutator;
    private final LearnerWalkableByStudentScopeMaterializer learnerWalkableByStudentScopeMaterializer;
    private final LearnerConfigurationFieldMaterializer learnerConfigurationFieldMaterializer;
    private final LearnerConfigurationFieldMutator learnerConfigurationFieldMutator;
    private final LearnerManualGradingConfigurationMaterializer learnerManualGradingConfigurationMaterializer;
    private final LearnerManualGradingConfigurationMutator learnerManualGradingConfigurationMutator;
    private final LearnerElementMetaInformationMaterializer learnerElementMetaInformationMaterializer;
    private final LearnerElementMetaInformationMutator learnerElementMetaInformationMutator;
    private final LearnerElementMaterializer learnerElementMaterializer;
    private final LearnerElementMutator learnerElementMutator;

    @Inject
    public LearnerGateway(final Session session,
                          final LearnerStudentScopeRegistryMaterializer learnerStudentScopeRegistryMaterializer,
                          final LearnerStudentScopeRegistryMutator learnerStudentScopeRegistryMutator,
                          final LearnerWalkableByStudentScopeMaterializer learnerWalkableByStudentScopeMaterializer,
                          final LearnerConfigurationFieldMaterializer learnerConfigurationFieldMaterializer,
                          final LearnerConfigurationFieldMutator learnerConfigurationFieldMutator,
                          final LearnerManualGradingConfigurationMaterializer learnerManualGradingConfigurationMaterializer,
                          final LearnerManualGradingConfigurationMutator learnerManualGradingConfigurationMutator,
                          final LearnerElementMetaInformationMaterializer learnerElementMetaInformationMaterializer,
                          final LearnerElementMetaInformationMutator learnerElementMetaInformationMutator,
                          final LearnerElementMaterializer learnerElementMaterializer,
                          final LearnerElementMutator learnerElementMutator) {
        this.session = session;
        this.learnerStudentScopeRegistryMaterializer = learnerStudentScopeRegistryMaterializer;
        this.learnerStudentScopeRegistryMutator = learnerStudentScopeRegistryMutator;
        this.learnerWalkableByStudentScopeMaterializer = learnerWalkableByStudentScopeMaterializer;
        this.learnerConfigurationFieldMaterializer = learnerConfigurationFieldMaterializer;
        this.learnerConfigurationFieldMutator = learnerConfigurationFieldMutator;
        this.learnerManualGradingConfigurationMaterializer = learnerManualGradingConfigurationMaterializer;
        this.learnerManualGradingConfigurationMutator = learnerManualGradingConfigurationMutator;
        this.learnerElementMetaInformationMaterializer = learnerElementMetaInformationMaterializer;
        this.learnerElementMetaInformationMutator = learnerElementMetaInformationMutator;
        this.learnerElementMaterializer = learnerElementMaterializer;
        this.learnerElementMutator = learnerElementMutator;
    }

    /**
     * Persist a learner scope reference to the registry
     *
     * @param reference the reference info to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final LearnerScopeReference reference) {
        return Mutators.execute(session, Flux.just(
                learnerStudentScopeRegistryMutator.upsert(reference)
        ));
    }

    /**
     * Find a walkable by its student scope urn and deployment id
     *
     * @param studentScopeURN the student scope urn to find the walkable for
     * @param deploymentId    the deployment id to find the walkable in
     * @return a mono of courseware element
     */
    public Mono<CoursewareElement> findWalkable(final UUID studentScopeURN, final UUID deploymentId) {
        return ResultSets.query(session, learnerWalkableByStudentScopeMaterializer.findElement(studentScopeURN, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerWalkableByStudentScopeMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the student scope registered element
     *
     * @param studentScopeURN the student scope to find the registered element for
     * @param deploymentId    the deployment id to look in
     * @param changeId        the change id
     * @param elementId       the element id that should be registered to the student scope
     * @return a mono of learner scope reference
     */
    @Trace(async = true)
    public Mono<LearnerScopeReference> findRegisteredElement(final UUID studentScopeURN, final UUID deploymentId, final UUID changeId, final UUID elementId) {
        return ResultSets.query(session, learnerStudentScopeRegistryMaterializer.findRegisteredElement(
                studentScopeURN, deploymentId, changeId, elementId))
                .flatMapIterable(row -> row)
                .map(learnerStudentScopeRegistryMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all elements registered in the scope
     *
     * @param studentScopeURN the student scope to find the registered elements for
     * @param deploymentId    the deployment id to look in
     * @param changeId        the change id
     * @return a flux of learner scope reference
     */
    public Flux<LearnerScopeReference> findAllRegistered(final UUID studentScopeURN, final UUID deploymentId, final UUID changeId) {
        return ResultSets.query(session, learnerStudentScopeRegistryMaterializer.findAllRegistered(
                studentScopeURN, deploymentId, changeId))
                .flatMapIterable(row -> row)
                .map(learnerStudentScopeRegistryMaterializer::fromRow);
    }

    /**
     * Persist a learner configuration field for an element
     *
     * @param configurationField the configuration field to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final LearnerElementConfigurationField configurationField) {
        return Mutators.execute(session, Flux.just(
                learnerConfigurationFieldMutator.upsert(configurationField)
        ));
    }

    /**
     * Find a configuration field for a deployed courseware element
     *
     * @param deploymentId the deployment id to find the element in
     * @param changeId the change id of the deployment
     * @param elementId the element id to find the configuration field for
     * @param fieldName the name of the configuration field to find
     * @return a mono of configuration field
     */
    @Trace(async = true)
    public Mono<ConfigurationField> findConfigurationField(final UUID deploymentId, final UUID changeId, final UUID elementId, final String fieldName) {
        return ResultSets.query(session, learnerConfigurationFieldMaterializer.fetchField(deploymentId, changeId, elementId, fieldName))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerConfigurationFieldMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist a learner element meta information
     *
     * @param learnerElementMetaInformation the obj to persist
     * @return a flux of void
     */
    public Flux<Void> persist(final LearnerElementMetaInformation learnerElementMetaInformation) {
        return Mutators.execute(session, Flux.just(
                learnerElementMetaInformationMutator.upsert(learnerElementMetaInformation)
        )).doOnError(throwable -> {
            log.error("error while persisting learner element meta information", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch learner element meta information by element id and key
     *
     * @param elementId the element id to find the meta info for
     * @param key the name of the meta information to fetch fot the element
     * @return a mono with the meta information or an empty mono when not found
     */
    public Mono<LearnerElementMetaInformation> fetchMetaInformation(final UUID elementId, final UUID deploymentId,
                                                                    final UUID changeId, final String key) {
        return ResultSets.query(session, learnerElementMetaInformationMaterializer
                .findMetaInformation(elementId, deploymentId, changeId, key))
                .flatMapIterable(row -> row)
                .map(learnerElementMetaInformationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist a learner courseware element to the db
     *
     * @param element the element to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final LearnerCoursewareElement element) {
        return Mutators.execute(session, Flux.just(
                learnerElementMutator.upsert(element)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an element by id and  deployment id
     *
     * @param elementId       the element id
     * @param deploymentId    the deployment id to find the element in
     * @return a mono of courseware element
     */
    @Trace(async = true)
    public Mono<LearnerCoursewareElement> fetchElementByDeployment(final UUID elementId, final UUID deploymentId) {
        return ResultSets.query(session, learnerElementMaterializer.findByDeployment(elementId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerElementMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
