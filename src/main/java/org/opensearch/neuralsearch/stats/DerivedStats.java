/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import org.opensearch.neuralsearch.stats.suppliers.DerivedSupplier;
import org.opensearch.neuralsearch.util.NeuralSearchClusterUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public class DerivedStats {
    private static final String AGG_KEY_PREFIX = "all_nodes.";
    private static DerivedStats INSTANCE;

    public static DerivedStats instance() {
        if (INSTANCE == null) {
            INSTANCE = new DerivedStats();
        }
        return INSTANCE;
    }

    private final Map<String, NeuralStat<?>> derivedStatsMap;
    private Map<String, Long> aggregatedNodeResponse;

    public DerivedStats() {
        this.derivedStatsMap = new ConcurrentSkipListMap<>();
        register("derived.cluster_version", DerivedStats::clusterVersion);
    }

    public Map<String, Long> aggregateNodesResponses(List<Map<String, Long>> nodeResponses) {
        Map<String, Long> summedMap = new HashMap<>();
        for (Map<String, Long> map : nodeResponses) {
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                summedMap.merge(AGG_KEY_PREFIX + entry.getKey(), entry.getValue(), Long::sum);
            }
        }
        return summedMap;
    }

    public Map<String, Object> addDerivedStats(List<Map<String, Long>> nodeResponses) {
        // Reference to provide derived methods access to node Responses
        this.aggregatedNodeResponse = aggregateNodesResponses(nodeResponses);

        Map<String, Object> computedDerivedStats = new TreeMap<>();
        for (Map.Entry<String, NeuralStat<?>> neuralStatEntry : derivedStatsMap.entrySet()) {
            computedDerivedStats.put(neuralStatEntry.getKey(), neuralStatEntry.getValue().getValue());
        }

        computedDerivedStats.putAll(aggregatedNodeResponse);
        // Reset reference to not store
        this.aggregatedNodeResponse = null;
        return computedDerivedStats;
    }

    private void register(String statPath, Function<DerivedStats, ?> derivedMethod) {
        if (derivedStatsMap.containsKey(statPath)) {
            // Validation error here
            return;
        }
        NeuralStat<?> neuralStat = new NeuralStat<>(new DerivedSupplier<>(this, derivedMethod));
        derivedStatsMap.put(statPath, neuralStat);
    }

    private String clusterVersion() {
        return NeuralSearchClusterUtil.instance().getClusterMinVersion().toString();
    }
}
