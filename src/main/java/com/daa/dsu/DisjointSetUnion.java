package com.daa.dsu;

import com.daa.metrics.OperationCounts;

public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private int setCount;

    public DisjointSetUnion(int elementCount) {
        if (elementCount < 0) {
            throw new IllegalArgumentException("Element count cannot be negative: " + elementCount);
        }

        parent = new int[elementCount];
        size = new int[elementCount];
        setCount = elementCount;
        for (int element = 0; element < elementCount; element++) {
            parent[element] = element;
            size[element] = 1;
        }
    }

    public int find(int element) {
        validateElement(element);

        int root = element;
        while (root != parent[root]) {
            root = parent[root];
        }

        while (element != root) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
        return root;
    }

    // Each invocation counts once, including the two find calls made by union.
    public int find(int element, OperationCounts counters) {
        validateElement(element);
        counters.increment(OperationCounts.Counter.dsuFindCalls);
        return find(element);
    }

    public boolean union(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) {
            return false;
        }

        if (size[firstRoot] < size[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }
        parent[secondRoot] = firstRoot;
        size[firstRoot] += size[secondRoot];
        setCount--;
        return true;
    }

    public boolean union(int first, int second, OperationCounts counters) {
        int firstRoot = find(first, counters);
        int secondRoot = find(second, counters);
        if (firstRoot == secondRoot) {
            return false;
        }

        mergeRoots(firstRoot, secondRoot);
        return true;
    }

    private void mergeRoots(int firstRoot, int secondRoot) {
        // Attach the smaller tree to keep the tree shallow.
        if (size[firstRoot] < size[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }
        parent[secondRoot] = firstRoot;
        size[firstRoot] += size[secondRoot];
        setCount--;
    }

    public boolean isConnected(int first, int second) {
        return find(first) == find(second);
    }

    public int getSetCount() {
        return setCount;
    }

    private void validateElement(int element) {
        if (element < 0 || element >= parent.length) {
            throw new IndexOutOfBoundsException(
                    "Element index " + element + " is outside [0, " + parent.length + ')');
        }
    }
}
