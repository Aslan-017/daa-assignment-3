package com.daa.dsu;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisjointSetUnionTest {
    @Test
    void initializesEachElementInItsOwnSet() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);

        assertEquals(4, dsu.getSetCount());
        for (int element = 0; element < 4; element++) {
            assertEquals(element, dsu.find(element));
            assertFalse(dsu.isConnected(element, (element + 1) % 4));
        }
    }

    @Test
    void unionsSeparateSetsAndReportsConnectivity() {
        DisjointSetUnion dsu = new DisjointSetUnion(5);

        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));

        assertTrue(dsu.isConnected(0, 2));
        assertFalse(dsu.isConnected(0, 3));
        assertEquals(3, dsu.getSetCount());
    }

    @Test
    void repeatedUnionDoesNotChangeComponentCount() {
        DisjointSetUnion dsu = new DisjointSetUnion(3);

        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));

        assertEquals(2, dsu.getSetCount());
    }

    @Test
    void findCompressesNodesAlongThePathToTheRoot() throws ReflectiveOperationException {
        DisjointSetUnion dsu = new DisjointSetUnion(4);
        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(0, 2);

        Field parentField = DisjointSetUnion.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        int[] parents = (int[]) parentField.get(dsu);
        int root = dsu.find(3);

        assertEquals(root, parents[3]);
    }

    @Test
    void rejectsNegativeElementCountAndInvalidIndices() {
        assertThrows(IllegalArgumentException.class, () -> new DisjointSetUnion(-1));

        DisjointSetUnion dsu = new DisjointSetUnion(2);
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(2));
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.union(0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.isConnected(-1, 0));
    }

    @Test
    void supportsAnEmptyCollection() {
        DisjointSetUnion dsu = new DisjointSetUnion(0);

        assertEquals(0, dsu.getSetCount());
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(0));
    }
}
