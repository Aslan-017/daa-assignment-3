package com.daa.experiments;

import com.daa.connectivity.StronglyConnectedComponents;
import com.daa.connectivity.TopologicalSort;
import com.daa.graph.DirectedGraph;
import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;
import com.daa.mst.KruskalMst;
import com.daa.mst.PrimMst;
import com.daa.shortestpath.Dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ExperimentRunner {
    public static final int REPETITIONS = 5;
    private static final int WEIGHT_BOUND = 1_000;

    private ExperimentRunner() {
    }

    public static ExperimentResult benchmarkKruskal(WeightedGraph graph) {
        return benchmarkKruskal(graph, "connected");
    }

    public static ExperimentResult benchmarkKruskal(WeightedGraph graph, String scenario) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Kruskal", scenario, graph.getVertexCount(), graph.getEdgeCount(),
                () -> KruskalMst.findMinimumSpanningTree(graph),
                counters -> KruskalMst.findMinimumSpanningTree(graph, counters));
    }

    public static ExperimentResult benchmarkPrim(WeightedGraph graph) {
        return benchmarkPrim(graph, "connected");
    }

    public static ExperimentResult benchmarkPrim(WeightedGraph graph, String scenario) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Prim", scenario, graph.getVertexCount(), graph.getEdgeCount(),
                () -> PrimMst.findMinimumSpanningTree(graph),
                counters -> PrimMst.findMinimumSpanningTree(graph, counters));
    }

    public static ExperimentResult benchmarkTopologicalSort(DirectedGraph graph) {
        return benchmarkTopologicalSort(graph, "dag");
    }

    public static ExperimentResult benchmarkTopologicalSort(DirectedGraph graph, String scenario) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Topological sort", scenario, graph.getVertexCount(), graph.getEdgeCount(),
                () -> TopologicalSort.sort(graph),
                counters -> TopologicalSort.sort(graph, counters));
    }

    public static ExperimentResult benchmarkStronglyConnectedComponents(DirectedGraph graph) {
        return benchmarkStronglyConnectedComponents(graph, "directed");
    }

    public static ExperimentResult benchmarkStronglyConnectedComponents(
            DirectedGraph graph, String scenario) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Kosaraju SCC", scenario, graph.getVertexCount(), graph.getEdgeCount(),
                () -> StronglyConnectedComponents.find(graph),
                counters -> StronglyConnectedComponents.find(graph, counters));
    }

    public static ExperimentResult benchmarkDijkstra(WeightedGraph graph, int source) {
        return benchmarkDijkstra(graph, source, "connected");
    }

    public static ExperimentResult benchmarkDijkstra(
            WeightedGraph graph, int source, String scenario) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Dijkstra", scenario, graph.getVertexCount(), graph.getEdgeCount(),
                () -> Dijkstra.findShortestPaths(graph, source),
                counters -> Dijkstra.findShortestPaths(graph, source, counters));
    }

    public static List<ExperimentResult> runStandardExperiments(long seed) {
        GraphGenerator generator = new GraphGenerator(seed);
        List<WeightedGraph> mstGraphs = new ArrayList<>();
        List<DirectedGraph> dagGraphs = new ArrayList<>();
        List<DirectedGraph> cyclicGraphs = new ArrayList<>();
        List<DirectedGraph> partialCycleGraphs = new ArrayList<>();
        List<WeightedGraph> dijkstraGraphs = new ArrayList<>();
        List<WeightedGraph> disconnectedDijkstraGraphs = new ArrayList<>();

        for (GraphGenerator.GraphSize size : GraphGenerator.standardMstSizes()) {
            mstGraphs.add(generator.generateConnectedWeightedGraph(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
            dagGraphs.add(generator.generateDag(size.vertexCount(), size.edgeCount()));
            cyclicGraphs.add(generator.generateCyclicDirectedGraph(
                    size.vertexCount(), size.edgeCount()));
            partialCycleGraphs.add(generator.generatePartialCycleDirectedGraph(
                    size.vertexCount(), size.edgeCount()));
            dijkstraGraphs.add(generator.generateConnectedWeightedGraph(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
            disconnectedDijkstraGraphs.add(generator.generateWeightedGraphWithUnreachableVertex(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
        }

        List<ExperimentResult> results = new ArrayList<>();
        for (int index = 0; index < mstGraphs.size(); index++) {
            WeightedGraph graph = mstGraphs.get(index);
            results.add(benchmarkKruskal(graph, "connected"));
            results.add(benchmarkPrim(graph, "connected"));
        }
        for (int index = 0; index < dagGraphs.size(); index++) {
            results.add(benchmarkTopologicalSort(dagGraphs.get(index), "dag"));
            results.add(benchmarkTopologicalSort(cyclicGraphs.get(index), "cyclic"));
            results.add(benchmarkTopologicalSort(
                    partialCycleGraphs.get(index), "partial_cycle"));
            results.add(benchmarkStronglyConnectedComponents(cyclicGraphs.get(index), "directed"));
            results.add(benchmarkDijkstra(dijkstraGraphs.get(index), 0, "connected"));
            results.add(benchmarkDijkstra(
                    disconnectedDijkstraGraphs.get(index), 0, "unreachable"));
        }
        return List.copyOf(results);
    }

    private static ExperimentResult benchmark(
            String algorithm,
            String scenario,
            int vertexCount,
            int edgeCount,
            Supplier<?> invocation,
            Function<OperationCounts, ?> instrumentedInvocation) {
        long elapsedTotal = 0;
        for (int repetition = 0; repetition < REPETITIONS; repetition++) {
            long start = System.nanoTime();
            invocation.get();
            long elapsed = System.nanoTime() - start;
            elapsedTotal += elapsed;
        }
        OperationCounts counters = new OperationCounts();
        instrumentedInvocation.apply(counters);

        return new ExperimentResult(
                algorithm,
                scenario,
                vertexCount,
                edgeCount,
                REPETITIONS,
                elapsedTotal / REPETITIONS,
                counters.asMap());
    }
}
