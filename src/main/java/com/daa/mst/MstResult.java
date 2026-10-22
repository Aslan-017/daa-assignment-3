package com.daa.mst;

import com.daa.graph.Edge;

import java.util.List;

/**
 * The result of an MST algorithm. For a disconnected graph, {@code edges}
 * contains a minimum spanning forest and {@link #isSpanningTree()} is false.
 */
public final class MstResult {
    private final List<Edge> edges;
    private final long totalWeight;
    private final boolean spanningTree;

    public MstResult(List<Edge> edges, long totalWeight, boolean spanningTree) {
        this.edges = List.copyOf(edges);
        this.totalWeight = totalWeight;
        this.spanningTree = spanningTree;
    }

    /**
     * Returns the selected edges in the order they were added by the algorithm.
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Returns the sum of selected edge weights as a {@code long}.
     */
    public long getTotalWeight() {
        return totalWeight;
    }

    /**
     * Returns true when the result is one tree spanning every graph vertex.
     * This is also true for a graph containing a single vertex.
     */
    public boolean isSpanningTree() {
        return spanningTree;
    }
}
