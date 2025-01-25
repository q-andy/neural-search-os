/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import java.util.HashMap;
import java.util.Map;

public class NeuralStats {
    private final Map<String, NeuralStat<?>> neuralStats;

    public NeuralStats() {
        this.neuralStats = new HashMap<>();
        neuralStats.put(StatNames.NEURAL_QUERY_COUNT.getName(), new NeuralStat<>(() -> "nqc"));
        neuralStats.put(StatNames.HYBRID_QUERY_COUNT.getName(), new NeuralStat<>(() -> "hqc"));
    }

    /**
     * Get the stats
     *
     * @return all the stats
     */
    public Map<String, NeuralStat<?>> getStats() {
        return neuralStats;
    }

    /**
     * Get a map of the stats that are kept at the node level
     *
     * @return Map of stats kept at the node level
     */
    public Map<String, NeuralStat<?>> getNodeStats() {
        return getClusterOrNodeStats(false);
    }

    /**
     * Get a map of the stats that are kept at the cluster level
     *
     * @return Map of stats kept at the cluster level
     */
    public Map<String, NeuralStat<?>> getClusterStats() {
        return getClusterOrNodeStats(true);
    }

    private Map<String, NeuralStat<?>> getClusterOrNodeStats(Boolean getClusterStats) {
        return neuralStats;
        // Map<String, NeuralStat<?>> statsMap = new HashMap<>();
        //
        // for (Map.Entry<String, NeuralStat<?>> entry : NeuralStat.entrySet()) {
        // if (entry.getValue().isClusterLevel() == getClusterStats) {
        // statsMap.put(entry.getKey(), entry.getValue());
        // }
        // }
        // return statsMap;
    }
}
