package com.smartsparrow.learner.data;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.commons.collections4.map.HashedMap;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.UUID;

@Singleton
public class GradePassbackGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GradePassbackGateway.class);

    private final Session session;

    private final GradePassbackNotificationMutator gradePassbackNotifcationMutator;
    private final GradePassbackNotificationMaterializer gradePassbackNotificationMaterializer;
    private final GradePassbackNotificationByDeploymentMutator gradePassbackNotificationByDeploymentMutator;
    private final GradePassbackNotificationByDeploymentMaterializer gradePassbackNotificationByDeploymentMaterializer;

    @Inject
    public GradePassbackGateway(Session session,
                                GradePassbackNotificationMutator gradePassbackNotifcationMutator,
                                GradePassbackNotificationMaterializer gradePassbackNotificationMaterializer,
                                GradePassbackNotificationByDeploymentMutator gradePassbackNotificationByDeploymentMutator,
                                GradePassbackNotificationByDeploymentMaterializer gradePassbackNotificationByDeploymentMaterializer) {
        this.session = session;
        this.gradePassbackNotifcationMutator = gradePassbackNotifcationMutator;
        this.gradePassbackNotificationMaterializer = gradePassbackNotificationMaterializer;
        this.gradePassbackNotificationByDeploymentMutator = gradePassbackNotificationByDeploymentMutator;
        this.gradePassbackNotificationByDeploymentMaterializer = gradePassbackNotificationByDeploymentMaterializer;
    }

    public Flux<Void> persist(final GradePassbackNotification notification) {
        return Mutators.execute(session,
                    Flux.just(gradePassbackNotifcationMutator.upsert(notification)
                            , gradePassbackNotificationByDeploymentMutator.upsert(notification)
                ))
                .doOnEach(log.reactiveErrorThrowable("error while saving notification", throwable -> new HashedMap<String, Object>() {
                    {
                        put("notificationId", notification.getNotificationId());
                        put("deploymentId", notification.getDeploymentId());
                        put("changeId", notification.getChangeId());
                        put("studentId", notification.getStudentId());
                        put("coursewareElementId", notification.getCoursewareElementId());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    public Mono<GradePassbackNotification> findNotifcationById(final UUID notifcationId) {
        return ResultSets.query(session, gradePassbackNotificationMaterializer.findById(notifcationId))
                .flatMapIterable(row -> row)
                .map(gradePassbackNotificationMaterializer::fromRow)
                .singleOrEmpty();
    }
}
