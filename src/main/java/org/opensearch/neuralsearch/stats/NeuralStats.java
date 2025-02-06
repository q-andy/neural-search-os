/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import com.google.common.annotations.VisibleForTesting;
import org.opensearch.neuralsearch.stats.names.StatName;
import org.opensearch.neuralsearch.stats.names.StatType;
import org.opensearch.neuralsearch.stats.suppliers.CounterSupplier;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class NeuralStats {
    private Map<String, NeuralStat<Long>> counterStatsMap;

    public static NeuralStats INSTANCE;

    public static NeuralStats instance() {
        if (INSTANCE == null) {
            INSTANCE = new NeuralStats();
        }
        return INSTANCE;
    }

    public static NeuralStatBuilder recordMetric() {
        return new NeuralStatBuilder(instance());
    }

    public static NeuralStat<Long> record(StatName statName) {
        return instance().getStats().computeIfAbsent(statName.getName(), k -> new NeuralStat<>(new CounterSupplier()));
    }

    public NeuralStats() {
        this.counterStatsMap = new ConcurrentSkipListMap<>();

        // Initialize event counter stats
        for (StatName statName : EnumSet.allOf(StatName.class)) {
            if (statName.getStatType() == StatType.COUNTER_EVENT) {
                counterStatsMap.computeIfAbsent(statName.getName(), k -> new NeuralStat<>(new CounterSupplier()));
            }
        }
    }

    /**
     * Get the stats
     *
     * @return all the stats
     */
    public Map<String, NeuralStat<Long>> getStats() {
        return counterStatsMap;
    }

    @VisibleForTesting
    public void resetStats() {
        // Risk of memory leak?
        this.counterStatsMap = new ConcurrentSkipListMap<>();
        for (StatName statName : EnumSet.allOf(StatName.class)) {
            if (statName.getStatType() == StatType.COUNTER_EVENT) {
                counterStatsMap.computeIfAbsent(statName.getName(), k -> new NeuralStat<>(new CounterSupplier()));
            }
        }
    }
}
