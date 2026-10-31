package com.daa.experiments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DsuExperimentCli {
    private static final long DEFAULT_SEED = 42L;
    private static final Path DEFAULT_OUTPUT =
            Path.of("results", "tables", "dsu-variants.csv");

    private DsuExperimentCli() {
    }

    public static void main(String[] arguments) throws IOException {
        if (arguments.length > 2) {
            throw new IllegalArgumentException(
                    "Usage: DsuExperimentCli [seed] [output-csv-path]");
        }

        long seed = arguments.length >= 1 ? Long.parseLong(arguments[0]) : DEFAULT_SEED;
        Path output = arguments.length == 2 ? Path.of(arguments[1]) : DEFAULT_OUTPUT;
        List<DsuExperimentResult> results =
                DsuVariantExperiment.runStandardExperiments(seed);
        CsvExporter.exportDsuResults(results, output);
        System.out.println("Seed: " + seed);
        System.out.println("DSU experiment rows exported: " + results.size());
        System.out.println("CSV output: " + output);
    }
}
