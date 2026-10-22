package com.daa.experiments;

import com.daa.graph.DirectedGraph;
import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Seeded generators for the assignment's graph experiments.
 *
 * <p>Undirected graphs are simple: no self-loops or parallel edges. Edge
 * weights are in [0, {@code weightBound}). DAG edges are sampled from a random
 * vertex ordering and only point forward in that ordering. Cyclic directed
 * graphs begin with a directed cycle and then receive sampled additional
 * non-loop edges. Directed graphs do not contain parallel edges.</p>
 */
public final class GraphGenerator {
    private static final List<GraphSize> MST_SIZES = List.of(
            new GraphSize(10, 30),
            new GraphSize(100, 500),
            new GraphSize(1_000, 5_000));

    private final Random random;

    public GraphGenerator(long seed) {
        random = new Random(seed);
    }

    /**
     * Returns the required connected MST experiment sizes.
     */
    public static List<GraphSize> standardMstSizes() {
        return MST_SIZES;
    }

    /**
     * Generates a connected simple undirected graph with exactly the requested
     * number of edges. A random spanning tree is selected first; the remaining
     * edges are sampled without replacement from the other vertex pairs.
     * Candidate-pair enumeration uses O(V^2) temporary space.
     *
     * @param weightBound exclusive upper bound for generated weights
     */
    public WeightedGraph generateConnectedWeightedGraph(
            int vertexCount, int edgeCount, int weightBound) {
        validateWeightBound(weightBound);
        validateUndirectedSize(vertexCount, edgeCount);
        if (vertexCount == 0) {
            throw new IllegalArgumentException("A connected MST graph needs at least one vertex");
        }
        if (edgeCount < vertexCount - 1) {
            throw new IllegalArgumentException(
                    "A connected graph needs at least vertexCount - 1 edges");
        }

        WeightedGraph graph = new WeightedGraph(vertexCount);
        Set<Long> treeEdges = new HashSet<>();
        for (int vertex = 1; vertex < vertexCount; vertex++) {
            int parent = random.nextInt(vertex);
            long edgeKey = undirectedEdgeKey(parent, vertex);
            treeEdges.add(edgeKey);
            addWeightedEdge(graph, parent, vertex, weightBound);
        }

        List<Long> remainingPairs = new ArrayList<>(edgeCount - treeEdges.size());
        for (int first = 0; first < vertexCount; first++) {
            for (int second = first + 1; second < vertexCount; second++) {
                long edgeKey = undirectedEdgeKey(first, second);
                if (!treeEdges.contains(edgeKey)) {
                    remainingPairs.add(edgeKey);
                }
            }
        }
        Collections.shuffle(remainingPairs, random);

        int extraEdgeCount = edgeCount - treeEdges.size();
        for (int index = 0; index < extraEdgeCount; index++) {
            long edgeKey = remainingPairs.get(index);
            graph.addEdge((int) (edgeKey >>> 32), (int) edgeKey, random.nextInt(weightBound));
        }
        return graph;
    }

    /**
     * Generates a simple undirected weighted graph, which may be disconnected.
     */
    public WeightedGraph generateWeightedGraph(
            int vertexCount, int edgeCount, int weightBound) {
        validateWeightBound(weightBound);
        validateUndirectedSize(vertexCount, edgeCount);

        List<Long> pairs = new ArrayList<>();
        for (int first = 0; first < vertexCount; first++) {
            for (int second = first + 1; second < vertexCount; second++) {
                pairs.add(undirectedEdgeKey(first, second));
            }
        }
        Collections.shuffle(pairs, random);

        WeightedGraph graph = new WeightedGraph(vertexCount);
        for (int index = 0; index < edgeCount; index++) {
            long edgeKey = pairs.get(index);
            graph.addEdge((int) (edgeKey >>> 32), (int) edgeKey, random.nextInt(weightBound));
        }
        return graph;
    }

    /**
     * Generates a graph in which the last vertex is isolated, making it
     * unreachable from source vertex 0. Edges are chosen only among the other
     * vertices.
     */
    public WeightedGraph generateWeightedGraphWithUnreachableVertex(
            int vertexCount, int edgeCount, int weightBound) {
        if (vertexCount < 2) {
            throw new IllegalArgumentException(
                    "At least two vertices are required to include an unreachable vertex");
        }
        validateUndirectedSize(vertexCount - 1, edgeCount);
        return copyWithIsolatedLastVertex(
                vertexCount, generateWeightedGraph(vertexCount - 1, edgeCount, weightBound));
    }

    /**
     * Generates a simple directed acyclic graph. A random permutation defines
     * a valid topological order; exactly {@code edgeCount} distinct forward
     * edges are sampled from that order.
     */
    public DirectedGraph generateDag(int vertexCount, int edgeCount) {
        validateDagSize(vertexCount, edgeCount);

        List<Integer> order = vertexOrder(vertexCount);
        List<Long> forwardEdges = new ArrayList<>();
        for (int sourcePosition = 0; sourcePosition < vertexCount; sourcePosition++) {
            for (int destinationPosition = sourcePosition + 1;
                 destinationPosition < vertexCount;
                 destinationPosition++) {
                forwardEdges.add(directedEdgeKey(
                        order.get(sourcePosition), order.get(destinationPosition)));
            }
        }
        Collections.shuffle(forwardEdges, random);

        DirectedGraph graph = new DirectedGraph(vertexCount);
        for (int index = 0; index < edgeCount; index++) {
            long edgeKey = forwardEdges.get(index);
            graph.addEdge((int) (edgeKey >>> 32), (int) edgeKey);
        }
        return graph;
    }

    /**
     * Generates a simple directed graph containing a cycle. For one vertex
     * the cycle is a self-loop; otherwise it is a cycle through all vertices.
     */
    public DirectedGraph generateCyclicDirectedGraph(int vertexCount, int edgeCount) {
        if (vertexCount < 0 || edgeCount < 0) {
            throw new IllegalArgumentException("Vertex and edge counts cannot be negative");
        }
        long maximumEdges = vertexCount == 1
                ? 1
                : (long) vertexCount * (vertexCount - 1);
        if (edgeCount > maximumEdges) {
            throw new IllegalArgumentException(
                    "Simple directed graph supports at most " + maximumEdges + " edges");
        }
        int minimumCycleEdges = vertexCount == 1 ? 1 : vertexCount;
        if (vertexCount == 0 || edgeCount < minimumCycleEdges) {
            throw new IllegalArgumentException(
                    "A cyclic graph needs at least " + minimumCycleEdges + " edges");
        }

        DirectedGraph graph = new DirectedGraph(vertexCount);
        Set<Long> cycleEdges = new HashSet<>();
        if (vertexCount == 1) {
            cycleEdges.add(directedEdgeKey(0, 0));
            graph.addEdge(0, 0);
        } else {
            List<Integer> cycleOrder = vertexOrder(vertexCount);
            for (int index = 0; index < vertexCount; index++) {
                int source = cycleOrder.get(index);
                int destination = cycleOrder.get((index + 1) % vertexCount);
                cycleEdges.add(directedEdgeKey(source, destination));
                graph.addEdge(source, destination);
            }
        }

        List<Long> additionalEdges = new ArrayList<>();
        for (int source = 0; source < vertexCount; source++) {
            for (int destination = 0; destination < vertexCount; destination++) {
                long edgeKey = directedEdgeKey(source, destination);
                if (source != destination && !cycleEdges.contains(edgeKey)) {
                    additionalEdges.add(edgeKey);
                }
            }
        }
        Collections.shuffle(additionalEdges, random);

        int additionalEdgeCount = edgeCount - cycleEdges.size();
        for (int index = 0; index < additionalEdgeCount; index++) {
            long edgeKey = additionalEdges.get(index);
            graph.addEdge((int) (edgeKey >>> 32), (int) edgeKey);
        }
        return graph;
    }

    private WeightedGraph copyWithIsolatedLastVertex(
            int vertexCount, WeightedGraph partialGraph) {
        WeightedGraph graph = new WeightedGraph(vertexCount);
        for (Edge edge : partialGraph.getEdges()) {
            graph.addEdge(edge.getSource(), edge.getDestination(), edge.getWeight());
        }
        return graph;
    }

    private void addWeightedEdge(
            WeightedGraph graph, int first, int second, int weightBound) {
        graph.addEdge(first, second, random.nextInt(weightBound));
    }

    private List<Integer> vertexOrder(int vertexCount) {
        List<Integer> order = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            order.add(vertex);
        }
        Collections.shuffle(order, random);
        return order;
    }

    private static void validateUndirectedSize(int vertexCount, int edgeCount) {
        if (vertexCount < 0 || edgeCount < 0) {
            throw new IllegalArgumentException("Vertex and edge counts cannot be negative");
        }
        long maximumEdges = (long) vertexCount * (vertexCount - 1) / 2;
        if (edgeCount > maximumEdges) {
            throw new IllegalArgumentException(
                    "Simple undirected graph supports at most " + maximumEdges + " edges");
        }
    }

    private static void validateDagSize(int vertexCount, int edgeCount) {
        if (vertexCount < 0 || edgeCount < 0) {
            throw new IllegalArgumentException("Vertex and edge counts cannot be negative");
        }
        long maximumEdges = (long) vertexCount * (vertexCount - 1) / 2;
        if (edgeCount > maximumEdges) {
            throw new IllegalArgumentException(
                    "A DAG supports at most " + maximumEdges + " simple edges");
        }
    }

    private static void validateDirectedSize(int vertexCount, int edgeCount) {
        if (vertexCount < 0 || edgeCount < 0) {
            throw new IllegalArgumentException("Vertex and edge counts cannot be negative");
        }
        long maximumEdges = (long) vertexCount * (vertexCount - 1);
        if (edgeCount > maximumEdges) {
            throw new IllegalArgumentException(
                    "Simple directed graph supports at most " + maximumEdges + " non-loop edges");
        }
    }

    private static void validateWeightBound(int weightBound) {
        if (weightBound <= 0) {
            throw new IllegalArgumentException("Weight bound must be positive");
        }
    }

    private static long undirectedEdgeKey(int first, int second) {
        return ((long) first << 32) | (second & 0xffff_ffffL);
    }

    private static long directedEdgeKey(int source, int destination) {
        return ((long) source << 32) | (destination & 0xffff_ffffL);
    }

    public record GraphSize(int vertexCount, int edgeCount) {
    }
}
