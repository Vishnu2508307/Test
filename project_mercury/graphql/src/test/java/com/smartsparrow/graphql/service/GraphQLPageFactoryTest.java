package com.smartsparrow.graphql.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import com.smartsparrow.exception.IllegalArgumentFault;

import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

class GraphQLPageFactoryTest {

    private static final List<String> nodes = Lists.newArrayList("1", "2", "3", "4", "5");

    @Test
    void createPage() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, null, null);

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPage_invalidLast() {
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory.createPage(nodes, null, -1));
    }

    @Test
    void createPage_invalidBefore() {
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory.createPage(nodes, "-1", null));
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory.createPage(nodes, "before", null));
    }

    @Test
    void createPage_before1() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "1", null);

        assertNotNull(result);
        assertEquals(0, result.getEdges().size());
    }

    @Test
    void createPage_before3() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "3", null);

        assertNotNull(result);
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void createPage_before6() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "6", null);

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPage_before10() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "10", null);

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPage_last0() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, null, 0);

        assertNotNull(result);
        assertEquals(0, result.getEdges().size());
    }

    @Test
    void createPage_last2() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, null, 2);

        assertNotNull(result);
        assertEquals(2, result.getEdges().size());
        assertEquals("4", result.getEdges().get(0).getNode());
        assertEquals("5", result.getEdges().get(1).getNode());
    }

    @Test
    void createPage_before3last1() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "3", 1);

        assertNotNull(result);
        assertEquals(1, result.getEdges().size());
        assertEquals("2", result.getEdges().get(0).getNode());
    }

    @Test
    void createPage_before10last5() {
        Page<String> result = GraphQLPageFactory.createPage(nodes, "10", 5);

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPageReactive() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), null, null).block();

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPageReactive_invalidLast() {
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory
                .createPage(Mono.just(nodes), null, -1).block());
    }

    @Test
    void createPageReactive_invalidBefore() {
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory.createPage(Mono.just(nodes), "-1", null).block());
        assertThrows(IllegalArgumentFault.class, () -> GraphQLPageFactory.createPage(Mono.just(nodes), "before", null).block());
    }

    @Test
    void createPageReactive_before1() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "1", null).block();

        assertNotNull(result);
        assertEquals(0, result.getEdges().size());
    }

    @Test
    void createPageReactive_before3() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "3", null).block();

        assertNotNull(result);
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void createPageReactive_before6() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "6", null).block();

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPageReactive_before10() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "10", null).block();

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }

    @Test
    void createPageReactive_last0() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), null, 0).block();

        assertNotNull(result);
        assertEquals(0, result.getEdges().size());
    }

    @Test
    void createPageReactive_last2() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), null, 2).block();

        assertNotNull(result);
        assertEquals(2, result.getEdges().size());
        assertEquals("4", result.getEdges().get(0).getNode());
        assertEquals("5", result.getEdges().get(1).getNode());
    }

    @Test
    void createPageReactive_before3last1() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "3", 1).block();

        assertNotNull(result);
        assertEquals(1, result.getEdges().size());
        assertEquals("2", result.getEdges().get(0).getNode());
    }

    @Test
    void createPageReactive_before10last5() {
        Page<String> result = GraphQLPageFactory.createPage(Mono.just(nodes), "10", 5).block();

        assertNotNull(result);
        assertEquals(5, result.getEdges().size());
    }
}
