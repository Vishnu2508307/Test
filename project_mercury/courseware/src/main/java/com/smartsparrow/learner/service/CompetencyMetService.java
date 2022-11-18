package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.CompetencyMetGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CompetencyMetService {

    private final CompetencyMetGateway learnerCompetencyMetGateway;

    @Inject
    public CompetencyMetService(CompetencyMetGateway learnerCompetencyMetGateway) {
        this.learnerCompetencyMetGateway = learnerCompetencyMetGateway;
    }

    /**
     * Create and persist a competency met for a learner
     *
     * @param studentId             the account id of the learner
     * @param deploymentId          the deployment id of the courseware
     * @param changeId              the change id of the deployment
     * @param coursewareElementId   the element id for which the competency is met
     * @param coursewareElementType the coursewareElementType for which the competency is met
     * @param evaluationId          the identifier for the evaluation
     * @param documentId            the document id of which the competency is part of
     * @param documentVersionId     the document version id of the competency
     * @param documentItemId        the item id which is the competency
     * @param attemptId             the attempt id for the learner
     * @param value                 the value for the competency met
     * @param confidence            the confidence level for the competency met
     * @return {@link CompetencyMet}
     */
    @Trace(async = true)
    public Mono<CompetencyMet> create(@Nonnull UUID studentId,
            @Nullable UUID deploymentId,
            @Nullable UUID changeId,
            @Nullable UUID coursewareElementId,
            @Nullable CoursewareElementType coursewareElementType,
            @Nullable UUID evaluationId,
            @Nonnull UUID documentId,
            @Nonnull UUID documentVersionId,
            @Nonnull UUID documentItemId,
            @Nullable UUID attemptId,
            @Nonnull Float value,
            @Nonnull Float confidence) {

        UUID competencyMetId = UUIDs.timeBased();

        CompetencyMet competencyMet = new CompetencyMet()
                .setId(competencyMetId)
                .setStudentId(studentId)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setCoursewareElementId(coursewareElementId)
                .setCoursewareElementType(coursewareElementType)
                .setEvaluationId(evaluationId)
                .setDocumentId(documentId)
                .setDocumentVersionId(documentVersionId)
                .setDocumentItemId(documentItemId)
                .setAttemptId(attemptId)
                .setValue(value)
                .setConfidence(confidence);

        return learnerCompetencyMetGateway.persist(competencyMet)
                .then(Mono.just(competencyMet));

    }

    /**
     * Create and persist a competency met for a learner with supplied award timestamp. Should be used in very limited
     * cases, mainly importing customer legacy data
     *
     * @param studentId             the account id of the learner
     * @param deploymentId          the deployment id of the courseware
     * @param changeId              the change id of the deployment
     * @param coursewareElementId   the element id for which the competency is met
     * @param coursewareElementType the coursewareElementType for which the competency is met
     * @param evaluationId          the identifier for the evaluation
     * @param documentId            the document id of which the competency is part of
     * @param documentVersionId     the document version id of the competency
     * @param documentItemId        the item id which is the competency
     * @param attemptId             the attempt id for the learner
     * @param value                 the value for the competency met
     * @param confidence            the confidence level for the competency met
     * @param awardedAt             the timestamp of when the competency was awarded
     * @return {@link CompetencyMet}
     */
    public Mono<CompetencyMet> create(@Nonnull UUID studentId,
                                      @Nullable UUID deploymentId,
                                      @Nullable UUID changeId,
                                      @Nullable UUID coursewareElementId,
                                      @Nullable CoursewareElementType coursewareElementType,
                                      @Nullable UUID evaluationId,
                                      @Nonnull UUID documentId,
                                      @Nonnull UUID documentVersionId,
                                      @Nonnull UUID documentItemId,
                                      @Nullable UUID attemptId,
                                      @Nonnull Float value,
                                      @Nonnull Float confidence,
                                      @Nonnull Long awardedAt) {

        // Generate UUID based off the supplied awardedAt timestamp
        UUID competencyMetId = com.smartsparrow.util.UUIDs.timeBased(awardedAt);

        CompetencyMet competencyMet = new CompetencyMet()
                .setId(competencyMetId)
                .setStudentId(studentId)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setCoursewareElementId(coursewareElementId)
                .setCoursewareElementType(coursewareElementType)
                .setEvaluationId(evaluationId)
                .setDocumentId(documentId)
                .setDocumentVersionId(documentVersionId)
                .setDocumentItemId(documentItemId)
                .setAttemptId(attemptId)
                .setValue(value)
                .setConfidence(confidence);

        return learnerCompetencyMetGateway.persist(competencyMet)
                .then(Mono.just(competencyMet));

    }

    /**
     * Fetch a competency met for a learner by the id
     *
     * @param metId - the id when a competency is met
     * @return {@link Mono<CompetencyMet>} a mono of CompetencyMet
     */
    public Mono<CompetencyMet> findCompetencyMet(UUID metId) {
        affirmNotNull(metId, "metId is required");

        return learnerCompetencyMetGateway.findById(metId);
    }

    /**
     * Fetch all the competencies met by a learner
     *
     * @param studentId - the account id of the learner to fetch the competencies for
     * @return {@link Flux<CompetencyMet>} a flux of CompetencyMet
     */
    public Flux<CompetencyMetByStudent> findCompetenciesMetByStudent(UUID studentId) {
        affirmNotNull(studentId, "studentId is required");

        return learnerCompetencyMetGateway.findAll(studentId);
    }

    /**
     * Fetch all competencies met by a learner for a document item
     *
     * @param studentId  - the account id of the learner to fetch the competencies for
     * @param documentId - the document id which is the competency
     * @param itemId     - the item id which is the competency
     * @return {@link Flux<CompetencyMet>} a flux of CompetencyMet
     */
    public Flux<CompetencyMetByStudent> findCompetenciesMetByDocumentItem(UUID studentId,
                                                                          UUID documentId,
                                                                          UUID itemId) {

        affirmNotNull(studentId, "studentId is required");
        affirmNotNull(documentId, "documentId is required");
        affirmNotNull(itemId, "itemId is required");

        return learnerCompetencyMetGateway.findAll(studentId, documentId, itemId);
    }

    /**
     * Fetch the latest competency met by a learner over a given document item
     *
     * @param studentId the student to find the met entry for
     * @param documentId the document the document item belongs to
     * @param itemId the item id to find the student met entry for
     * @return a mono of competency met by student
     */
    @Trace(async = true)
    public Mono<CompetencyMetByStudent> findLatest(@Nonnull final UUID studentId,
                                                   @Nonnull final UUID documentId,
                                                   @Nonnull final UUID itemId) {
        return learnerCompetencyMetGateway.findLatest(studentId, documentId, itemId);
    }

    /**
     * Fetch all the latest competency met entries for a learner over a given document
     *
     * @param studentId the student to find the competency met entries for
     * @param documentId the document to find the student competency met entries over
     * @return a flux of competency met by student
     */
    public Flux<CompetencyMetByStudent> findLatest(@Nonnull final UUID studentId,
                                                   @Nonnull final UUID documentId) {
        return learnerCompetencyMetGateway.findAllLatest(studentId, documentId);
    }

    /**
     * Fetch all the latest competency met entries for a learner
     *
     * @param studentId the student id to fetch the competency met entries for
     * @return a flux of competency met by student
     */
    public Flux<CompetencyMetByStudent> findLatest(@Nonnull final UUID studentId) {
        return learnerCompetencyMetGateway.findAll(studentId);
    }

    /**
     * <p>Delete competency met entries for specif student, document, item_id and met_id</p>
     *
     * <p><b>WARNING:</b> this is not a regular operation. Only used for data maintenance in special cases.
     * <BR><b>DO NOT DELETE</b> if you are unsure what you're doing.</p>
     *
     *
     * @param studentId             the account id of the learner
     * @param documentId            the document id of which the competency is part of
     * @param documentItemId        the item id which is the competency
     * @param metId                 the met id of the entry being deleted
     *
     */
    public Flux<Void> delete(@Nonnull UUID studentId,
                            @Nonnull UUID documentId,
                            @Nonnull UUID documentItemId,
                            @Nonnull UUID metId) {

        affirmNotNull(studentId, "studentId is required");
        affirmNotNull(documentId, "documentId is required");
        affirmNotNull(documentItemId, "documentItemId is required");
        affirmNotNull(metId, "metId is required");

        CompetencyMet competencyMet = new CompetencyMet()
                .setStudentId(studentId)
                .setDocumentId(documentId)
                .setDocumentItemId(documentItemId)
                .setId(metId);
        return learnerCompetencyMetGateway.delete(competencyMet);
    }
}
