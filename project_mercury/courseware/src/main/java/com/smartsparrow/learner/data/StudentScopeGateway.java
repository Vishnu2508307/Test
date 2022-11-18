package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class StudentScopeGateway {

    private final Session session;
    private final StudentScopeEntryBySourceMutator studentScopeEntryByScopeMutator;
    private final StudentScopeEntryBySourceMaterializer studentScopeEntryByScopeMaterializer;
    private final StudentScopeMutator studentScopeMutator;
    private final StudentScopeMaterializer studentScopeMaterializer;
    private final StudentScopeTreeByElementMaterializer studentScopeTreeByElementMaterializer;
    private final StudentScopeTreeByElementMutator studentScopeTreeByElementMutator;
    private final LatestStudentScopeEntryMaterializer latestStudentScopeEntryMaterializer;
    private final LatestStudentScopeEntryMutator latestStudentScopeEntryMutator;

    @Inject
    public StudentScopeGateway(Session session,
                               StudentScopeEntryBySourceMutator studentScopeEntryByScopeMutator,
                               StudentScopeEntryBySourceMaterializer studentScopeEntryByScopeMaterializer,
                               StudentScopeMutator studentScopeMutator,
                               StudentScopeMaterializer studentScopeMaterializer,
                               StudentScopeTreeByElementMaterializer studentScopeTreeByElementMaterializer,
                               StudentScopeTreeByElementMutator studentScopeTreeByElementMutator,
                               LatestStudentScopeEntryMaterializer latestStudentScopeEntryMaterializer,
                               LatestStudentScopeEntryMutator latestStudentScopeEntryMutator) {
        this.session = session;
        this.studentScopeEntryByScopeMutator = studentScopeEntryByScopeMutator;
        this.studentScopeEntryByScopeMaterializer = studentScopeEntryByScopeMaterializer;
        this.studentScopeMutator = studentScopeMutator;
        this.studentScopeMaterializer = studentScopeMaterializer;
        this.studentScopeTreeByElementMaterializer = studentScopeTreeByElementMaterializer;
        this.studentScopeTreeByElementMutator = studentScopeTreeByElementMutator;
        this.latestStudentScopeEntryMaterializer = latestStudentScopeEntryMaterializer;
        this.latestStudentScopeEntryMutator = latestStudentScopeEntryMutator;
    }

    /**
     * Persist a student scope. The student scope is also tracked by each of its ancestry element
     *
     * @param studentScope the student scope to save
     * @param toTrack the ancestry element to track this scope for
     * @return a flux of void
     */
    public Flux<Void> persist(final StudentScope studentScope, List<StudentScopeTrace> toTrack) {

        Flux<Statement> toTrackStatements = Mono.just(toTrack).flatMapMany(Flux::fromIterable).map(studentScopeTreeByElementMutator::upsert);

        Flux<Statement> studentScopeStatement = Flux.just(studentScopeMutator.upsert(studentScope));

        return Mutators.execute(session, Flux.merge(toTrackStatements, studentScopeStatement));
    }

    /**
     * Persist student scope entry
     *
     * @param studentScopeEntry student scope contains information about target scope, source and data
     */
    public Flux<Void> persist(StudentScopeEntry studentScopeEntry) {
        return Mutators.execute(session, Flux.just(
                studentScopeEntryByScopeMutator.upsert(studentScopeEntry),
                latestStudentScopeEntryMutator.upsert(studentScopeEntry))
        );
    }

    /**
     * Fetch the latest scope for the deployment, student, scope URN
     *
     * @param deploymentId the deployment id
     * @param accountId    the student id
     * @param scopeURN     the scope URN
     * @return Mono with StudentScope or empty if no student scope found
     */
    @Trace(async = true)
    public Mono<StudentScope> fetchLatestScope(UUID deploymentId, UUID accountId, UUID scopeURN) {
        return ResultSets.query(session, studentScopeMaterializer.findLatest(deploymentId, accountId, scopeURN))
                .flatMapIterable(row -> row)
                .map(studentScopeMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch the latest scope for the source
     *
     * @param scopeId  the scope id
     * @param sourceId the source id
     * @return Mono with StudentScopeEntry or empty if no student scope fro entry is found
     */
    @Trace(async = true)
    public Mono<StudentScopeEntry> fetchLatestEntry(UUID scopeId, UUID sourceId) {
        return ResultSets.query(session, studentScopeEntryByScopeMaterializer.findLatest(scopeId, sourceId))
                .flatMapIterable(row -> row)
                .map(studentScopeEntryByScopeMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the subtree of initialised student scope for a student at a particular courseware element on a deployment.
     *
     * @param deploymentId the deployment id
     * @param studentId the student id to find the initialised scope for
     * @param elementId the element to start looking for the student scope subtree from
     * @return a flux of tracked student scope
     */
    public Flux<StudentScopeTrace> findInitialisedStudentScopeSubTree(final UUID deploymentId, final UUID studentId, final UUID elementId) {
        return ResultSets.query(session, studentScopeTreeByElementMaterializer.findSubTree(deploymentId, studentId, elementId))
                .flatMapIterable(row -> row)
                .map(studentScopeTreeByElementMaterializer::fromRow);
    }

    /**
     * Find all the latest scope entries for a scope id that have been written by different source ids
     *
     * @param scopeId the scope id to find all the latest entries for
     * @return a flux of latest student scope entries
     */
    @Trace(async = true)
    public Flux<StudentScopeData> fetchLatestEntries(UUID scopeId) {
        return ResultSets.query(session, latestStudentScopeEntryMaterializer.findByScope(scopeId))
                .flatMapIterable(row -> row)
                .map(latestStudentScopeEntryMaterializer::fromRow);
    }
}
