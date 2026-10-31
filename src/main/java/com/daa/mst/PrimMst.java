package com.daa.mst;

import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

// Equal-weight candidates use endpoint order; different tie choices can produce another valid MST.
public final class PrimMst {
    private static final Comparator<Edge> EDGE_ORDER = Comparator
            .comparingInt(Edge::getWeight)
            .thenComparingInt(Edge::getSource)
            .thenComparingInt(Edge::getDestination);

    private PrimMst() {
    }

    public static MstResult findMinimumSpanningTree(WeightedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        List<Edge> selectedEdges = new ArrayList<>(Math.max(0, vertexCount - 1));
        boolean[] visited = new boolean[vertexCount];
        PriorityQueue<Edge> candidates = new PriorityQueue<>(EDGE_ORDER);
        long totalWeight = 0;

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            candidates.addAll(graph.getAdjacentEdges(start));
            while (!candidates.isEmpty()) {
                Edge edge = candidates.remove();
                int nextVertex = edge.getDestination();
                if (visited[nextVertex]) {
                    continue;
                }

                visited[nextVertex] = true;
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();
                for (Edge adjacentEdge : graph.getAdjacentEdges(nextVertex)) {
                    if (!visited[adjacentEdge.getDestination()]) {
                        candidates.add(adjacentEdge);
                    }
                }
            }
        }

        boolean isSpanningTree = vertexCount > 0
                && selectedEdges.size() == vertexCount - 1;
        return new MstResult(selectedEdges, totalWeight, isSpanningTree);
    }

    // Counts every queue insertion/removal, stale polled candidates, and removed candidates examined.
    public static MstResult findMinimumSpanningTree(
            WeightedGraph graph, OperationCounts counters) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(counters, "Counters cannot be null");
        counters.enable(
                OperationCounts.Counter.priorityQueueInsertions,
                OperationCounts.Counter.priorityQueueRemovals,
                OperationCounts.Counter.staleEntriesSkipped,
                OperationCounts.Counter.edgesExamined);

        int vertexCount = graph.getVertexCount();
        List<Edge> selectedEdges = new ArrayList<>(Math.max(0, vertexCount - 1));
        boolean[] visited = new boolean[vertexCount];
        PriorityQueue<Edge> candidates = new PriorityQueue<>(EDGE_ORDER);
        long totalWeight = 0;

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            for (Edge edge : graph.getAdjacentEdges(start)) {
                candidates.add(edge);
                counters.increment(OperationCounts.Counter.priorityQueueInsertions);
            }
            while (!candidates.isEmpty()) {
                Edge edge = candidates.remove();
                counters.increment(OperationCounts.Counter.priorityQueueRemovals);
                counters.increment(OperationCounts.Counter.edgesExamined);
                int nextVertex = edge.getDestination();
                if (visited[nextVertex]) {
                    counters.increment(OperationCounts.Counter.staleEntriesSkipped);
                    continue;
                }

                visited[nextVertex] = true;
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();
                for (Edge adjacentEdge : graph.getAdjacentEdges(nextVertex)) {
                    if (!visited[adjacentEdge.getDestination()]) {
                        candidates.add(adjacentEdge);
                        counters.increment(OperationCounts.Counter.priorityQueueInsertions);
                    }
                }
            }
        }

        boolean isSpanningTree = vertexCount > 0
                && selectedEdges.size() == vertexCount - 1;
        return new MstResult(selectedEdges, totalWeight, isSpanningTree);
    }
}
