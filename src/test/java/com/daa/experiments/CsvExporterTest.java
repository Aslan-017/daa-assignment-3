package com.daa.experiments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesHeaderAndStructuredRowsWithBlankCellsForMissingCounters() throws IOException {
        List<ExperimentResult> results = List.of(
                new ExperimentResult(
                        "Prim", "connected", 10, 30, 5, 120, Map.of("edgesExamined", 20L)),
                new ExperimentResult(
                        "Dijkstra", "unreachable", 10, 30, 5, 90,
                        Map.of("staleEntriesSkipped", 2L)),
                new ExperimentResult(
                        "Topological sort", "partial_cycle", 10, 30, 5, 140,
                        Map.of("verticesProcessed", 3L)));
        Path output = temporaryDirectory.resolve("measurements.csv");

        CsvExporter.export(results, output);

        List<String> lines = Files.readAllLines(output);
        assertEquals(
                "algorithm,scenario,vertex_count,edge_count,repetition_count,average_elapsed_time_ns,"
                        + "edgesExamined,staleEntriesSkipped,verticesProcessed",
                lines.get(0));
        assertEquals("Prim,connected,10,30,5,120,20,,", lines.get(1));
        assertEquals("Dijkstra,unreachable,10,30,5,90,,2,", lines.get(2));
        assertEquals("Topological sort,partial_cycle,10,30,5,140,,,3", lines.get(3));
    }

    @Test
    void escapesCommasQuotesAndNewlinesInStringFields() throws IOException {
        ExperimentResult result = new ExperimentResult(
                "Prim, \"optimized\"\nvariant", "connected", 2, 1, 5, 10, Map.of());
        Path output = temporaryDirectory.resolve("escaped.csv");

        CsvExporter.export(List.of(result), output);

        String csv = Files.readString(output);
        assertTrue(csv.contains(
                "\"Prim, \"\"optimized\"\"\nvariant\",connected,2,1,5,10\r\n"));
    }

    @Test
    void createsOutputDirectoriesAutomatically() throws IOException {
        Path output = temporaryDirectory.resolve("results").resolve("tables").resolve("run.csv");
        assertFalse(Files.exists(output.getParent()));

        Path writtenPath = CsvExporter.export(List.of(), output);

        assertEquals(output, writtenPath);
        assertTrue(Files.isRegularFile(output));
    }

    @Test
    void writesHeaderForEmptyResults() throws IOException {
        Path output = temporaryDirectory.resolve("empty.csv");

        CsvExporter.export(List.of(), output);

        assertEquals(
                "algorithm,scenario,vertex_count,edge_count,repetition_count,"
                        + "average_elapsed_time_ns",
                Files.readAllLines(output).get(0));
    }

    @Test
    void writesDsuVariantResultsAndOperationMetrics() throws IOException {
        DsuExperimentResult result = new DsuExperimentResult(
                "basic",
                42,
                5,
                6,
                5,
                120,
                Map.of(
                        "connectivityQueries", 2L,
                        "dsuFindCalls", 12L,
                        "findTraversals", 5L,
                        "unionParentPointerUpdates", 4L,
                        "pathCompressionParentPointerUpdates", 0L,
                        "successfulUnions", 4L));
        Path output = temporaryDirectory.resolve("dsu-variants.csv");

        CsvExporter.exportDsuResults(List.of(result), output);

        List<String> lines = Files.readAllLines(output);
        assertEquals(
                "variant,seed,vertex_count,operation_count,repetition_count,"
                        + "average_elapsed_time_ns,connectivityQueries,dsuFindCalls,"
                        + "findTraversals,pathCompressionParentPointerUpdates,"
                        + "successfulUnions,unionParentPointerUpdates",
                lines.get(0));
        assertEquals("basic,42,5,6,5,120,2,12,5,0,4,4", lines.get(1));
    }
}
