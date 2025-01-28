/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class NeuralStats {
    private final Map<String, NeuralStat<?>> neuralStats;

    public NeuralStats() {
        this.neuralStats = new ConcurrentSkipListMap<>();
        neuralStats.put("ingest_processor.text_chunking.algorithm.delimiter.execution_count", new NeuralStat<>(() -> "10"));
        neuralStats.put("Bratwurst", new NeuralStat<>(() -> "Sushi"));
        neuralStats.put("ingest_processor.text_embedding.execution_count", new NeuralStat<>(() -> "3123"));
        neuralStats.put("ingest_processor.text_chunking.execution_count", new NeuralStat<>(() -> "777"));
        neuralStats.put("ingest_processor.text_chunking.algorithm.fixed_length.execution_count", new NeuralStat<>(() -> "32"));
    }

    /**
     * Get the stats
     *
     * @return all the stats
     */
    public Map<String, NeuralStat<?>> getStats() {
        return neuralStats;
    }
}
