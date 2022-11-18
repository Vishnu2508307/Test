package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CompetencyMetGateway {

    private static final Logger log = LoggerFactory.getLogger(CompetencyMetGateway.class);

    private final Session session;
    private final CompetencyMetMaterializer competencyMetMaterializer;
    private final CompetencyMetMutator competencyMetMutator;
    private final CompetencyMetByStudentMaterializer competencyMetByStudentMaterializer;
    private final CompetencyMetByStudentMutator competencyMetByStudentMutator;
    private final CompetencyMetByStudentHistoryMaterializer competencyMetByStudentHistoryMaterializer;
    private final CompetencyMetByStudentHistoryMutator competencyMetByStudentHistoryMutator;

    @Inject
    public CompetencyMetGateway(Session session,
                                CompetencyMetMaterializer competencyMetMaterializer,
                                CompetencyMetMutator competencyMetMutator,
                                CompetencyMetByStudentMaterializer competencyMetByStudentMaterializer,
                                CompetencyMetByStudentMutator competencyMetByStudentMutator,
                                CompetencyMetByStudentHistoryMaterializer competencyMetByStudentHistoryMaterializer,
                                CompetencyMetByStudentHistoryMutator competencyMetByStudentHistoryMutator) {
        this.session = session;
        this.competencyMetMaterializer = competencyMetMaterializer;
        this.competencyMetMutator = competencyMetMutator;
        this.competencyMetByStudentMaterializer = competencyMetByStudentMaterializer;
        this.competencyMetByStudentMutator = competencyMetByStudentMutator;
        this.competencyMetByStudentHistoryMaterializer = competencyMetByStudentHistoryMaterializer;
        this.competencyMetByStudentHistoryMutator = competencyMetByStudentHistoryMutator;
    }

    /**
     * Persist the competency met for a learner
     *
     * @param competencyMet - The competency object {@link CompetencyMet}
     * @return {@link Flux<Void>} - a flux of Void
     */
    @Trace(async = true)
    public Flux<Void> persist(final CompetencyMet competencyMet) {
        return Mutators.execute(session,
                Flux.just(competencyMetMutator.upsert(competencyMet),
                        competencyMetByStudentMutator.upsert(competencyMet),
                        competencyMetByStudentHistoryMutator.upsert(competencyMet)))
                .doOnError(throwable -> {
                    log.warn(throwable.getMessage());
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a competency met by metId
     *
     * @param metId - the id of competency met
     * @return {@link Mono<CompetencyMet>} a mono of competency met
     */
    public Mono<CompetencyMet> findById(final UUID metId) {
        return ResultSets
                .query(session, competencyMetMaterializer.findById(metId))
                .flatMapIterable(row -> row)
                .map(competencyMetMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the learners achievements
     *
     * @param studentId - the account id to find the competency met for
     * @return {@link Flux<CompetencyMetByStudent>}
     */
    public Flux<CompetencyMetByStudent> findAll(final UUID studentId) {
        return ResultSets
                .query(session, competencyMetByStudentMaterializer.findByStudent(studentId))
                .flatMapIterable(one -> one)
                .map(competencyMetByStudentMaterializer::fromRow);
    }

    /**
     * Find all the learners achievements for the given competency item
     *
     * @param studentId  - the account id to find the competency met for
     * @param documentId - the document id to find the competency met for
     * @param itemId     - the document id to find the competency met for
     * @return {@link Flux<CompetencyMetByStudent>}
     */
    public Flux<CompetencyMetByStudent> findAll(final UUID studentId, final UUID documentId, final UUID itemId) {
        return ResultSets
                .query(session, competencyMetByStudentHistoryMaterializer.findAll(studentId, documentId, itemId))
                .flatMapIterable(one -> one)
                .map(competencyMetByStudentHistoryMaterializer::fromRow);
    }

    /**
     * Find the latest competency met entry for a student over a given document item
     *
     * @param studentId the student to find the latest competency met entry for
     * @param documentId the document id the document item belongs to
     * @param itemId the document item id to find the met entry for the student
     * @return a mono of competency met by student
     */
    @Trace(async = true)
    public Mono<CompetencyMetByStudent> findLatest(final UUID studentId, final UUID documentId, final UUID itemId) {
        return ResultSets.query(session, competencyMetByStudentMaterializer.findByDocumentItem(studentId, documentId, itemId))
                .flatMapIterable(row->row)
                .map(competencyMetByStudentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the latest competency met entries for a student over a complete document
     *
     * @param studentId the student to find the competency met entries for
     * @param documentId the document to find the student competency met entries over
     * @return a flux of competency met by student
     */
    public Flux<CompetencyMetByStudent> findAllLatest(final UUID studentId, final UUID documentId) {
        return ResultSets.query(session, competencyMetByStudentMaterializer.findByDocument(studentId, documentId))
                .flatMapIterable(row->row)
                .map(competencyMetByStudentMaterializer::fromRow);
    }

    /**
     * <p>Delete competecy met entries for specif student, document, item_id and met_id</p>
     *
     * <p><b>WARNING:</b> this is not a regular operation. Only used for data maintenance in special cases.
     * <BR><b>DO NOT DELETE</b> if you are unsure what you're doing.</p>
     *
     *
     * @param competencyMet the competency met object to be deleted
     *
     */
    public Flux<Void> delete(final CompetencyMet competencyMet) {
        Flux<Statement> deleteStmts = Flux.just(
                competencyMetMutator.delete(competencyMet),
                competencyMetByStudentMutator.delete(competencyMet),
                competencyMetByStudentHistoryMutator.delete(competencyMet));

        return Mutators.execute(session, deleteStmts)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting competency met rows for %s", competencyMet.toString()),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

}
