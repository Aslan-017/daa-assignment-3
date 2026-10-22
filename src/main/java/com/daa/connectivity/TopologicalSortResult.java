package com.daa.connectivity;

import java.util.List;

/**
 * A topological ordering result. An empty ordering is valid when the input
 * graph is empty; when the graph contains a cycle, {@link #isAcyclic()} is
 * false and the ordering is empty.
 */
public final class TopologicalSortResult {
    private final List<Integer> ordering;
    private final boolean acyclic;

    TopologicalSortResult(List<Integer> ordering, boolean acyclic) {
        this.ordering = List.copyOf(ordering);
        this.acyclic = acyclic;
    }

    public boolean isAcyclic() {
        return acyclic;
    }

    /**
     * Returns the complete ordering for a DAG, or an empty list if a cycle
     * prevents a complete ordering.
     */
    public List<Integer> getOrdering() {
        return ordering;
    }
}
