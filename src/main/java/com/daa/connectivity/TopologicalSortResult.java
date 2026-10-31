package com.daa.connectivity;

import java.util.List;

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

    public List<Integer> getOrdering() {
        return ordering;
    }
}
