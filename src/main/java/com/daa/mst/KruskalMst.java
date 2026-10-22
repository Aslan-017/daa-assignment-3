package com.daa.mst;

import com.daa.dsu.DisjointSetUnion;
import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Kruskal's minimum spanning tree algorithm.
 *
 * <p>Edges of equal weight are considered in their graph insertion order.
 * Different tie choices may produce different valid minimum spanning trees.</p>
 */
public final class KruskalMst {
    private KruskalMst() {
    }

    /**
     * Finds a minimum spanning tree, or a minimum spanning forest when the
     * graph is disconnected.
     */
    public static MstResult findMinimumSpanningTree(WeightedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        // List.sort is stable, so equal-weight edges retain insertion order.
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
}
