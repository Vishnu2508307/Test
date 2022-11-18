package com.smartsparrow.courseware.data;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class CoursewareElementDescriptionGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareChangeLogGateway.class);

    private final Session session;
    private final CoursewareElementDescriptionMaterializer coursewareElementDescriptionMaterializer;
    private final CoursewareElementDescriptionMutator coursewareElementDescriptionMutator;

    @Inject
    public CoursewareElementDescriptionGateway(final Session session,
                                               final CoursewareElementDescriptionMaterializer coursewareElementDescriptionMaterializer,
                                               final CoursewareElementDescriptionMutator coursewareElementDescriptionMutator) {
        this.session = session;
        this.coursewareElementDescriptionMaterializer = coursewareElementDescriptionMaterializer;
        this.coursewareElementDescriptionMutator = coursewareElementDescriptionMutator;
    }

    /**
     * fetch the description details by element id.
     *
     * @param elementId ,the element id to find the element details.
     * @return flux of courseware description by element.
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> fetchDescriptionByElement(UUID elementId) {
        return ResultSets.query(session, coursewareElementDescriptionMaterializer.findById(elementId))
                .flatMapIterable(row -> row)
                .map(coursewareElementDescriptionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the courseware element description details by element.
     *
     * @param coursewareElementDescription courseware element description by element object.
     */
    public Flux<Void> persist(CoursewareElementDescription coursewareElementDescription) {
        return Mutators.execute(session, Flux.just(coursewareElementDescriptionMutator.upsert(coursewareElementDescription)))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving courseware element description by element %s",
                            coursewareElementDescription), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
