package com.smartsparrow.courseware.data;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.UUID;

@Singleton
public class CoursewareChangeLogGateway {

    private static final Logger log = LoggerFactory.getLogger(CoursewareChangeLogGateway.class);

    private final Session session;
    private final CoursewareChangeLogProjectMaterializer coursewareChangeLogProjectMaterializer;
    private final CoursewareChangeLogProjectMutator coursewareChangeLogProjectMutator;
    private final CoursewareChangeLogElementMaterializer coursewareChangeLogElementMaterializer;
    private final CoursewareChangeLogElementMutator coursewareChangeLogElementMutator;

    @Inject
    public CoursewareChangeLogGateway(final Session session,
                                      final CoursewareChangeLogProjectMaterializer coursewareChangeLogProjectMaterializer,
                                      final CoursewareChangeLogProjectMutator coursewareChangeLogProjectMutator,
                                      final CoursewareChangeLogElementMaterializer coursewareChangeLogElementMaterializer,
                                      final CoursewareChangeLogElementMutator coursewareChangeLogElementMutator) {
        this.session = session;
        this.coursewareChangeLogProjectMaterializer = coursewareChangeLogProjectMaterializer;
        this.coursewareChangeLogProjectMutator = coursewareChangeLogProjectMutator;
        this.coursewareChangeLogElementMaterializer = coursewareChangeLogElementMaterializer;
        this.coursewareChangeLogElementMutator = coursewareChangeLogElementMutator;
    }

    /**
     * fetch the change log details by project id.
     * @param projectId the project id to find the change log details.
     * @param limit , query fetch limit.
     * @return flux of courseware change log by project .
     */
    public Flux<ChangeLogByProject> fetchChangeLogByProject(UUID projectId, int limit) {
        return ResultSets.query(session, coursewareChangeLogProjectMaterializer.findChangeLogByProject(projectId, limit))
                .flatMapIterable(row -> row)
                .map(coursewareChangeLogProjectMaterializer::fromRow);
    }

    /**
     * Save the courseware change log details by project.
     * @param changeLogByProject changelog by project object.
     */
    public Flux<Void> persist(ChangeLogByProject changeLogByProject) {
        return Mutators.execute(session, Flux.just(coursewareChangeLogProjectMutator.upsert(changeLogByProject)))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving courseware change log by project %s",
                            changeLogByProject), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * fetch the change log details by element id.
     * @param elementId ,the element id to find the change log details.
     * @param limit , query fetch limit.
     * @return flux of courseware change log by element.
     */
    public Flux<ChangeLogByElement> fetchChangeLogByElement(UUID elementId, int limit) {
        return ResultSets.query(session, coursewareChangeLogElementMaterializer.findChangeLogByElement(elementId, limit))
                .flatMapIterable(row -> row)
                .map(coursewareChangeLogElementMaterializer::fromRow);
    }

    /**
     * Save the courseware change log details by element.
     * @param changeLogByElement changelog by element object.
     */
    public Flux<Void> persist(ChangeLogByElement changeLogByElement) {
        return Mutators.execute(session, Flux.just(coursewareChangeLogElementMutator.upsert(changeLogByElement)))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving courseware change log by element %s",
                            changeLogByElement), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
