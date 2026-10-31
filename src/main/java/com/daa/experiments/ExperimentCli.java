package com.daa.experiments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Run from the repository root with {@code mvn -q compile}, then
 * {@code java -cp target/classes com.daa.experiments.ExperimentCli [seed] [output-csv-path]}.
 */
public final class ExperimentCli {
    private static final long DEFAULT_SEED = 42L;
    private static final Path DEFAULT_OUTPUT =
            Path.of("results", "tables", "assignment-3-experiments.csv");

    private ExperimentCli() {
    }

    public static void main(String[] arguments) throws IOException {
        if (arguments.length > 2) {
            throw new IllegalArgumentException(
                    "Usage: ExperimentCli [seed] [output-csv-path]");
        }

        long seed = arguments.length >= 1 ? Long.parseLong(arguments[0]) : DEFAULT_SEED;
        Path output = arguments.length == 2 ? Path.of(arguments[1]) : DEFAULT_OUTPUT;
        List<ExperimentResult> results = ExperimentRunner.runStandardExperiments(seed);
        CsvExporter.export(results, output);
        System.out.println("Seed: " + seed);
        System.out.println("Experiment rows exported: " + results.size());
        System.out.println("CSV output: " + output);
    }
}
