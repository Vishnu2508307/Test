package com.smartsparrow.learner.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerElementMetaInformation;
import com.smartsparrow.learner.data.LearnerGateway;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareElementMetaInformationService {

    private final CoursewareGateway coursewareGateway;
    private final LearnerGateway learnerGateway;

    @Inject
    public CoursewareElementMetaInformationService(final CoursewareGateway coursewareGateway,
                                                   final LearnerGateway learnerGateway) {
        this.coursewareGateway = coursewareGateway;
        this.learnerGateway = learnerGateway;
    }

    /**
     * Create and persist courseware element meta information to the database
     *
     * @param elementId the element id to create the meta information for
     * @param key       the name if the meta info
     * @param value     the value of the meta info
     * @return a mono with the created courseware element meta info
     */
    public Mono<CoursewareElementMetaInformation> createMetaInfo(final UUID elementId, final String key, final String value) {
        final CoursewareElementMetaInformation info = new CoursewareElementMetaInformation()
                .setElementId(elementId)
                .setKey(key)
                .setValue(value);

        return coursewareGateway.persist(info)
                .then(Mono.just(info));
    }

    /**
     * Find all the meta information given an element id and publish each found record as a learner element meta info
     *
     * @param elementId  the element to find and publish all the meta information for
     * @param deployment the deployment to publish the meta info to
     * @return a flux with the published meta information
     */
    public Flux<LearnerElementMetaInformation> publish(final UUID elementId, final Deployment deployment) {
        // find all the meta information
        return coursewareGateway.fetchAllMetaInformation(elementId)
                .flatMap(coursewareElementMetaInformation -> {
                    // create the learner element meta information
                    LearnerElementMetaInformation toPublish = new LearnerElementMetaInformation()
                            .setChangeId(deployment.getChangeId())
                            .setDeploymentId(deployment.getId())
                            .setElementId(elementId)
                            .setKey(coursewareElementMetaInformation.getKey())
                            .setValue(coursewareElementMetaInformation.getValue());

                    // persist the meta info to publish
                    return learnerGateway.persist(toPublish)
                            // return the published meta information
                            .thenMany(Flux.just(toPublish));
                });
    }

    /**
     * Find all the meta information given an element id and duplicate each found record with the new element id
     *
     * @param elementId    the id of the element to duplicate the meta info from
     * @param newElementId the new id (duplicated) of the element to store the meta info for
     * @return a flux with the duplicated meta info
     */
    @Trace(async = true)
    public Flux<CoursewareElementMetaInformation> duplicate(final UUID elementId, final UUID newElementId) {
        // find all the meta information
        return coursewareGateway.fetchAllMetaInformation(elementId)
                .flatMap(coursewareElementMetaInformation -> {
                    // duplicate the found meta info with the new element id
                    CoursewareElementMetaInformation duplicated = new CoursewareElementMetaInformation()
                            .setElementId(newElementId)
                            .setKey(coursewareElementMetaInformation.getKey())
                            .setValue(coursewareElementMetaInformation.getValue());

                    // persist the duplicated meta info
                    return coursewareGateway.persist(duplicated)
                            // return the duplicated meta info
                            .thenMany(Flux.just(duplicated));
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch meta information for a given element by meta key.
     *
     * @param elementId the id of the element to find the meta information for
     * @param key       the key (name) of the meta information to fetch
     * @return a mono with the courseware element meta information or an empty object when not found
     */
    @Trace(async = true)
    public Mono<CoursewareElementMetaInformation> findMetaInfo(final UUID elementId, final String key) {
        return coursewareGateway.fetchMetaInformation(elementId, key)
                // default to a meta info object with null value when not found
                .defaultIfEmpty(new CoursewareElementMetaInformation()
                        .setElementId(elementId)
                        .setKey(key))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch multiple meta info for a courseware element
     *
     * @param elementId the id of the element to find the meta information for
     * @param keys a list of keys (names) of the meta information to fetch
     * @return a flux with the requested meta info
     */
    @Trace(async = true)
    public Flux<CoursewareElementMetaInformation> findMetaInfo(final UUID elementId, final List<String> keys) {
        return keys.stream()
                .map(key -> findMetaInfo(elementId, key)
                        .flux()
                        .doOnEach(ReactiveTransaction.linkOnNext()))
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
