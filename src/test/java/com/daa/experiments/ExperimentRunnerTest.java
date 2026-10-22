package com.daa.experiments;

import com.daa.graph.DirectedGraph;
import com.daa.graph.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentRunnerTest {
    @Test
    void reportsDeterministicMetricsSeparatelyFromFiveTimingRuns() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 2, 1);

        ExperimentResult first = ExperimentRunner.benchmarkDijkstra(graph, 0);
        ExperimentResult second = ExperimentRunner.benchmarkDijkstra(graph, 0);

        assertEquals("Dijkstra", first.algorithm());
        assertEquals(3, first.vertexCount());
        assertEquals(3, first.edgeCount());
        assertEquals(ExperimentRunner.REPETITIONS, first.repetitionCount());
        assertTrue(first.averageElapsedTimeNanos() >= 0);
        assertEquals(Map.of(
                "priorityQueueInsertions", 4L,
                "priorityQueueRemovals", 4L,
                "staleEntriesSkipped", 1L,
                "edgesExamined", 6L), first.operationCounts());
        assertEquals(first.operationCounts(), second.operationCounts());
    }

    @Test
    void capturesCountersForEachAlgorithmOutsideItsTimedRun() {
        WeightedGraph weightedGraph = new WeightedGraph(3);
        weightedGraph.addEdge(0, 1, 1);
        weightedGraph.addEdge(1, 2, 2);
        weightedGraph.addEdge(0, 2, 3);
        DirectedGraph directedGraph = new DirectedGraph(3);
        directedGraph.addEdge(0, 1);
        directedGraph.addEdge(1, 2);

        assertEquals(4L, ExperimentRunner.benchmarkKruskal(weightedGraph)
                .operationCounts().get("dsuFindCalls"));
        assertTrue(ExperimentRunner.benchmarkPrim(weightedGraph)
                .operationCounts().containsKey("priorityQueueInsertions"));
        assertEquals(4L, ExperimentRunner.benchmarkTopologicalSort(directedGraph)
                .operationCounts().get("edgesExamined"));
        assertEquals(4L, ExperimentRunner.benchmarkStronglyConnectedComponents(directedGraph)
                .operationCounts().get("edgesExamined"));
        assertEquals(3L, ExperimentRunner.benchmarkDijkstra(weightedGraph, 0)
                .operationCounts().get("priorityQueueRemovals"));
    }
}
