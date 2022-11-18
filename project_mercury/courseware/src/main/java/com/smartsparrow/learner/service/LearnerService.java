package com.smartsparrow.learner.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.courseware.data.LearnerElementConfigurationField;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.data.LearnerDocumentItemLinkGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemTag;
import com.smartsparrow.learner.data.LearnerGateway;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.data.LearnerWalkable;

import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerService {

    private final CoursewareGateway coursewareGateway;
    private final LearnerGateway learnerGateway;
    private final DocumentItemLinkService documentItemLinkService;
    private final LearnerDocumentItemLinkGateway learnerDocumentItemLinkGateway;
    private final PluginService pluginService;

    @Inject
    public LearnerService(CoursewareGateway coursewareGateway,
                          LearnerGateway learnerGateway,
                          DocumentItemLinkService documentItemLinkService,
                          LearnerDocumentItemLinkGateway learnerDocumentItemLinkGateway,
                          PluginService pluginService) {
        this.coursewareGateway = coursewareGateway;
        this.learnerGateway = learnerGateway;
        this.documentItemLinkService = documentItemLinkService;
        this.learnerDocumentItemLinkGateway = learnerDocumentItemLinkGateway;
        this.pluginService = pluginService;
    }

    /**
     * Find all the registered elements to the student scope of this walkable and for each one persist a
     * {@link LearnerScopeReference} instance in the learner env
     *
     * @param walkable the walkable to find the registered elements for
     * @param deployment the deployment info
     * @return a flux of void
     */
    public Flux<Void> replicateRegisteredStudentScopeElements(final LearnerWalkable walkable,
                                                              final Deployment deployment,
                                                              final boolean lockPluginVersionEnabled) {
        return coursewareGateway.findRegisteredElements(walkable.getStudentScopeURN())
                .map(scopeReference -> new LearnerScopeReference()
                        .setElementId(scopeReference.getElementId())
                        .setElementType(scopeReference.getElementType())
                        .setScopeURN(scopeReference.getScopeURN())
                        .setPluginId(scopeReference.getPluginId())
                        .setPluginVersion(pluginService.resolvePluginVersion(scopeReference.getPluginId(),
                                                                             scopeReference.getPluginVersion(),
                                                                             lockPluginVersionEnabled))
                        .setDeploymentId(deployment.getId())
                        .setChangeId(deployment.getChangeId()))
                .flatMap(learnerGateway::persist);
    }

    /**
     * Find the entry for the registered element to the given student scope urn
     *
     * @param studentScopeURN the student scope urn the element should be registered to
     * @param deploymentId the deployment id to search the registered reference in
     * @param changeId the change id to search the registered reference for
     * @param elementId the element that should be registered to the student scope urn supplied
     * @return a mono of learner scope reference holding the registration info
     */
    @Trace(async = true)
    public Mono<LearnerScopeReference> findRegisteredElement(final UUID studentScopeURN, final UUID deploymentId, final UUID changeId, final UUID elementId) {
        return learnerGateway.findRegisteredElement(studentScopeURN, deploymentId, changeId, elementId);
    }

    /**
     * Find all registered elements to the given student scope urn
     *
     * @param studentScopeURN the student scope urn elements should be registered to
     * @param deploymentId the deployment id to search the registered references in
     * @param changeId the change id to search the registered references for
     * @return a flux of learner scope reference holding the registration info
     */
    public Flux<LearnerScopeReference> findAllRegistered(final UUID studentScopeURN, final UUID deploymentId, final UUID changeId) {
        return learnerGateway.findAllRegistered(studentScopeURN, deploymentId, changeId);
    }

    /**
     * Find a walkable by its own student scope urn and deployment id
     *
     * @param studentScopeURN the student scope urn to find the element for
     * @param deploymentId the deployment id
     * @return a mono of courseware element
     */
    public Mono<CoursewareElement> findWalkable(final UUID studentScopeURN, final UUID deploymentId) {
        return learnerGateway.findWalkable(studentScopeURN, deploymentId);
    }

    /**
     * Find all the linked document items to the courseware element and publish those links to the learner
     *
     * @param elementId the element to publish the linked document items for
     * @param elementType the element type
     * @param deployment the deployment the links should be published for
     * @return a flux of void
     */
    public Flux<Void> publishDocumentItemLinks(final UUID elementId, final CoursewareElementType elementType,
                                               final Deployment deployment) {

        return documentItemLinkService.findAll(elementId)
                .flatMap(documentItemTag -> {

                    LearnerDocumentItemTag tag = new LearnerDocumentItemTag()
                            .setDeploymentId(deployment.getId())
                            .setChangeId(deployment.getChangeId())
                            .setDocumentId(documentItemTag.getDocumentId())
                            .setDocumentItemId(documentItemTag.getDocumentItemId())
                            .setElementId(elementId)
                            .setElementType(elementType);

                    return learnerDocumentItemLinkGateway.persist(tag);
                });
    }

    /**
     * Find all the extracted configuration fields for a courseware element and publish those
     *
     * @param deployment the deployment the element is being published to
     * @param elementId the element id to find the configuration fields for
     * @return a flux of void
     */
    public Flux<Void> publishConfigurationFields(final Deployment deployment, final UUID elementId) {
        return coursewareGateway.fetchConfigurationFields(elementId)
                .map(configurationField -> new LearnerElementConfigurationField()
                        .setDeploymentId(deployment.getId())
                        .setChangeId(deployment.getChangeId())
                        .setElementId(elementId)
                        .setFieldName(configurationField.getFieldName())
                        .setFieldValue(configurationField.getFieldValue()))
                .flatMap(learnerGateway::persist);
    }

    /**
     * Find the configuration field values for a specific element in a deployment. When a field is not found a
     * {@link ConfigurationField} object is returned with a <code>null</code> {@link ConfigurationField#getFieldValue()}
     *
     * @param deploymentId the deployment the element belongs to
     * @param changeId the change id of the deployment
     * @param elementId the element id to find the configuration fields for
     * @param fieldNames the configuration fields to find the values for
     * @return a flux of configuration fields
     */
    @Trace(async = true)
    public Flux<ConfigurationField> fetchFields(final UUID deploymentId, final UUID changeId, final UUID elementId, final List<String> fieldNames) {

        return fieldNames.stream()
                .map(fieldName -> learnerGateway.findConfigurationField(deploymentId, changeId, elementId, fieldName)
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .defaultIfEmpty(new ConfigurationField()
                                .setFieldName(fieldName))
                        .flux())
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty());

    }


    /**
     * Find the courseware element type for a given id and deployment
     *
     * @param elementId the element id to find the type for
     * @param deploymentId the deployment id to find element by
     * @return a mono with the courseware element
     * @throws NotFoundFault when the element type not found
     */
    public Mono<LearnerCoursewareElement> findElementByDeployment(final UUID elementId, final UUID deploymentId) {
        return learnerGateway.fetchElementByDeployment(elementId, deploymentId);
    }
}
