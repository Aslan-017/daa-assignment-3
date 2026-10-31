package com.daa.experiments;

import com.daa.metrics.OperationCounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class DsuVariantExperiment {
    public static final int REPETITIONS = 5;

    public enum Variant {
        BASIC("basic", false, false),
        PATH_COMPRESSION_ONLY("path_compression_only", true, false),
        UNION_BY_SIZE_ONLY("union_by_size_only", false, true),
        PATH_COMPRESSION_AND_UNION_BY_SIZE(
                "path_compression_and_union_by_size", true, true);

        private final String label;
        private final boolean pathCompression;
        private final boolean unionBySize;

        Variant(String label, boolean pathCompression, boolean unionBySize) {
            this.label = label;
            this.pathCompression = pathCompression;
            this.unionBySize = unionBySize;
        }

        public String label() {
            return label;
        }
    }

    public enum OperationType {
        UNION,
        CONNECTIVITY_QUERY
    }

    public record Operation(OperationType type, int first, int second) {
        public Operation {
            Objects.requireNonNull(type, "Operation type cannot be null");
            if (first < 0 || second < 0) {
                throw new IllegalArgumentException("Operation indices cannot be negative");
            }
        }
    }

    private DsuVariantExperiment() {
    }

    public static List<Operation> generateOperations(
            int vertexCount, int operationCount, long seed) {
        if (vertexCount <= 0 || operationCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex count must be positive and operation count cannot be negative");
        }
        Random random = new Random(seed);
        List<Operation> operations = new ArrayList<>(operationCount);
        for (int index = 0; index < operationCount; index++) {
            OperationType type = random.nextBoolean()
                    ? OperationType.UNION
                    : OperationType.CONNECTIVITY_QUERY;
            operations.add(new Operation(
                    type, random.nextInt(vertexCount), random.nextInt(vertexCount)));
        }
        return List.copyOf(operations);
    }

    public static List<DsuExperimentResult> runStandardExperiments(long seed) {
        List<DsuExperimentResult> results = new ArrayList<>();
        for (GraphGenerator.GraphSize size : GraphGenerator.standardMstSizes()) {
            int operationCount = Math.max(1_000, size.edgeCount() * 10);
            List<Operation> operations = generateOperations(
                    size.vertexCount(), operationCount, seed);
            results.addAll(runExperiment(size.vertexCount(), operations, seed));
        }
        return List.copyOf(results);
    }

    public static List<Boolean> verifyConnectivityResults(
            int vertexCount, List<Operation> operations) {
        Objects.requireNonNull(operations, "Operations cannot be null");
        validateOperations(vertexCount, operations);
        List<Boolean> expected = executeAndRecord(
                new VariantDsu(vertexCount, Variant.BASIC), operations);
        for (Variant variant : Variant.values()) {
            List<Boolean> actual = executeAndRecord(
                    new VariantDsu(vertexCount, variant), operations);
            if (!expected.equals(actual)) {
                throw new IllegalStateException(
                        "DSU variant produced different connectivity query results: "
                                + variant.label());
            }
        }
        return expected;
    }

    public static List<DsuExperimentResult> runExperiment(
            int vertexCount, List<Operation> operations, long seed) {
        Objects.requireNonNull(operations, "Operations cannot be null");
        validateOperations(vertexCount, operations);
        List<Boolean> expectedQueries = verifyConnectivityResults(vertexCount, operations);
        long expectedOutcome = outcomeFor(
                expectedQueries,
                countSuccessfulUnions(
                        new VariantDsu(vertexCount, Variant.BASIC), operations));

        List<DsuExperimentResult> results = new ArrayList<>(Variant.values().length);
        for (Variant variant : Variant.values()) {
            OperationCounts counters = new OperationCounts();
            counters.enable(
                    OperationCounts.Counter.dsuFindCalls,
                    OperationCounts.Counter.findTraversals,
                    OperationCounts.Counter.unionParentPointerUpdates,
                    OperationCounts.Counter.pathCompressionParentPointerUpdates,
                    OperationCounts.Counter.successfulUnions,
                    OperationCounts.Counter.connectivityQueries);
            long instrumentedOutcome = executeInstrumented(
                    new VariantDsu(vertexCount, variant), operations, counters);
            if (instrumentedOutcome != expectedOutcome) {
                throw new IllegalStateException(
                        "Instrumented DSU run produced unexpected results: " + variant.label());
            }

            long elapsedTotal = 0;
            for (int repetition = 0; repetition < REPETITIONS; repetition++) {
                VariantDsu dsu = new VariantDsu(vertexCount, variant);
                long start = System.nanoTime();
                long outcome = execute(dsu, operations);
                long elapsed = System.nanoTime() - start;
                elapsedTotal += elapsed;
                if (outcome != expectedOutcome) {
                    throw new IllegalStateException(
                            "Timed DSU run produced unexpected results: " + variant.label());
                }
            }
            results.add(new DsuExperimentResult(
                    variant.label(),
                    seed,
                    vertexCount,
                    operations.size(),
                    REPETITIONS,
                    elapsedTotal / REPETITIONS,
                    counters.asMap()));
        }
        return List.copyOf(results);
    }

    private static void validateOperations(int vertexCount, List<Operation> operations) {
        if (vertexCount <= 0) {
            throw new IllegalArgumentException("Vertex count must be positive");
        }
        for (Operation operation : operations) {
            Objects.requireNonNull(operation, "Operation cannot be null");
            if (operation.first() >= vertexCount || operation.second() >= vertexCount) {
                throw new IndexOutOfBoundsException("Operation index is outside the DSU");
            }
        }
    }

    private static List<Boolean> executeAndRecord(
            VariantDsu dsu, List<Operation> operations) {
        List<Boolean> queryResults = new ArrayList<>();
        for (Operation operation : operations) {
            if (operation.type() == OperationType.UNION) {
                dsu.union(operation.first(), operation.second());
            } else {
                queryResults.add(dsu.isConnected(operation.first(), operation.second()));
            }
        }
        return List.copyOf(queryResults);
    }

    private static int countSuccessfulUnions(VariantDsu dsu, List<Operation> operations) {
        int successfulUnions = 0;
        for (Operation operation : operations) {
            if (operation.type() == OperationType.UNION
                    && dsu.union(operation.first(), operation.second())) {
                successfulUnions++;
            } else if (operation.type() == OperationType.CONNECTIVITY_QUERY) {
                dsu.isConnected(operation.first(), operation.second());
            }
        }
        return successfulUnions;
    }

    private static long outcomeFor(List<Boolean> queryResults, int successfulUnions) {
        long trueQueries = queryResults.stream().filter(Boolean::booleanValue).count();
        return successfulUnions + trueQueries;
    }

    private static long execute(VariantDsu dsu, List<Operation> operations) {
        long outcome = 0;
        for (Operation operation : operations) {
            if (operation.type() == OperationType.UNION) {
                if (dsu.union(operation.first(), operation.second())) {
                    outcome++;
                }
            } else if (dsu.isConnected(operation.first(), operation.second())) {
                outcome++;
            }
        }
        return outcome;
    }

    private static long executeInstrumented(
            VariantDsu dsu, List<Operation> operations, OperationCounts counters) {
        long outcome = 0;
        for (Operation operation : operations) {
            if (operation.type() == OperationType.UNION) {
                if (dsu.unionInstrumented(operation.first(), operation.second(), counters)) {
                    counters.increment(OperationCounts.Counter.successfulUnions);
                    outcome++;
                }
            } else {
                counters.increment(OperationCounts.Counter.connectivityQueries);
                if (dsu.isConnectedInstrumented(
                        operation.first(), operation.second(), counters)) {
                    outcome++;
                }
            }
        }
        return outcome;
    }

    private static final class VariantDsu {
        private final int[] parent;
        private final int[] size;
        private final Variant variant;

        private VariantDsu(int vertexCount, Variant variant) {
            if (vertexCount <= 0) {
                throw new IllegalArgumentException("Vertex count must be positive");
            }
            this.variant = Objects.requireNonNull(variant, "Variant cannot be null");
            parent = new int[vertexCount];
            size = new int[vertexCount];
            for (int element = 0; element < vertexCount; element++) {
                parent[element] = element;
                size[element] = 1;
            }
        }

        private int find(int element) {
            int root = element;
            while (root != parent[root]) {
                root = parent[root];
            }
            if (variant.pathCompression) {
                while (element != root) {
                    int next = parent[element];
                    parent[element] = root;
                    element = next;
                }
            }
            return root;
        }

        private int findInstrumented(int element, OperationCounts counters) {
            counters.increment(OperationCounts.Counter.dsuFindCalls);
            int root = element;
            while (root != parent[root]) {
                counters.increment(OperationCounts.Counter.findTraversals);
                root = parent[root];
            }
            if (variant.pathCompression) {
                while (element != root) {
                    int next = parent[element];
                    if (parent[element] != root) {
                        parent[element] = root;
                        counters.increment(
                                OperationCounts.Counter.pathCompressionParentPointerUpdates);
                    }
                    element = next;
                }
            }
            return root;
        }

        private boolean union(int first, int second) {
            int firstRoot = find(first);
            int secondRoot = find(second);
            if (firstRoot == secondRoot) {
                return false;
            }
            attach(firstRoot, secondRoot);
            return true;
        }

        private boolean unionInstrumented(
                int first, int second, OperationCounts counters) {
            int firstRoot = findInstrumented(first, counters);
            int secondRoot = findInstrumented(second, counters);
            if (firstRoot == secondRoot) {
                return false;
            }
            attachInstrumented(firstRoot, secondRoot, counters);
            return true;
        }

        private boolean isConnected(int first, int second) {
            return find(first) == find(second);
        }

        private boolean isConnectedInstrumented(
                int first, int second, OperationCounts counters) {
            return findInstrumented(first, counters) == findInstrumented(second, counters);
        }

        private void attach(int firstRoot, int secondRoot) {
            int parentRoot = firstRoot;
            int childRoot = secondRoot;
            if (variant.unionBySize
                    && (size[firstRoot] < size[secondRoot]
                    || (size[firstRoot] == size[secondRoot] && firstRoot > secondRoot))) {
                parentRoot = secondRoot;
                childRoot = firstRoot;
            }
            parent[childRoot] = parentRoot;
            if (variant.unionBySize) {
                size[parentRoot] += size[childRoot];
            }
        }

        private void attachInstrumented(
                int firstRoot, int secondRoot, OperationCounts counters) {
            int parentRoot = firstRoot;
            int childRoot = secondRoot;
            if (variant.unionBySize
                    && (size[firstRoot] < size[secondRoot]
                    || (size[firstRoot] == size[secondRoot] && firstRoot > secondRoot))) {
                parentRoot = secondRoot;
                childRoot = firstRoot;
            }
            if (parent[childRoot] != parentRoot) {
                parent[childRoot] = parentRoot;
                counters.increment(OperationCounts.Counter.unionParentPointerUpdates);
            }
            if (variant.unionBySize) {
                size[parentRoot] += size[childRoot];
            }
        }
    }
}
