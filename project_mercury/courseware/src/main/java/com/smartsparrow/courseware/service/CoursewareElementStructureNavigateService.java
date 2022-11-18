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
public class CoursewareElementStructureNavigateService extends CoursewareElementStructureService{

    private final CoursewareService coursewareService;
    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final InteractiveService interactiveService;

    @Inject
    public CoursewareElementStructureNavigateService(final CoursewareService coursewareService,
                                                     final ActivityService activityService,
                                                     final PathwayService pathwayService,
                                                     final InteractiveService interactiveService) {
        super(coursewareService, activityService, pathwayService, interactiveService);
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
                    return populateChildrenForNavigate(elementNode, fieldNames);
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
                                return populateChildrenForNavigate(elementNode, fieldNames);
                            });
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForNavigate(final CoursewareElementNode node, final List<String> fieldNames) {
        CoursewareElementType type = node.getType();
        switch (type) {
            case ACTIVITY:
                    return coursewareService.fetchConfigurationFields(node.getElementId(), fieldNames)
                            .collectList()
                            .flatMap(configFields -> {
                                node.setConfigFields(configFields);
                                return populateChildrenForActivity(node, fieldNames);
                            }).flatMap(coursewareElementNode -> {
                                /*
                                 * This CoursewareElementNode is build on below structure with CoursewareElementType types
                                 * where A = Activity, I = Interactive, P = Pathway and C = Component
                                 * Immediate node after Activity is Pathway
                                 *          A
                                 *          |
                                 *          P
                                 *        /   \
                                 *       I     I
                                 *      / \   / \
                                 *     C   C C   C
                                 * Hence taking immediate child of Activity as pathway to identify its children
                                 */
                                CoursewareElementNode pathWayNode = coursewareElementNode.getChildren().get(0);
                                return coursewareService.fetchConfigurationFields(pathWayNode.getElementId(), fieldNames)
                                        .collectList()
                                        .flatMap(configFields -> {
                                            pathWayNode.setConfigFields(configFields);
                                            return populateChildrenForPathway(pathWayNode, fieldNames).flatMap(updatedPathWayNode -> Mono.just(node));
                                        })
                                        .doOnEach(ReactiveTransaction.linkOnNext());
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

    /**
     * Finds whether provided element has child elements or not
     * @param node {@link CoursewareElementNode} of any {@link CoursewareElementType} type
     * @return true if {@link CoursewareElementNode} has children else false
     */
    @Trace(async = true)
    private Mono<Boolean> hasChildElements(final CoursewareElementNode node) {

        switch (node.getType()) {
            case ACTIVITY:
                return  activityService.hasChildPathwayIds(node.getElementId());
            case INTERACTIVE:
                return interactiveService.hasChildComponentIds(node.getElementId());
            case PATHWAY:
                return pathwayService.hasOrderedWalkableChildren(node.getElementId());
            case COMPONENT:
                //Component does not have children
                return Mono.just(Boolean.FALSE);
            default:
                throw new UnsupportedOperationFault("Unsupported courseware element type " + node.getType());
        }
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForActivity(final CoursewareElementNode node, final List<String> fieldNames) {
        return activityService.findChildPathwayIds(node.getElementId())
                .flatMapIterable(p -> p)
                .flatMap(pathwayId -> {
                    CoursewareElementNode childNode = new CoursewareElementNode()
                            .setElementId(pathwayId)
                            .setType(CoursewareElementType.PATHWAY)
                            .setParentId(node.getElementId())
                            .setTopParentId(node.getTopParentId());
                        return getElementConfigurationAndUpdateElement(node, fieldNames, childNode);
                })
                .flatMap(newNode -> activityService.findChildComponentIds(node.getElementId())
                        .flatMapIterable(c -> c)
                        .flatMap(componentId -> {
                            CoursewareElementNode childNode =
                                    new CoursewareElementNode()
                                            .setElementId(componentId)
                                            .setType(CoursewareElementType.COMPONENT)
                                            .setParentId(node.getElementId())
                                            .setTopParentId(node.getTopParentId());
                                return Mono.just(node.addChild(newNode.addChild(childNode)));
                        }))
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForInteractive(final CoursewareElementNode node, final List<String> fieldNames) {
        return interactiveService.findChildComponentIds(node.getElementId())
                .flatMapIterable(c -> c)
                .flatMap(componentId -> {
                    CoursewareElementNode childNode = new CoursewareElementNode()
                            .setElementId(componentId)
                            .setType(CoursewareElementType.COMPONENT)
                            .setParentId(node.getElementId())
                            .setTopParentId(node.getTopParentId());
                        return getElementConfigurationAndUpdateElement(node, fieldNames, childNode);
                })
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds element configuration fields and identifies element has children or not
     * @param node {@link CoursewareElementNode} can of type {@link CoursewareElementType}
     * @param fieldNames fields requested from FE
     * @param childNode
     * @return CoursewareElementNode which will have config fields and children status
     */
    @Trace(async = true)
    private Mono<CoursewareElementNode> getElementConfigurationAndUpdateElement(final CoursewareElementNode node, final List<String> fieldNames, final CoursewareElementNode childNode) {
        return coursewareService.fetchConfigurationFields(childNode.getElementId(), fieldNames)
                .collectList()
                .flatMap(configurationFields -> {
                    childNode.setConfigFields(configurationFields);
                    return hasChildElements(childNode).flatMap(aBoolean -> {
                        childNode.setHasChildren(aBoolean);
                        return Mono.just(node.addChild(childNode));
                    });
                });
    }

    @Trace(async = true)
    private Mono<CoursewareElementNode> populateChildrenForPathway(final CoursewareElementNode node, final List<String> fieldNames) {
        return pathwayService.getOrderedWalkableChildren(node.getElementId())
                .flatMapIterable(c -> c)
                .concatMap(walkable -> {
                    CoursewareElementNode childNode = new CoursewareElementNode()
                            .setElementId(walkable.getElementId())
                            .setType(walkable.getElementType())
                            .setParentId(node.getElementId())
                            .setTopParentId(node.getTopParentId());
                        return getElementConfigurationAndUpdateElement(node, fieldNames, childNode);
                })
                .then(Mono.just(node))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
