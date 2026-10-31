package com.daa.experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class CsvExporter {
    private static final List<String> FIXED_COLUMNS = List.of(
            "algorithm",
            "scenario",
            "vertex_count",
            "edge_count",
            "repetition_count",
            "average_elapsed_time_ns");

    private CsvExporter() {
    }

    public static Path export(List<ExperimentResult> results, Path outputFile) throws IOException {
        Objects.requireNonNull(results, "Results cannot be null");
        Objects.requireNonNull(outputFile, "Output file cannot be null");

        Set<String> counterColumns = new TreeSet<>();
        for (ExperimentResult result : results) {
            counterColumns.addAll(Objects.requireNonNull(result, "Result cannot be null")
                    .operationCounts().keySet());
        }

        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                outputFile, StandardCharsets.UTF_8)) {
            writeRow(writer, combinedColumns(counterColumns));
            for (ExperimentResult result : results) {
                writeResult(writer, result, counterColumns);
            }
        }
        return outputFile;
    }

    private static List<String> combinedColumns(Set<String> counterColumns) {
        java.util.ArrayList<String> columns = new java.util.ArrayList<>(FIXED_COLUMNS);
        columns.addAll(counterColumns);
        return columns;
    }

    private static void writeResult(
            BufferedWriter writer, ExperimentResult result, Set<String> counterColumns)
            throws IOException {
        Objects.requireNonNull(result, "Result cannot be null");
        java.util.ArrayList<String> row = new java.util.ArrayList<>();
        row.add(result.algorithm());
        row.add(result.scenario());
        row.add(Integer.toString(result.vertexCount()));
        row.add(Integer.toString(result.edgeCount()));
        row.add(Integer.toString(result.repetitionCount()));
        row.add(Long.toString(result.averageElapsedTimeNanos()));
        for (String counter : counterColumns) {
            Long value = result.operationCounts().get(counter);
            row.add(value == null ? "" : value.toString());
        }
        writeRow(writer, row);
    }

    private static void writeRow(BufferedWriter writer, List<String> fields) throws IOException {
        for (int index = 0; index < fields.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write(escape(fields.get(index)));
        }
        writer.write("\r\n");
    }

    private static String escape(String field) {
        boolean needsQuotes = field.indexOf(',') >= 0
                || field.indexOf('"') >= 0
                || field.indexOf('\n') >= 0
                || field.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return field;
        }
        return '"' + field.replace("\"", "\"\"") + '"';
    }
}
