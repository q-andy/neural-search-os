/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import org.opensearch.neuralsearch.processor.TextChunkingProcessor;
import org.opensearch.neuralsearch.stats.names.DerivedStatName;
import org.opensearch.neuralsearch.stats.names.StatType;
import org.opensearch.neuralsearch.util.NeuralSearchClusterUtil;
import org.opensearch.neuralsearch.util.PipelineInfoUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DerivedStatsManager {
    private static final String AGG_KEY_PREFIX = "all_nodes.";
    public static final String PROCESSORS_KEY = "processors";
    public static final String ALGORITHM_KEY = "algorithm";

    // Text chunking processor keys
    public static final String ALGORITHM_FIXED_TOKEN_LENGTH_KEY = "fixed_token_length";
    public static final String ALGORITHM_DELIMITER_KEY = "delimiter";
    public static final String TOKENIZER_KEY = "tokenizer";
    public static final String TOKENIZER_STANDARD = "standard";
    public static final String TOKENIZER_LETTER = "letter";
    public static final String TOKENIZER_LOWERCASE = "lowercase";
    public static final String TOKENIZER_WHITESPACE = "whitespace";

    // Search Response
    public static final String REQUEST_PROCESSORS_KEY = "request_processors";
    public static final String RESPONSE_PROCESSORS_KEY = "response_processors";
    public static final String PHASE_PROCESSORS_KEY = "phase_results_processors";
    public static final String COMBINATION_KEY = "combination";
    public static final String NORMALIZATION_KEY = "normalization";
    public static final String TECHNIQUE_KEY = "technique";

    private static DerivedStatsManager INSTANCE;

    public static DerivedStatsManager instance() {
        if (INSTANCE == null) {
            INSTANCE = new DerivedStatsManager();
        }
        return INSTANCE;
    }

    public Map<String, Object> getStats(List<Map<String, Long>> nodeResponses) {
        // Reference to provide derived methods access to node Responses
        Map<String, Long> aggregatedNodeResponses = aggregateNodesResponses(nodeResponses);

        Map<String, Object> computedDerivedStats = new TreeMap<>();

        // Initialize empty map with keys so stat names are visible in JSON even if not calculated
        for (DerivedStatName stat : EnumSet.allOf(DerivedStatName.class)) {
            if (stat.getStatType() == StatType.DERIVED_INFO_COUNTER) {
                computedDerivedStats.put(stat.getName(), 0L);
            }
        }

        getStats(computedDerivedStats);
        computedDerivedStats.putAll(aggregatedNodeResponses);
        return computedDerivedStats;
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

    private void getStats(Map<String, Object> stats) {
        addClusterVersionStat(stats);

        // Parses search pipeline processor configs for processor info
        addSearchProcessorStats(stats);

        // Parses ingest pipeline processor configs for processor info
        addIngestProcessorStats(stats);
    }

    private void addClusterVersionStat(Map<String, Object> stats) {
        // Example cluster level info stat
        String version = NeuralSearchClusterUtil.instance().getClusterMinVersion().toString();
        stats.put(DerivedStatName.CLUSTER_VERSION.getName(), version);
    }

    private void addIngestProcessorStats(Map<String, Object> stats) {
        List<Map<String, Object>> pipelineConfigs = PipelineInfoUtil.instance().getIngestPipelineConfigs();

        for (Map<String, Object> pipelineConfig : pipelineConfigs) {
            List<Map<String, Object>> ingestProcessors = asListOfMaps(pipelineConfig.get(PROCESSORS_KEY));
            for (Map<String, Object> ingestProcessor : ingestProcessors) {
                for (Map.Entry<String, Object> entry : ingestProcessor.entrySet()) {
                    String processorType = entry.getKey();
                    Map<String, Object> processorConfig = asMap(entry.getValue());
                    switch (processorType) {
                        case TextChunkingProcessor.TYPE:
                            addTextChunkingProcessorStats(stats, processorConfig);
                            break;
                        // Add additional ingest processor cases here
                    }
                }
            }
        }
    }

    private void addTextChunkingProcessorStats(Map<String, Object> stats, Map<String, Object> processorConfig) {
        increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_PROCESSOR_COUNT.getName());

        Map<String, Object> algorithmField = asMap(asMap(processorConfig).get(ALGORITHM_KEY));
        for (Map.Entry<String, Object> field : algorithmField.entrySet()) {
            switch (field.getKey()) {
                case ALGORITHM_DELIMITER_KEY:
                    increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_ALGORITHM_DELIMITER.getName());
                    break;
                case ALGORITHM_FIXED_TOKEN_LENGTH_KEY:
                    increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_ALGORITHM_FIXED_LENGTH.getName());
                    String tokenizer = getValue(asMap(field.getValue()), TOKENIZER_KEY, String.class);
                    switch (tokenizer) {
                        case TOKENIZER_STANDARD:
                            increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_TOKENIZER_STANDARD.getName());
                            break;
                        case TOKENIZER_LETTER:
                            increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_TOKENIZER_LETTER.getName());
                            break;
                        case TOKENIZER_LOWERCASE:
                            increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_TOKENIZER_LOWERCASE.getName());
                            break;
                        case TOKENIZER_WHITESPACE:
                            increment(stats, DerivedStatName.INGEST_TEXT_CHUNKING_TOKENIZER_WHITESPACE.getName());
                            break;
                    }
                    break;
            }
        }
    }

    private void addSearchProcessorStats(Map<String, Object> stats) {
        List<Map<String, Object>> pipelineConfigs = PipelineInfoUtil.instance().getSearchPipelineConfigs();

        System.out.println(pipelineConfigs);
        for (Map<String, Object> pipelineConfig : pipelineConfigs) {
            for (Map.Entry<String, Object> entry : pipelineConfig.entrySet()) {
                String searchProcessorType = entry.getKey();
                List<Map<String, Object>> processors = asListOfMaps(entry.getValue());

                switch (searchProcessorType) {
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
        // Count processor info using countProcessors method here
    }

    private void countSearchResponseProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        // Count processor info using countProcessors method here
    }

    private void countSearchPhaseResultsProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        // Count processor info using countProcessors method here
    }

    private void countProcessors(
        Map<String, Object> stats,
        List<Map<String, Object>> processors,
        String processorType,
        DerivedStatName derivedStatName
    ) {
        long count = processors.stream().filter(p -> p.containsKey(processorType)).count();
        incrementBy(stats, derivedStatName.getName(), count);
        // Add additional processor cases here
    }

    private void countCombinationTechniques(
        Map<String, Object> stats,
        List<Map<String, Object>> processors,
        String combinationTechnique,
        DerivedStatName derivedStatName
    ) {
        // Parses to access combination technique field
        for (Map<String, Object> processorObj : processors) {
            Map<String, Object> processor = asMap(processorObj);
            for (Object processorConfigObj : processor.values()) {
                Map<String, Object> config = asMap(processorConfigObj);
                Map<String, Object> combination = asMap(config.get(COMBINATION_KEY));
                String technique = getValue(combination, TECHNIQUE_KEY, String.class);
                if (technique != null && technique.equals(combinationTechnique)) {
                    increment(stats, derivedStatName.getName());
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
