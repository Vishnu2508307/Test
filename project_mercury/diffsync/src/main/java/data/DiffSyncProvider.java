package data;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DiffSyncProvider {

    // First key is entityTypeAndEntityId and second key is DIffSyncIdentifier urn
    private ConcurrentHashMap<String, ConcurrentHashMap<String, DiffSync>> diffSyncProviders;

    @Inject
    public DiffSyncProvider() {
        this.diffSyncProviders = new ConcurrentHashMap<>();
    }


    /**
     * This method add diff sync instances by entity(@EntityType and entity id) and diff sync identifier urn to the provider map
     *
     * @param entity the entity(entityType and entityId)
     * @param urn the urn is client and server identifier
     * @param diffSync the diff sync instance
     */
    public void add(final String entity, final String urn, DiffSync diffSync) {
        ConcurrentHashMap<String, DiffSync> newDiffSync = new ConcurrentHashMap<>();
        newDiffSync.put(urn, diffSync);

        if (diffSyncProviders.get(entity) != null) {

            ConcurrentHashMap<String, DiffSync> diffSyncs = diffSyncProviders.get(entity);
            diffSyncs.putAll(newDiffSync);
            diffSyncProviders.put(entity, diffSyncs);
        } else {
            diffSyncProviders.put(entity, newDiffSync);
        }
    }

    /**
     * get all diff sync instances by entity
     * @param entity the entity(entityType and entityId)
     * @return collection of diff sync
     */
    public Collection<DiffSync> getDiffSyncByEntity(final String entity) {
        return diffSyncProviders.get(entity).values();
    }

    /**
     * Checks whether diff sync instance is present or not by entity and urn
     * @param entity the entity(entityType and entityId)
     * @param urn the diff sync identifier urn
     * @return boolean
     */
    public boolean isDiffSyncPresent(final String entity, final String urn) {
        return diffSyncProviders.get(entity).containsKey(urn);
    }

    /**
     * This method remove diff sync instance by entity
     *
     * @param entity the entity info(entity type and entity id)
     */
    public void remove(String entity) {
        if (diffSyncProviders.get(entity) != null) {
            diffSyncProviders.remove(entity);
        }
    }

    /**
     * Return map of diff sync instance
     *
     * @return
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<String, DiffSync>> getDiffSyncs() {
        return diffSyncProviders;
    }

}
