package com.daa.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * An undirected weighted graph with a fixed number of vertices.
 *
 * <p>Parallel edges are allowed: every call to {@link #addEdge(int, int, int)}
 * inserts a new edge, even if an edge with the same endpoints and weight
 * already exists. Self-loops are allowed and appear once in their vertex's
 * adjacency list.</p>
 */
public final class WeightedGraph {
    private final int vertexCount;
    private final List<Edge> edges;
    private final List<List<Edge>> adjacencyList;

    public WeightedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative: " + vertexCount);
        }

        this.vertexCount = vertexCount;
        this.edges = new ArrayList<>();
        this.adjacencyList = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Inserts an undirected edge with a non-negative weight.
     *
     * @return the edge in the same endpoint order supplied by the caller
     */
    public Edge addEdge(int source, int destination, int weight) {
        validateVertex(source);
        validateVertex(destination);
        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative: " + weight);
        }

        Edge edge = new Edge(source, destination, weight);
        edges.add(edge);
        adjacencyList.get(source).add(edge);
        if (source != destination) {
            // Adjacency entries are oriented from the vertex whose list contains them.
            adjacencyList.get(destination).add(new Edge(destination, source, weight));
        }
        return edge;
    }

    /**
     * Returns each inserted edge once, in insertion order.
     */
    public List<Edge> getEdges() {
        return List.copyOf(edges);
    }

    /**
     * Returns an immutable adjacency-list snapshot. Each adjacency entry is
     * oriented from its list's vertex to the neighboring vertex.
     */
    public List<List<Edge>> getAdjacencyList() {
        List<List<Edge>> snapshot = new ArrayList<>(vertexCount);
        for (List<Edge> adjacentEdges : adjacencyList) {
            snapshot.add(List.copyOf(adjacentEdges));
        }
        return List.copyOf(snapshot);
    }

    /**
     * Returns the immutable list of edges adjacent to {@code vertex}.
     */
    public List<Edge> getAdjacentEdges(int vertex) {
        validateVertex(vertex);
        return List.copyOf(adjacencyList.get(vertex));
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + vertex + " is outside [0, " + vertexCount + ')');
        }
    }
}
