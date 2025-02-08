/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import org.opensearch.neuralsearch.processor.chunker.DelimiterChunker;
import org.opensearch.neuralsearch.processor.chunker.FixedTokenLengthChunker;
import org.opensearch.neuralsearch.stats.names.EventStatName;
import org.opensearch.neuralsearch.stats.names.StatType;
import org.opensearch.neuralsearch.stats.suppliers.CounterSupplier;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class EventStatsManager {
    private Map<String, NeuralStat<Long>> eventStatsMap;

    public static EventStatsManager INSTANCE;

    public static EventStatsManager instance() {
        if (INSTANCE == null) {
            INSTANCE = new EventStatsManager();
        }
        return INSTANCE;
    }

    private static void increment(EventStatName eventStatName) {
        instance().getStats().computeIfAbsent(eventStatName.getName(), k -> new NeuralStat<>(new CounterSupplier())).increment();
    }

    public EventStatsManager() {
        this.eventStatsMap = new ConcurrentSkipListMap<>();

        // Initialize event counter stats
        for (EventStatName eventStatName : EnumSet.allOf(EventStatName.class)) {
            if (eventStatName.getStatType() == StatType.EVENT_COUNTER) {
                eventStatsMap.computeIfAbsent(eventStatName.getName(), k -> new NeuralStat<>(new CounterSupplier()));
            }
        }
    }

    /**
     * Get the stats
     *
     * @return all the stats
     */
    public Map<String, NeuralStat<Long>> getStats() {
        return eventStatsMap;
    }

    public void resetStats() {
        // Risk of memory leak?
        this.eventStatsMap = new ConcurrentSkipListMap<>();
        for (EventStatName eventStatName : EnumSet.allOf(EventStatName.class)) {
            if (eventStatName.getStatType() == StatType.EVENT_COUNTER) {
                eventStatsMap.computeIfAbsent(eventStatName.getName(), k -> new NeuralStat<>(new CounterSupplier()));
            }
        }
    }

    public static void recordTextChunkingExecution(String algorithm) {
        increment(EventStatName.TEXT_CHUNKING_PROCESSOR_EXECUTIONS);
        switch (algorithm) {
            case DelimiterChunker.ALGORITHM_NAME:
                increment(EventStatName.TEXT_CHUNKING_ALGORITHM_DELIMITER_EXECUTIONS);
                break;
            case FixedTokenLengthChunker.ALGORITHM_NAME:
                increment(EventStatName.TEXT_CHUNKING_ALGORITHM_FIXED_LENGTH_EXECUTIONS);
                break;
        }
    }

    public static void recordTextEmbeddingExecution() {
        increment(EventStatName.TEXT_EMBEDDING_PROCESSOR_EXECUTIONS);
    }
}
