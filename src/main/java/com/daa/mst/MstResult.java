package com.daa.mst;

import com.daa.graph.Edge;

import java.util.List;

public final class MstResult {
    private final List<Edge> edges;
    private final long totalWeight;
    private final boolean spanningTree;

    public MstResult(List<Edge> edges, long totalWeight, boolean spanningTree) {
        this.edges = List.copyOf(edges);
        this.totalWeight = totalWeight;
        this.spanningTree = spanningTree;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public long getTotalWeight() {
        return totalWeight;
    }

    public boolean isSpanningTree() {
        return spanningTree;
    }
}
