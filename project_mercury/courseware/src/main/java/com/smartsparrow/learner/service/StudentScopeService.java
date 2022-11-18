package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.data.StudentScope;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.data.StudentScopeGateway;
import com.smartsparrow.learner.data.StudentScopeTrace;
import com.smartsparrow.learner.lang.DataValidationException;
import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.learner.lang.RegisteredElementNotFoundException;
import com.smartsparrow.learner.payload.StudentScopePayload;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginSchemaParser;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class StudentScopeService {

    private final StudentScopeGateway studentScopeGateway;
    private final PluginService pluginService;
    private final PluginSchemaParser pluginSchemaParser;
    private final LearnerService learnerService;
    private final LearnerCoursewareService learnerCoursewareService;

    @Inject
    public StudentScopeService(StudentScopeGateway studentScopeGateway,
                               PluginService pluginService,
                               CoursewareService coursewareService,
                               PluginSchemaParser pluginSchemaParser,
                               LearnerService learnerService,
                               LearnerCoursewareService learnerCoursewareService) {
        this.studentScopeGateway = studentScopeGateway;
        this.pluginService = pluginService;
        this.pluginSchemaParser = pluginSchemaParser;
        this.learnerService = learnerService;
        this.learnerCoursewareService = learnerCoursewareService;
    }

    /**
     * Create a new student scope for the given deployment, student and scope URN. The method build a student scope trace
     * for each courseware element found in the ancestry for the element associated to the supplied scope urn.
     *
     * @param deploymentId the deployment id
     * @param studentId the student id
     * @param scopeURN the scope urn
     * @return a mono of student scope
     */
    public Mono<StudentScope> createScope(final UUID deploymentId, final UUID studentId, final UUID scopeURN) {
        affirmArgument(deploymentId != null, "deploymentId can not be null");
        affirmArgument(studentId != null, "studentId can not be null");
        affirmArgument(scopeURN != null, "scopeURN can not be null");

        final UUID scopeId = UUIDs.timeBased();

        StudentScope scope = new StudentScope()
                .setDeploymentId(deploymentId)
                .setAccountId(studentId)
                .setScopeUrn(scopeURN)
                .setId(scopeId);

        Mono<CoursewareElement> walkableMono = learnerService.findWalkable(scopeURN, deploymentId);

        return walkableMono
                .flux()
                .flatMap(walkable -> trackStudentScopeByAncestry(deploymentId, studentId, scopeURN, scopeId, walkable))
                .collectList()
                .flux()
                .flatMap(one -> studentScopeGateway.persist(scope, one))
                .then(Mono.just(scope));
    }

    /**
     * Create a flux of student scope info to be tracked by each element in the ancestry. The ancestry is relative to
     * the courseware element associated with the scopeUrn the student scope is created for. This allows later on to
     * find the sub-tree of courseware elements the student scope has been initialised for from any element in the
     * courseware element structure.
     *
     * @param deploymentId the deployment id
     * @param studentId the student id the scope is initialised for
     * @param scopeURN the scope urn that has been initialised
     * @param scopeId the if of the initialised scope
     * @param walkable the walkable the student scope ancestry should be tracked for
     */
    private Flux<StudentScopeTrace> trackStudentScopeByAncestry(UUID deploymentId, UUID studentId, UUID scopeURN,
                                                                UUID scopeId, CoursewareElement walkable) {
        return learnerCoursewareService
                .getAncestry(deploymentId, walkable.getElementId(), walkable.getElementType())
                .map(ancestry -> {
                    if (ancestry.isEmpty()) {
                        throw new IllegalStateFault("ancestry list must contain at least 1 element");
                    }
                    return ancestry;
                })
                .flatMapMany(Flux::fromIterable)
                .map(ancestor -> buildStudentScopeTrace(deploymentId, studentId, scopeURN, scopeId, walkable, ancestor));
    }

    /**
     * Build a student scope trace for a particular ancestor.
     *
     * @param deploymentId the deployment id
     * @param studentId the student id to track the student scope for
     * @param scopeURN the scope urn that has been initialised
     * @param scopeId the id of the initialised scope
     * @param walkable the walkable associated with the scope urn
     * @param ancestor the ancestor to track this student scope by
     * @return a student scope trace
     */
    private StudentScopeTrace buildStudentScopeTrace(UUID deploymentId, UUID studentId, UUID scopeURN,
                                                     UUID scopeId, CoursewareElement walkable,
                                                     CoursewareElement ancestor) {
        return new StudentScopeTrace()
                .setRootId(ancestor.getElementId())
                .setStudentId(studentId)
                .setStudentScopeUrn(scopeURN)
                .setDeploymentId(deploymentId)
                .setScopeId(scopeId)
                .setElementId(walkable.getElementId())
                .setElementType(walkable.getElementType());
    }

    /**
     * Save a scope value for source
     *
     * @param scopeId  the scope id
     * @param sourceId the source id
     * @param data     the data
     * @return Mono of StudentScopeEntry
     */
    public Mono<StudentScopeEntry> createScopeEntry(UUID scopeId, UUID sourceId, String data) {
        checkArgument(scopeId != null, "scopeId can not be null");
        checkArgument(sourceId != null, "sourceId can not be null");
        checkArgument(data != null, "data can not be null");

        StudentScopeEntry scope = new StudentScopeEntry()
                .setScopeId(scopeId)
                .setSourceId(sourceId)
                .setData(data)
                .setId(UUIDs.timeBased());

        return studentScopeGateway.persist(scope).then(Mono.just(scope));
    }

    /**
     * Save a scope value for source
     *
     * @param scopeId  the scope id
     * @param sourceId the source id
     * @param data     the data
     * @param id       the scope entry id
     * @return Mono of StudentScopeEntry
     */
    public Mono<StudentScopeEntry> createScopeEntry(UUID scopeId, UUID sourceId, String data, UUID id) {
        checkArgument(scopeId != null, "scopeId can not be null");
        checkArgument(sourceId != null, "sourceId can not be null");
        checkArgument(data != null, "data can not be null");
        checkArgument(id != null,"missing id");

        StudentScopeEntry scope = new StudentScopeEntry()
                .setScopeId(scopeId)
                .setSourceId(sourceId)
                .setData(data)
                .setId(id);

        return studentScopeGateway.persist(scope).then(Mono.just(scope));
    }

    /**
     * Generate initial scope data for source from plugin output schema.
     * Return Empty String if either outputSchema or config  or both are empty string or null.
     *
     * @param scopeId          the scope id
     * @param registeredSource the registered source
     * @return Mono of StudentScopeEntry
     * @throws PluginNotFoundFault if plugin with given plugin and version doesn't exist
     * @throws VersionParserFault  if version contains unexpected symbols
     */
    public Mono<String> initFromOutputSchema(UUID scopeId, LearnerScopeReference registeredSource) {
        checkArgument(scopeId != null, "scopeId can not be null");
        checkArgument(registeredSource != null, "registeredSource can not be null");

        Mono<String> outputSchema = pluginService.findOutputSchema(registeredSource.getPluginId(), registeredSource.getPluginVersion())
                .defaultIfEmpty(StringUtils.EMPTY);

        Mono<String> config = learnerCoursewareService
                .fetchConfig(registeredSource.getElementId(), registeredSource.getDeploymentId(), registeredSource.getElementType())
                .defaultIfEmpty(StringUtils.EMPTY);

        return Mono.zip(outputSchema, config)
                .map(tuple2 -> pluginSchemaParser.extractOutputConfig(tuple2.getT1(), tuple2.getT2()));
    }

    /**
     * Fetch the latest student scope id for the given deployment, student and scope URN
     *
     * @param deploymentId the deployment id
     * @param studentId    the student id
     * @param scopeURN     the scope URN
     * @return Mono with StudentScope Id or empty mono if no scope exists
     */
    @Trace(async = true)
    public Mono<UUID> findScopeId(UUID deploymentId, UUID studentId, UUID scopeURN) {
        checkArgument(deploymentId != null, "deploymentId can not be null");
        checkArgument(studentId != null, "studentId can not be null");
        checkArgument(scopeURN != null, "scopeURN can not be null");

        return studentScopeGateway.fetchLatestScope(deploymentId, studentId, scopeURN)
                .map(StudentScope::getId);
    }

    /**
     * Fetch the latest scope entry for the given scope and source
     *
     * @param scopeId  the scope id
     * @param sourceId the source id
     * @return Mono of StudentScopeEntry, empty Mono if no entry exists
     */
    @Trace(async = true)
    public Mono<StudentScopeEntry> fetchScopeEntry(UUID scopeId, UUID sourceId) {
        checkArgument(scopeId != null, "scopeId can not be null");
        checkArgument(sourceId != null, "sourceId can not be null");

        return studentScopeGateway.fetchLatestEntry(scopeId, sourceId);
    }

    /**
     * Fetch the whole scope for the given deployment, student and scopeURN. If there is no scope - the scope is initialized.
     * If there is no data for sources in a scope - the initial data is generated from plugin output schema.
     *
     * @param deploymentId the deployment id
     * @param studentId    the student id
     * @param scopeURN     the scope URN
     * @return Flux of StudentScopePayload
     * @throws PluginNotFoundFault if plugin with given plugin and version doesn't exist
     * @throws VersionParserFault  if version contains unexpected symbols
     */
    public Flux<StudentScopePayload> fetchScope(UUID deploymentId, UUID studentId, UUID scopeURN, UUID changeId) {
        return getOrInitScopeId(studentId, scopeURN, deploymentId)
                .flatMapMany(scopeId -> learnerService.findAllRegistered(scopeURN, deploymentId, changeId)
                        .flatMap(registry -> fetchScopeEntry(scopeId, registry.getElementId())
                                .switchIfEmpty(initFromOutputSchema(scopeId, registry)
                                        .flatMap(data -> createScopeEntry(scopeId, registry.getElementId(), data))))
                        .map(entry -> StudentScopePayload.from(entry, scopeURN)));
    }

    /**
     * Set the student scope with some data
     *
     * @param deployment      the deployment object
     * @param studentId       the student id attempting to save the data
     * @param studentScopeURN the student scope reference
     * @param sourceId        the element id writing the data
     * @param data            the data to save in the scope
     * @return a mono of a student scope entry
     * @throws IllegalArgumentException when any of the required argument is <code>null</code>
     * @throws DataValidationException  when fails to validate the data
     */
    @Trace(async = true)
    public Mono<StudentScopeEntry> setStudentScope(Deployment deployment, UUID studentId, UUID studentScopeURN, UUID sourceId,
                                                   String data) {
        checkArgument(deployment != null, "missing deployment");
        checkArgument(studentId != null, "missing studentId");
        checkArgument(studentScopeURN != null, "missing studentScopeURN");
        checkArgument(sourceId != null, "missing sourceId");
        checkArgument(data != null, "missing data");

        UUID deploymentId = deployment.getId();
        UUID changeId = deployment.getChangeId();

        return validateData(studentScopeURN, sourceId, data, deploymentId, changeId)
                .then(saveStudentData(studentId, studentScopeURN, sourceId, data, deploymentId));
    }

    /**
     * Set the student scope with some data
     *
     * @param deployment      the deployment object
     * @param studentId       the student id attempting to save the data
     * @param studentScopeURN the student scope reference
     * @param sourceId        the element id writing the data
     * @param data            the data to save in the scope
     * @param id              the scope entry id
     * @return a mono of a student scope entry
     * @throws IllegalArgumentException when any of the required argument is <code>null</code>
     * @throws DataValidationException  when fails to validate the data
     */
    @Trace(async = true)
    public Mono<StudentScopeEntry> setStudentScope(Deployment deployment, UUID studentId, UUID studentScopeURN, UUID sourceId,
                                                   String data, UUID id) {
        checkArgument(deployment != null, "missing deployment");
        checkArgument(studentId != null, "missing studentId");
        checkArgument(studentScopeURN != null, "missing studentScopeURN");
        checkArgument(sourceId != null, "missing sourceId");
        checkArgument(data != null, "missing data");
        checkArgument(id != null,"missing id");

        UUID deploymentId = deployment.getId();
        UUID changeId = deployment.getChangeId();

        return validateData(studentScopeURN, sourceId, data, deploymentId, changeId)
                .then(saveStudentData(studentId, studentScopeURN, sourceId, data, deploymentId, id));
    }

    /**
     * Reset all the scopes that have been initialised in the subtree below the supplied elementId for the student.
     *
     * @param deploymentId the deployment id
     * @param elementId the root element to start looking for the initialised student scope from
     * @param studentId the student id to reset the scopes for
     * @return a flux with the ids of the newly initialised scopes
     */
    public Flux<StudentScope> resetScopesFor(final UUID deploymentId, final UUID elementId, final UUID studentId) {
         return studentScopeGateway.findInitialisedStudentScopeSubTree(deploymentId, studentId, elementId)
                .flatMap(studentScopeTrace -> createScope(deploymentId, studentId, studentScopeTrace.getStudentScopeUrn()));
    }

    /**
     * Find all the latest scope entry for a student over a deployment and a particular student scope URN
     *
     * @param deploymentId the deployment id
     * @param studentId the student to find the latest scope data entries for
     * @param studentScopeURN the student scope urn the data is written to
     * @return a mono map of source id and scope data, or an empty map when either the scope id is not found or scope data
     * entries are not found
     */
    @Trace(async = true)
    public Mono<Map<UUID, String>> findLatestEntries(UUID deploymentId, UUID studentId, UUID studentScopeURN) {
        return findScopeId(deploymentId, studentId, studentScopeURN)
                .flux()
                .flatMap(studentScopeGateway::fetchLatestEntries)
                .map(studentScopeData -> {
                    Map<UUID, String> map = new HashMap<>();
                    map.put(studentScopeData.getSourceId(), studentScopeData.getData());
                    return map;
                }).reduce((prevMap, nextMap) -> {
                    prevMap.putAll(nextMap);
                    return prevMap;
                }).defaultIfEmpty(new HashMap<>());
    }

    /**
     * Find the {@link StudentScope#getId()} so that a student scope entry can be saved with the data
     *
     * @param studentId       the student to save the scope entry for
     * @param studentScopeURN the student scope reference
     * @param sourceId        the element writing to the scope
     * @param data            the data to set to the student scope
     * @param deploymentId    the deployment id
     * @return a mono of a student scope entry object
     */
    private Mono<StudentScopeEntry> saveStudentData(UUID studentId, UUID studentScopeURN, UUID sourceId, String data,
                                                    UUID deploymentId) {
        return getOrInitScopeId(studentId, studentScopeURN, deploymentId)
                .flatMap(scopeId -> createScopeEntry(scopeId, sourceId, data));
    }

    /**
     * Find the {@link StudentScope#getId()} so that a student scope entry can be saved with the data
     *
     * @param studentId       the student to save the scope entry for
     * @param studentScopeURN the student scope reference
     * @param sourceId        the element writing to the scope
     * @param data            the data to set to the student scope
     * @param deploymentId    the deployment id
     * @param id              the scope entry id
     * @return a mono of a student scope entry object
     */
    private Mono<StudentScopeEntry> saveStudentData(UUID studentId, UUID studentScopeURN, UUID sourceId, String data,
                                                    UUID deploymentId, UUID id) {
        return getOrInitScopeId(studentId, studentScopeURN, deploymentId)
                .flatMap(scopeId -> createScopeEntry(scopeId, sourceId, data, id));
    }

    /**
     * Try to find the scope id and if it is not found a new scope is created and the scope id returned
     *
     * @param studentId       the student id to find the scope for
     * @param studentScopeURN the scope reference
     * @param deploymentId    the deployment id
     * @return a mono of uuid representing the {@link StudentScope#getId()}
     */
    private Mono<UUID> getOrInitScopeId(UUID studentId, UUID studentScopeURN, UUID deploymentId) {
        return findScopeId(deploymentId, studentId, studentScopeURN)
                .switchIfEmpty(createScope(deploymentId, studentId, studentScopeURN).map(StudentScope::getId));
    }

    /**
     * Validate the data fields against the output schema fields
     *
     * @param studentScopeURN the student scope urn to find the registered element for
     * @param sourceId        the element id
     * @param data            the data to validate the fields for
     * @param deploymentId    the deployment id
     * @param changeId        the change id
     * @return a mono of void or an empty mono if the output schema is not found //TODO what todo when the outputSchema is not found?
     * @throws DataValidationException when:
     *                                 <ul>
     *                                 <li>The registered element is not found</li>
     *                                 <li>Either the data or the outputSchema have an invalid json format</li>
     *                                 <li>Invalid fields where found in the data json object</li>
     *                                 </ul>
     */
    @Trace(async = true)
    private Mono<String> validateData(UUID studentScopeURN, UUID sourceId, String data, UUID deploymentId, UUID changeId) {
        return findOutputSchema(studentScopeURN, sourceId, deploymentId, changeId)
                .doOnError(RegisteredElementNotFoundException.class, ex -> {
                    throw new DataValidationException(ex.getMessage(), ex);
                })
                .map(schema -> {
                    pluginSchemaParser.validateDataAgainstSchema(data, schema);
                    return schema;
                })
                .doOnError(JSONException.class, ex -> {
                    throw new DataValidationException("Invalid json format", ex);
                })
                .doOnError(InvalidFieldsException.class, ex -> {
                    throw new DataValidationException("data has invalid fields", ex);
                });
    }

    /**
     * Attempts to find the output schema via learner student scope registry
     *
     * @param studentScopeURN the student scope urn to find the registered element for
     * @param sourceId        the element id
     * @param deploymentId    the deployment id
     * @param changeId        the change id
     * @return a mono of string representing the outputSchema or an empty mono if the outputSchema is not found
     * @throws RegisteredElementNotFoundException when the registered element is not found
     */
    @Trace(async = true)
    private Mono<String> findOutputSchema(UUID studentScopeURN, UUID sourceId, UUID deploymentId, UUID changeId) {
        return learnerService.findRegisteredElement(studentScopeURN, deploymentId, changeId, sourceId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new RegisteredElementNotFoundException(
                            String.format("Element `%s` not found in registry for studentScopeUrn `%s`",
                                    sourceId, studentScopeURN), ex);
                })
                .flatMap(r -> pluginService.findOutputSchema(r.getPluginId(), r.getPluginVersion()));
    }

}
