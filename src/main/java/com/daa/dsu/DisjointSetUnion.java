package com.daa.dsu;

/**
 * Disjoint-set data structure using path compression and union by size.
 */
public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private int setCount;

    /**
     * Creates {@code elementCount} singleton sets, indexed from 0 to
     * {@code elementCount - 1}.
     */
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

    /**
     * Returns the representative of the set containing {@code element}.
     * Nodes on the path to the representative are compressed to point directly
     * to it.
     */
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

    /**
     * Merges the sets containing the two elements.
     *
     * @return {@code true} if two previously separate sets were merged
     */
    public boolean union(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) {
            return false;
        }

        // Attaching the smaller tree keeps the height bounded before compression.
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
