/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import com.google.common.collect.ImmutableSet;
import org.opensearch.neuralsearch.processor.ExplanationResponseProcessor;
import org.opensearch.neuralsearch.processor.NeuralQueryEnricherProcessor;
import org.opensearch.neuralsearch.processor.RRFProcessor;
import org.opensearch.neuralsearch.processor.combination.ArithmeticMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.GeometricMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.HarmonicMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.RRFScoreCombinationTechnique;
import org.opensearch.neuralsearch.stats.names.StatName;
import org.opensearch.neuralsearch.stats.names.StatType;
import org.opensearch.neuralsearch.util.NeuralSearchClusterUtil;
import org.opensearch.neuralsearch.util.StatsInfoUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DerivedStats {
    private static final String AGG_KEY_PREFIX = "all_nodes.";
    public static final String REQUEST_PROCESSORS_KEY = "request_processors";
    public static final String RESPONSE_PROCESSORS_KEY = "response_processors";
    public static final String PHASE_PROCESSORS_KEY = "phase_results_processors";
    public static final String COMBINATION_KEY = "combination";
    public static final String NORMALIZATION_KEY = "normalization";
    public static final String TECHNIQUE_KEY = "technique";

    public static final Set<String> COMBINATION_TECHNIQUES = ImmutableSet.of(
        ArithmeticMeanScoreCombinationTechnique.TECHNIQUE_NAME,
        HarmonicMeanScoreCombinationTechnique.TECHNIQUE_NAME,
        GeometricMeanScoreCombinationTechnique.TECHNIQUE_NAME,
        RRFScoreCombinationTechnique.TECHNIQUE_NAME
    );

    private static DerivedStats INSTANCE;

    public static DerivedStats instance() {
        if (INSTANCE == null) {
            INSTANCE = new DerivedStats();
        }
        return INSTANCE;
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
        Map<String, Long> aggregatedNodeResponses = aggregateNodesResponses(nodeResponses);

        Map<String, Object> computedDerivedStats = new TreeMap<>();

        // Initialize empty map with keys so stat names are visible in JSON even if not calculated
        for (StatName stat : EnumSet.allOf(StatName.class)) {
            if (stat.getStatType() == StatType.INFO_DERIVED) {
                computedDerivedStats.put(stat.getName(), 0L);
            }
        }

        calculateDerivedStats(computedDerivedStats);
        computedDerivedStats.putAll(aggregatedNodeResponses);
        return computedDerivedStats;
    }

    private void calculateDerivedStats(Map<String, Object> stats) {
        addClusterVersionStat(stats);
        addSearchProcessorStats(stats);
    }

    private void addClusterVersionStat(Map<String, Object> stats) {
        String version = NeuralSearchClusterUtil.instance().getClusterMinVersion().toString();
        stats.put(StatName.CLUSTER_VERSION.getName(), version);
    }

    private void addSearchProcessorStats(Map<String, Object> stats) {
        List<Map<String, Object>> pipelineConfigs = StatsInfoUtil.instance().listAllPipelineConfigs();

        System.out.println(pipelineConfigs);
        for (Map<String, Object> pipelineConfig : pipelineConfigs) {
            for (Map.Entry<String, Object> entry : pipelineConfig.entrySet()) {
                String processorType = entry.getKey();
                List<Map<String, Object>> processors = asListOfMaps(entry.getValue());

                switch (processorType) {
                    case REQUEST_PROCESSORS_KEY:
                        countSearchRequestProcessors(stats, processors);
                        break;
                    case RESPONSE_PROCESSORS_KEY:
                        countSearchResponseProcessors(stats, processors);
                        break;
                    case PHASE_PROCESSORS_KEY:
                        countSearchPhaseResultsProcessors(stats, processors);
                        break;
                }
            }
        }
    }

    private void countSearchRequestProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        countProcessors(
            stats,
            pipelineConfig,
            NeuralQueryEnricherProcessor.TYPE,
            StatName.SEARCH_PIPELINE_NEURAL_QUERY_ENRICHER_PROCESSOR_COUNT
        );
    }

    private void countSearchResponseProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        countProcessors(stats, pipelineConfig, ExplanationResponseProcessor.TYPE, StatName.SEARCH_PIPELINE_EXPLANATION_PROCESSOR_COUNT);
    }

    private void countSearchPhaseResultsProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        countProcessors(stats, pipelineConfig, RRFProcessor.TYPE, StatName.SEARCH_PIPELINE_RRF_PROCESSOR_COUNT);

        countCombinationTechniques(
            stats,
            pipelineConfig,
            RRFScoreCombinationTechnique.TECHNIQUE_NAME,
            StatName.SEARCH_PIPELINE_NORMALIZATION_COMBINATION_TECHNIQUE_RRF_COUNT
        );
    }

    private void countProcessors(Map<String, Object> stats, List<Map<String, Object>> processors, String processorType, StatName statName) {
        long count = processors.stream().filter(p -> p.containsKey(processorType)).count();
        incrementBy(stats, statName.getName(), count);
    }

    private void countCombinationTechniques(
        Map<String, Object> stats,
        List<Map<String, Object>> processors,
        String combinationTechnique,
        StatName statName
    ) {
        for (Map<String, Object> processorObj : processors) {
            Map<String, Object> processor = asMap(processorObj);
            for (Object processorConfigObj : processor.values()) {
                Map<String, Object> config = asMap(processorConfigObj);
                Map<String, Object> combination = asMap(config.get(COMBINATION_KEY));
                String technique = getValue(combination, TECHNIQUE_KEY, String.class);
                if (technique != null && technique.equals(combinationTechnique)) {
                    increment(stats, statName.getName());
                }
            }
        }
    }

    private void increment(Map<String, Object> stats, String path) {
        incrementBy(stats, path, 1L);
    }

    private void incrementBy(Map<String, Object> stats, String path, Long amount) {
        Object stat = stats.get(path);
        if (stat instanceof Long) {
            stats.put(path, (Long) stat + amount);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(Map<String, Object> map, String key, Class<T> clazz) {
        if (map == null || key == null) return null;
        Object value = map.get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asListOfMaps(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
                if (!(item instanceof Map)) return null;
            }
            return (List<Map<String, Object>>) value;
        }
        return null;
    }
}
