package com.daa.experiments;

import com.daa.graph.DirectedGraph;
import com.daa.graph.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
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
        assertEquals("connected", first.scenario());
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
        assertEquals("connected", ExperimentRunner.benchmarkKruskal(weightedGraph).scenario());
        assertTrue(ExperimentRunner.benchmarkPrim(weightedGraph)
                .operationCounts().containsKey("priorityQueueInsertions"));
        assertEquals("connected", ExperimentRunner.benchmarkPrim(weightedGraph).scenario());
        assertEquals("dag", ExperimentRunner.benchmarkTopologicalSort(directedGraph).scenario());
        assertEquals(4L, ExperimentRunner.benchmarkTopologicalSort(directedGraph)
                .operationCounts().get("edgesExamined"));
        assertEquals("directed",
                ExperimentRunner.benchmarkStronglyConnectedComponents(directedGraph).scenario());
        assertEquals(6L, ExperimentRunner.benchmarkStronglyConnectedComponents(directedGraph)
                .operationCounts().get("edgesExamined"));
        assertEquals(3L, ExperimentRunner.benchmarkDijkstra(weightedGraph, 0)
                .operationCounts().get("priorityQueueRemovals"));

        DirectedGraph cyclicGraph = new DirectedGraph(2);
        cyclicGraph.addEdge(0, 1);
        cyclicGraph.addEdge(1, 0);
        assertEquals("cyclic",
                ExperimentRunner.benchmarkTopologicalSort(cyclicGraph, "cyclic").scenario());
        DirectedGraph partialCycleGraph = new GraphGenerator(31)
                .generatePartialCycleDirectedGraph(12, 30);
        ExperimentResult partialCycleResult =
                ExperimentRunner.benchmarkTopologicalSort(partialCycleGraph, "partial_cycle");
        assertEquals("partial_cycle", partialCycleResult.scenario());
        assertTrue(partialCycleResult.operationCounts().get("verticesProcessed") > 0);
        assertTrue(partialCycleResult.operationCounts().get("verticesProcessed") < 12);

        WeightedGraph graphWithUnreachableVertex = new WeightedGraph(3);
        graphWithUnreachableVertex.addEdge(0, 1, 1);
        assertEquals("unreachable",
                ExperimentRunner.benchmarkDijkstra(
                        graphWithUnreachableVertex, 0, "unreachable").scenario());
    }

    @Test
    void runsBothAdditionalSccScenariosAtEveryStandardSize() {
        List<ExperimentResult> results = ExperimentRunner.runAdditionalSccExperiments(42);

        assertEquals(6, results.size());
        for (int index = 0; index < results.size(); index++) {
            ExperimentResult result = results.get(index);
            GraphGenerator.GraphSize size =
                    GraphGenerator.standardMstSizes().get(index / 2);
            assertEquals("Kosaraju SCC", result.algorithm());
            assertEquals(index % 2 == 0 ? "multiple_components" : "isolated_vertices",
                    result.scenario());
            assertEquals(size.vertexCount(), result.vertexCount());
            assertEquals(size.edgeCount(), result.edgeCount());
            assertEquals(ExperimentRunner.REPETITIONS, result.repetitionCount());
            assertEquals(3L * size.edgeCount(),
                    result.operationCounts().get("edgesExamined"));
            assertEquals(2L * size.vertexCount(),
                    result.operationCounts().get("verticesVisited"));
        }
    }
}
