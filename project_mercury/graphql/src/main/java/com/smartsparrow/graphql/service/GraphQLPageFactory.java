package com.smartsparrow.graphql.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;

import com.google.common.primitives.Ints;

import io.leangen.graphql.execution.relay.Page;
import io.leangen.graphql.execution.relay.generic.PageFactory;
import reactor.core.publisher.Mono;

public class GraphQLPageFactory {

    /**
     * Create page with a sub-list of the nodes list according parameters 'before' and 'last'.
     *
     * @param nodes the full list of nodes
     * @param before fetching only nodes before this node (exclusive)
     * @param last fetching only the last certain number of nodes
     * @param <N> type of nodes
     */
    public static <N> Page<N> createPage(List<N> nodes, String before, Integer last) {
        affirmLastArgumenttIsPositive(last);
        Integer beforeIndex = null;
        if (before != null) {
            beforeIndex = Ints.tryParse(before); //return null if before is not parseable
        }
        affirmValidBeforeArgument(before, beforeIndex);
        checkArgument(nodes != null, "list of nodes can not be null");

        int fromIndex = 0;
        int toIndex = nodes.size();

        if (beforeIndex != null && beforeIndex < nodes.size() + 1) {
            toIndex = beforeIndex - 1;
        }
        if (last != null && nodes.size() > last) {
            fromIndex = toIndex - last;
        }
        return PageFactory.createOffsetBasedPage(nodes.subList(fromIndex, toIndex), nodes.size(), fromIndex);
    }

    /**
     * Create Mono page {@link Mono<Page<N>>} with a sub-list of the nodes list according parameters 'before' and 'last'.
     *
     * @param nodes the {@link Mono} mono list of nodes
     * @param before fetching only nodes before this node (exclusive)
     * @param last fetching only the last certain number of nodes
     * @param <N> type of nodes
     */
    public static <N> Mono<Page<N>> createPage(Mono<List<N>> nodes, String before, Integer last) {
        affirmLastArgumenttIsPositive(last);
        affirmArgument(last == null || last >= 0, "'last' should be positive");
        Integer beforeIndex = null;
        if (before != null) {
            beforeIndex = Ints.tryParse(before); //return null if before is not parseable
        }
        affirmValidBeforeArgument(before, beforeIndex);
        checkArgument(nodes != null, "list of nodes can not be null");

        final Integer beforeIdx = beforeIndex;

        return nodes
                .map(items -> {
                    int fromIndex = 0;
                    int toIndex = items.size();
                    if (beforeIdx != null && beforeIdx < items.size() + 1) {
                        toIndex = beforeIdx - 1;
                    }
                    if (last != null && items.size() > last) {
                        fromIndex = toIndex - last;
                    }
                    return PageFactory.createOffsetBasedPage(items.subList(fromIndex, toIndex),
                                                             items.size(),
                                                             fromIndex);
                });
    }

    private static void affirmLastArgumenttIsPositive(Integer last) {
        affirmArgument(last == null || last >= 0, "'last' should be positive");
    }

    private static void affirmValidBeforeArgument(String before, Integer beforeIndex) {
        affirmArgument(before == null || beforeIndex != null && beforeIndex > 0, "invalid 'before' argument");
    }
}
