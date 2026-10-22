package com.daa.graph;

import java.util.Objects;

/**
 * An immutable weighted edge. The endpoints are not restricted to a particular
 * graph representation, and the weight may be negative when the edge is used
 * outside {@link WeightedGraph}.
 */
public final class Edge {
    private final int source;
    private final int destination;
    private final int weight;

    public Edge(int source, int destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Edge edge)) {
            return false;
        }
        return source == edge.source
                && destination == edge.destination
                && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, weight);
    }

    @Override
    public String toString() {
        return "Edge{source=" + source
                + ", destination=" + destination
                + ", weight=" + weight + '}';
    }
}
