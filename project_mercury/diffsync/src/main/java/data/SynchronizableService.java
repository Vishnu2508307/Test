package data;

import reactor.core.publisher.Mono;

public interface SynchronizableService {

    Mono<String> getEntity(DiffSyncEntity entity);

    Mono<Void> persist(DiffSyncEntity entity, String content);
}
