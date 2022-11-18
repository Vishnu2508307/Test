package com.smartsparrow.courseware.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class CoursewareElementStructureService {

    private final CoursewareService coursewareService;
    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final InteractiveService interactiveService;

    @Inject
    public CoursewareElementStructureService(final CoursewareService coursewareService,
                                             final ActivityService activityService,
                                             final PathwayService pathwayService,
                                             final InteractiveService interactiveService) {
        this.coursewareService = coursewareService;
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.interactiveService = interactiveService;
    }

    @Trace(async = true)
    public Mono<CoursewareElementNode> getCoursewareElementStructure(final UUID elementId, final CoursewareElementType elementType, final List<String> fieldNames) {
        return coursewareService.getRootElementId(elementId, elementType)
                .flatMap(rootElementId -> {
                    if (elementId == rootElementId) {
                        return generateStructureFromRootElement(rootElementId, fieldNames);
                    } else {
                        return generateStructureFromNonRootElement(rootElementId, elementId, elementType, fieldNames);
                    }
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> generateStructureFromRootElement(final UUID rootElementId, final List<String> fieldNames) {
        CoursewareElementNode elementNode = new CoursewareElementNode()
                .setElementId(rootElementId)
                .setType(CoursewareElementType.ACTIVITY)
                .setParentId(null)
                .setTopParentId(rootElementId);

        return coursewareService.fetchConfigurationFields(rootElementId, fieldNames)
                .collectList()
                .flatMap(configFields -> {
                    elementNode.setConfigFields(configFields);
                    return populateChildren(elementNode, fieldNames);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> generateStructureFromNonRootElement(final UUID rootElementId, final UUID elementId, final CoursewareElementType elementType, final List<String> fieldNames) {
        return coursewareService.getPath(elementId, elementType)
                .flatMap(path -> {
                    CoursewareElementNode elementNode = new CoursewareElementNode()
                            .setElementId(elementId)
                            .setType(elementType)
                            // The last item in the path is the current element, so the parent is the second last item.
                            .setParentId(path.get(path.size() - 2).getElementId())
                            .setTopParentId(rootElementId);
                    return coursewareService.fetchConfigurationFields(rootElementId, fieldNames)
                            .collectList()
                            .flatMap(configFields -> {
                                elementNode.setConfigFields(configFields);
                                return populateChildren(elementNode, fieldNames);
                            });
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildren(final CoursewareElementNode node, final List<String> fieldNames) {
        CoursewareElementType type = node.getType();
        switch (type) {
            case ACTIVITY:
                return coursewareService.fetchConfigurationFields(node.getElementId(), fieldNames)
                        .collectList()
                        .flatMap(configFields -> {
                            node.setConfigFields(configFields);
                            return populateChildrenForActivity(node, fieldNames);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case INTERACTIVE:
                return coursewareService.fetchConfigurationFields(node.getElementId(), fieldNames)
                        .collectList()
                        .flatMap(configFields -> {
                            node.setConfigFields(configFields);
                            return populateChildrenForInteractive(node, fieldNames);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case PATHWAY:
                return coursewareService.fetchConfigurationFields(node.getElementId(), fieldNames)
                        .collectList()
                        .flatMap(configFields -> {
                            node.setConfigFields(configFields);
                            return populateChildrenForPathway(node, fieldNames);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case COMPONENT:
                return coursewareService.fetchConfigurationFields(node.getElementId(), fieldNames)
                        .collectList()
                        .flatMap(configFields -> {
                            node.setConfigFields(configFields);
                            return Mono.just(node);
                        })
                        .doOnEach(ReactiveTransaction.linkOnNext());
            default:
                throw new UnsupportedOperationFault("Unsupported courseware element type " + type);
        }
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForActivity(final CoursewareElementNode node, final List<String> fieldNames) {
        return activityService.findChildPathwayIds(node.getElementId())
                .flatMapIterable(p -> p)
                .flatMap(pathwayId -> createChildNodeAndPopulateChildren(
                        new CoursewareElementNode()
                                .setElementId(pathwayId)
                                .setType(CoursewareElementType.PATHWAY)
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, fieldNames))
                .flatMap(newNode -> activityService.findChildComponentIds(node.getElementId())
                .flatMapIterable(c -> c)
                .flatMap(componentId -> createChildNodeAndPopulateChildren(
                        new CoursewareElementNode()
                                .setElementId(componentId)
                                .setType(CoursewareElementType.COMPONENT)
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), newNode, fieldNames)))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForInteractive(final CoursewareElementNode node, final List<String> fieldNames) {
        return interactiveService.findChildComponentIds(node.getElementId())
                .flatMapIterable(c -> c)
                .flatMap(componentId -> createChildNodeAndPopulateChildren(
                        new CoursewareElementNode()
                                .setElementId(componentId)
                                .setType(CoursewareElementType.COMPONENT)
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, fieldNames))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForPathway(final CoursewareElementNode node, final List<String> fieldNames) {
        return pathwayService.getOrderedWalkableChildren(node.getElementId())
                .flatMapIterable(c -> c)
                .concatMap(walkable -> createChildNodeAndPopulateChildren(
                        new CoursewareElementNode()
                                .setElementId(walkable.getElementId())
                                .setType(walkable.getElementType())
                                .setParentId(node.getElementId())
                                .setTopParentId(node.getTopParentId()), node, fieldNames))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> createChildNodeAndPopulateChildren(final CoursewareElementNode element, final CoursewareElementNode parentNode, final List<String> fieldNames) {
        return populateChildren(element, fieldNames)
                .flatMap(updatedNode -> Mono.just(parentNode.addChild(updatedNode)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
