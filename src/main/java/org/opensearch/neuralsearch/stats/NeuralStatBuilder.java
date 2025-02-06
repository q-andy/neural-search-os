/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import org.opensearch.neuralsearch.stats.suppliers.CounterSupplier;

import java.util.ArrayList;
import java.util.List;

public class NeuralStatBuilder {
    public static final String DELIMITER = ".";
    private NeuralStats neuralStats;
    private List<String> path;

    NeuralStatBuilder(NeuralStats neuralStats) {
        this.neuralStats = neuralStats;
        this.path = new ArrayList<>();
    }

    public String getPathString() {
        return String.join(DELIMITER, path);
    }

    public NeuralStatBuilder ingestProcessor(String processor) {
        // Add a validation check here
        // Refactor to constnats
        path.add("ingest_processor");
        path.add(processor);
        return this;
    }

    public NeuralStatBuilder searchProcessor(String processor) {
        // Add a validation check here
        // Refactor to constnats
        path.add("search_processor");
        path.add(processor);
        return this;
    }

    public NeuralStatBuilder metric(String metric) {
        // should do some validation here
        path.add(metric);
        return this;
    }

    public void increment() {
        // Should do some extra validation here
        // SOme kind of "validate path" method that makes sure the path is valid?
        neuralStats.getStats().computeIfAbsent(getPathString(), k -> new NeuralStat<>(new CounterSupplier())).increment();
    }
}
