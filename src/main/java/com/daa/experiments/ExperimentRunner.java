package com.daa.experiments;

import com.daa.connectivity.StronglyConnectedComponents;
import com.daa.connectivity.TopologicalSort;
import com.daa.dsu.DisjointSetUnion;
import com.daa.graph.DirectedGraph;
import com.daa.graph.WeightedGraph;
import com.daa.mst.KruskalMst;
import com.daa.mst.PrimMst;
import com.daa.shortestpath.Dijkstra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Runs reproducible algorithm timings over pre-generated graphs.
 *
 * <p>Each benchmark runs exactly five repetitions. Graph creation happens
 * before timing; only the algorithm invocation is inside the
 * {@link System#nanoTime()} interval. Current algorithm APIs do not expose
 * operation counters, so result counter maps are empty rather than estimated.</p>
 */
public final class ExperimentRunner {
    public static final int REPETITIONS = 5;
    private static final int WEIGHT_BOUND = 1_000;
    private static volatile Object lastBenchmarkResult;

    private ExperimentRunner() {
    }

    public static ExperimentResult benchmarkKruskal(WeightedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Kruskal", graph.getVertexCount(), graph.getEdgeCount(),
                () -> KruskalMst.findMinimumSpanningTree(graph));
    }

    public static ExperimentResult benchmarkPrim(WeightedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Prim", graph.getVertexCount(), graph.getEdgeCount(),
                () -> PrimMst.findMinimumSpanningTree(graph));
    }

    public static ExperimentResult benchmarkTopologicalSort(DirectedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Topological sort", graph.getVertexCount(), graph.getEdgeCount(),
                () -> TopologicalSort.sort(graph));
    }

    public static ExperimentResult benchmarkStronglyConnectedComponents(DirectedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Kosaraju SCC", graph.getVertexCount(), graph.getEdgeCount(),
                () -> StronglyConnectedComponents.find(graph));
    }

    public static ExperimentResult benchmarkDijkstra(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        return benchmark("Dijkstra", graph.getVertexCount(), graph.getEdgeCount(),
                () -> Dijkstra.findShortestPaths(graph, source));
    }

    /**
     * Generates the required MST sizes and representative DAG, cyclic, and
     * Dijkstra inputs, then benchmarks each algorithm five times per graph.
     * All graphs are generated before their corresponding timed invocations.
     */
    public static List<ExperimentResult> runStandardExperiments(long seed) {
        GraphGenerator generator = new GraphGenerator(seed);
        List<WeightedGraph> mstGraphs = new ArrayList<>();
        List<DirectedGraph> dagGraphs = new ArrayList<>();
        List<DirectedGraph> cyclicGraphs = new ArrayList<>();
        List<WeightedGraph> dijkstraGraphs = new ArrayList<>();
        List<WeightedGraph> disconnectedDijkstraGraphs = new ArrayList<>();

        for (GraphGenerator.GraphSize size : GraphGenerator.standardMstSizes()) {
            mstGraphs.add(generator.generateConnectedWeightedGraph(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
            dagGraphs.add(generator.generateDag(size.vertexCount(), size.edgeCount()));
            cyclicGraphs.add(generator.generateCyclicDirectedGraph(
                    size.vertexCount(), size.edgeCount()));
            dijkstraGraphs.add(generator.generateConnectedWeightedGraph(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
            disconnectedDijkstraGraphs.add(generator.generateWeightedGraphWithUnreachableVertex(
                    size.vertexCount(), size.edgeCount(), WEIGHT_BOUND));
        }

        List<ExperimentResult> results = new ArrayList<>();
        for (int index = 0; index < mstGraphs.size(); index++) {
            WeightedGraph graph = mstGraphs.get(index);
            results.add(benchmarkKruskal(graph));
            results.add(benchmarkPrim(graph));
        }
        for (int index = 0; index < dagGraphs.size(); index++) {
            results.add(benchmarkTopologicalSort(dagGraphs.get(index)));
            results.add(benchmarkTopologicalSort(cyclicGraphs.get(index)));
            results.add(benchmarkStronglyConnectedComponents(cyclicGraphs.get(index)));
            results.add(benchmarkDijkstra(dijkstraGraphs.get(index), 0));
            results.add(benchmarkDijkstra(disconnectedDijkstraGraphs.get(index), 0));
        }
        return List.copyOf(results);
    }

    private static ExperimentResult benchmark(
            String algorithm, int vertexCount, int edgeCount, Supplier<?> invocation) {
        long elapsedTotal = 0;
        for (int repetition = 0; repetition < REPETITIONS; repetition++) {
            long start = System.nanoTime();
            Object result = invocation.get();
            long elapsed = System.nanoTime() - start;
            lastBenchmarkResult = result;
            elapsedTotal += elapsed;
        }

        return new ExperimentResult(
                algorithm,
                vertexCount,
                edgeCount,
                REPETITIONS,
                elapsedTotal / REPETITIONS,
                Map.of());
    }
}
