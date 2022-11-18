package com.smartsparrow.courseware.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ManualGradingComponentByWalkable;
import com.smartsparrow.courseware.data.ParentByScenario;
import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.courseware.lang.CoursewareElementDuplicationFault;

/**
 * This class is used to keep context during activity duplication process
 */
public class DuplicationContext {

    /**
     * This is map to keep pairs old APIC id - new APIC id.
     */
    private Map<UUID, UUID> idsMap = new ConcurrentHashMap<>();

    /**
     * This is list to keep duplicated scenarios needed to save.
     * The scenarios for all APICs in courseware should be saved in the end,
     * because we need to replace all old ids with new ids in actions and conditions
     */
    private List<ParentByScenario> scenarios = new ArrayList<>();

    /**
     * This list keeps the original scope references for the element being duplicated. Only used with walkables
     */
    private List<ScopeReference> registeredScopeReferences = new ArrayList<>();

    private List<ManualGradingComponentByWalkable> manualGradingComponentByWalkables = new ArrayList<>();

    /**
     * This is the account id for the user that requested the duplication.
     */
    private UUID duplicatorAccount;

    /**
     * This is the subscription id for the user that requested the duplication.
     */
    private UUID duplicatorSubscriptionId;

    /**
     * This is to determine if old and new elements are in the same project or not
     */
    private boolean requireNewAssetId;

    private UUID oldRootElementId;
    private UUID newRootElementId;


    public Map<UUID, UUID> getIdsMap() {
        return idsMap;
    }

    public List<ParentByScenario> getScenarios() {
        return scenarios;
    }

    public UUID getDuplicatorAccount() {
        return duplicatorAccount;
    }

    public DuplicationContext setDuplicatorAccount(UUID accountId) {
        duplicatorAccount = accountId;
        return this;
    }

    public DuplicationContext putIds(UUID oldId, UUID newId) {
        idsMap.put(oldId, newId);
        return this;
    }

    public UUID getOldRootElementId() {
        return oldRootElementId;
    }

    public DuplicationContext setOldRootElementId(final UUID oldRootElementId) {
        this.oldRootElementId = oldRootElementId;
        return this;
    }

    public UUID getDuplicatorSubscriptionId() {
        return duplicatorSubscriptionId;
    }

    public DuplicationContext setDuplicatorSubscriptionId(UUID duplicatorSubscriptionId) {
        this.duplicatorSubscriptionId = duplicatorSubscriptionId;
        return this;
    }

    public Boolean getRequireNewAssetId() {
        return requireNewAssetId;
    }

    public DuplicationContext setRequireNewAssetId(Boolean requireNewAssetId) {
        this.requireNewAssetId = requireNewAssetId;
        return this;
    }

    public UUID getNewRootElementId() {
        return newRootElementId;
    }

    public DuplicationContext setNewRootElementId(final UUID newRootElementId) {
        this.newRootElementId = newRootElementId;
        return this;
    }

    public DuplicationContext addScenario(UUID scenarioId, UUID parentId, CoursewareElementType parentType) {
        scenarios.add(new ParentByScenario()
                .setScenarioId(scenarioId)
                .setParentId(parentId)
                .setParentType(parentType));
        return this;
    }

    public DuplicationContext addAllScopeReferences(List<ScopeReference> scopeReferences) {
        this.registeredScopeReferences.addAll(scopeReferences);
        return this;
    }

    public DuplicationContext addAllManualComponentByWalkable(List<ManualGradingComponentByWalkable> manualGradingComponentByWalkables) {
        this.manualGradingComponentByWalkables.addAll(manualGradingComponentByWalkables);
        return this;
    }

    public String replaceIds(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String newStr = str;
        for (Map.Entry<UUID, UUID> entry : idsMap.entrySet()) {
            newStr = newStr.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return newStr;
    }

    /**
     * Duplicate the manual grading component by walkable
     *
     * @return a list of duplicated manual grading component by walkable
     * @throws CoursewareElementDuplicationFault when any id is missing from the idsMap
     */
    public List<ManualGradingComponentByWalkable> duplicateManualGradingComponentByWalkable() {
        return manualGradingComponentByWalkables.stream()
                .map(manualGradingComponentByWalkable -> {

                    final UUID newWalkableId = idsMap.get(manualGradingComponentByWalkable.getWalkableId());
                    final UUID newComponentId = idsMap.get(manualGradingComponentByWalkable.getComponentId());
                    final UUID newComponentParentId = idsMap.get(manualGradingComponentByWalkable.getComponentParentId());

                    if (newWalkableId == null || newComponentId == null || newComponentParentId == null) {
                        throw new CoursewareElementDuplicationFault(String.format(
                                "some ids are missing: newWalkableId -> `%s` newComponentId -> `%s` newComponentParentId -> `%s`",
                                newWalkableId, newComponentId, newComponentParentId
                        ));
                    }

                    return new ManualGradingComponentByWalkable()
                            .setWalkableId(newWalkableId)
                            .setComponentId(newComponentId)
                            .setComponentParentId(newComponentParentId)
                            .setWalkableType(manualGradingComponentByWalkable.getWalkableType())
                            .setParentComponentType(manualGradingComponentByWalkable.getParentComponentType());
                }).collect(Collectors.toList());
    }

    /**
     * Duplicate the student scope registry based on the ids present in the idsMap
     *
     * @return a list of duplicated references
     * @throws CoursewareElementDuplicationFault when any id is missing from the idsMap
     */
    public List<ScopeReference> duplicateScopeReferences() {
        return registeredScopeReferences.stream()
                .map(registeredScopeReference -> {

                    final UUID newStudentScopeURN = idsMap.get(registeredScopeReference.getScopeURN());
                    final UUID newElementId = idsMap.get(registeredScopeReference.getElementId());

                    if (newStudentScopeURN == null || newElementId == null) {
                        throw new CoursewareElementDuplicationFault(String.format(
                                "some ids are missing: newStudentScopeUrn -> `%s` newElementId -> `%s`",
                                newStudentScopeURN, newElementId
                        ));
                    }

                    return new ScopeReference()
                            .setElementId(newElementId)
                            .setScopeURN(newStudentScopeURN)
                            .setElementType(registeredScopeReference.getElementType())
                            .setPluginId(registeredScopeReference.getPluginId())
                            .setPluginVersion(registeredScopeReference.getPluginVersion());
                }).collect(Collectors.toList());
    }
}

