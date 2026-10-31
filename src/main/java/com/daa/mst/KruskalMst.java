package com.daa.mst;

import com.daa.dsu.DisjointSetUnion;
import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Equal-weight edges retain insertion order; another tie order may select a different valid MST.
public final class KruskalMst {
    private KruskalMst() {
    }

    public static MstResult findMinimumSpanningTree(WeightedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        sortedEdges.sort(Comparator.comparingInt(Edge::getWeight));

        DisjointSetUnion sets = new DisjointSetUnion(vertexCount);
        List<Edge> selectedEdges = new ArrayList<>(Math.max(0, vertexCount - 1));
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            if (sets.union(edge.getSource(), edge.getDestination())) {
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();
                if (selectedEdges.size() == vertexCount - 1) {
                    break;
                }
            }
        }

        boolean isSpanningTree = vertexCount > 0
                && selectedEdges.size() == vertexCount - 1;
        return new MstResult(selectedEdges, totalWeight, isSpanningTree);
    }

    // Counts sorted edges reached before stopping, actual DSU find calls, successful unions,
    // and edges rejected because their endpoints are already connected.
    public static MstResult findMinimumSpanningTree(
            WeightedGraph graph, OperationCounts counters) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(counters, "Counters cannot be null");
        counters.enable(
                OperationCounts.Counter.edgesExamined,
                OperationCounts.Counter.dsuFindCalls,
                OperationCounts.Counter.successfulUnions,
                OperationCounts.Counter.rejectedCycleFormingEdges);

        int vertexCount = graph.getVertexCount();
        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        sortedEdges.sort(Comparator.comparingInt(Edge::getWeight));

        DisjointSetUnion sets = new DisjointSetUnion(vertexCount);
        List<Edge> selectedEdges = new ArrayList<>(Math.max(0, vertexCount - 1));
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            counters.increment(OperationCounts.Counter.edgesExamined);
            boolean merged = sets.union(edge.getSource(), edge.getDestination(), counters);
            if (merged) {
                counters.increment(OperationCounts.Counter.successfulUnions);
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();
                if (selectedEdges.size() == vertexCount - 1) {
                    break;
                }
            } else {
                counters.increment(OperationCounts.Counter.rejectedCycleFormingEdges);
            }
        }

        boolean isSpanningTree = vertexCount > 0
                && selectedEdges.size() == vertexCount - 1;
        return new MstResult(selectedEdges, totalWeight, isSpanningTree);
    }
}
