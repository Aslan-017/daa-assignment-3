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

public final class GraphGenerator {
    private static final List<GraphSize> MST_SIZES = List.of(
            new GraphSize(10, 30),
            new GraphSize(100, 500),
            new GraphSize(1_000, 5_000));

    private final Random random;

    public GraphGenerator(long seed) {
        random = new Random(seed);
    }

    public static List<GraphSize> standardMstSizes() {
        return MST_SIZES;
    }

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

    // Edges only point forward in a shuffled vertex order, so this graph is acyclic.
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

    // Start with a cycle, then add distinct non-loop edges.
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

    public DirectedGraph generatePartialCycleDirectedGraph(int vertexCount, int edgeCount) {
        if (vertexCount < 3 || edgeCount < 0) {
            throw new IllegalArgumentException(
                    "A partial-cycle graph needs at least three vertices");
        }

        List<Integer> order = vertexOrder(vertexCount);
        int cycleSize = Math.max(2, vertexCount / 4);
        cycleSize = Math.min(cycleSize, vertexCount - 1);
        int acyclicSize = vertexCount - cycleSize;
        int minimumEdges = cycleSize + acyclicSize - 1;
        List<Long> cycleEdges = new ArrayList<>();
        Set<Long> selectedEdges = new HashSet<>();

        for (int index = 0; index < cycleSize; index++) {
            int source = order.get(index);
            int destination = order.get((index + 1) % cycleSize);
            long edge = directedEdgeKey(source, destination);
            cycleEdges.add(edge);
            selectedEdges.add(edge);
        }

        List<Long> candidateEdges = new ArrayList<>();
        for (int index = cycleSize; index < vertexCount - 1; index++) {
            long edge = directedEdgeKey(order.get(index), order.get(index + 1));
            selectedEdges.add(edge);
        }
        for (int sourcePosition = 0; sourcePosition < cycleSize; sourcePosition++) {
            for (int destinationPosition = 0; destinationPosition < cycleSize; destinationPosition++) {
                if (sourcePosition != destinationPosition) {
                    long edge = directedEdgeKey(
                            order.get(sourcePosition), order.get(destinationPosition));
                    if (!selectedEdges.contains(edge)) {
                        candidateEdges.add(edge);
                    }
                }
            }
        }
        for (int sourcePosition = cycleSize; sourcePosition < vertexCount; sourcePosition++) {
            for (int destinationPosition = sourcePosition + 1;
                 destinationPosition < vertexCount;
                 destinationPosition++) {
                long edge = directedEdgeKey(
                        order.get(sourcePosition), order.get(destinationPosition));
                if (!selectedEdges.contains(edge)) {
                    candidateEdges.add(edge);
                }
            }
            for (int destinationPosition = 0; destinationPosition < cycleSize; destinationPosition++) {
                candidateEdges.add(directedEdgeKey(
                        order.get(sourcePosition), order.get(destinationPosition)));
            }
        }

        if (edgeCount < minimumEdges || edgeCount > minimumEdges + candidateEdges.size()) {
            throw new IllegalArgumentException(
                    "Edge count cannot preserve both a cycle and a processable acyclic region");
        }

        Collections.shuffle(candidateEdges, random);
        DirectedGraph graph = new DirectedGraph(vertexCount);
        for (long edge : cycleEdges) {
            graph.addEdge((int) (edge >>> 32), (int) edge);
        }
        for (int index = cycleSize; index < vertexCount - 1; index++) {
            graph.addEdge(order.get(index), order.get(index + 1));
        }
        int extraEdgeCount = edgeCount - minimumEdges;
        for (int index = 0; index < extraEdgeCount; index++) {
            long edge = candidateEdges.get(index);
            graph.addEdge((int) (edge >>> 32), (int) edge);
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
