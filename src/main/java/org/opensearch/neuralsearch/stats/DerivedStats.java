/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import com.google.common.collect.ImmutableSet;
import org.opensearch.neuralsearch.processor.NeuralQueryEnricherProcessor;
import org.opensearch.neuralsearch.processor.RRFProcessor;
import org.opensearch.neuralsearch.processor.combination.ArithmeticMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.GeometricMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.HarmonicMeanScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.combination.RRFScoreCombinationTechnique;
import org.opensearch.neuralsearch.stats.names.StatName;
import org.opensearch.neuralsearch.util.NeuralSearchClusterUtil;
import org.opensearch.neuralsearch.util.StatsInfoUtil;

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

    // private final Map<String, NeuralStat<?>> derivedStatsMap;

    private Map<String, Long> aggregatedNodeResponse;

    public DerivedStats() {
        // this.derivedStatsMap = new ConcurrentSkipListMap<>();

        // register(StatName.CLUSTER_VERSION, DerivedStats::clusterVersion);
        // register(StatName.SEARCH_PIPELINE_CONFIGS, DerivedStats::testPipelineInfo);
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

        calculateDerivedStats(computedDerivedStats);
        computedDerivedStats.putAll(aggregatedNodeResponse);
        // Reset reference to not store
        this.aggregatedNodeResponse = null;
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

    private String addSearchProcessorStats(Map<String, Object> stats) {
        List<Map<String, Object>> configs = StatsInfoUtil.instance().listAllPipelineConfigs();
        System.out.println(configs);

        for (Map<String, Object> config : configs) {
            for (Map.Entry<String, Object> pipelineConfig : config.entrySet()) {
                String searchProcessorType = pipelineConfig.getKey();
                switch (searchProcessorType) {
                    case REQUEST_PROCESSORS_KEY:
                        countSearchRequestProcessors(stats, (List<Map<String, Object>>) pipelineConfig.getValue());
                        break;
                    case RESPONSE_PROCESSORS_KEY:
                        countSearchResponseProcessors(stats, (List<Map<String, Object>>) pipelineConfig.getValue());
                        break;
                    case PHASE_PROCESSORS_KEY:
                        countSearchPhaseResultsProcessors(stats, (List<Map<String, Object>>) pipelineConfig.getValue());
                        break;
                }
            }
        }
        return configs.toString();
    }

    private void countSearchRequestProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        for (Map<String, Object> processor : pipelineConfig) {
            if (processor.get(NeuralQueryEnricherProcessor.TYPE) != null) {
                String path = String.format("pipelines.search.%s.%s.count", REQUEST_PROCESSORS_KEY, NeuralQueryEnricherProcessor.TYPE);
                increment(stats, path);
            }
        }
    }

    private void countSearchResponseProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {

    }

    private void countSearchPhaseResultsProcessors(Map<String, Object> stats, List<Map<String, Object>> pipelineConfig) {
        for (Map<String, Object> processor : pipelineConfig) {
            if (processor.get(RRFProcessor.TYPE) != null) {
                String processorPath = String.format("pipelines.search.%s.%s.count", PHASE_PROCESSORS_KEY, RRFProcessor.TYPE);
                increment(stats, processorPath);
                String combinationTechnique = ((Map<String, Map<String, String>>) processor.get(RRFProcessor.TYPE)).get(COMBINATION_KEY)
                    .get(TECHNIQUE_KEY);
                if (COMBINATION_TECHNIQUES.contains(combinationTechnique)) {
                    String techniquePath = String.format("pipelines.search.normalization.techniques.%s.count", combinationTechnique);
                    increment(stats, techniquePath);
                }
            }
        }
    }

    private void increment(Map<String, Object> stats, String path) {
        stats.putIfAbsent(path, 0L);
        Object stat = stats.get(path);
        if (stat instanceof Long) {
            stats.put(path, (Long) stat + 1L);
        }
    }

    // private String addIngestProcessorStats() {
    //// List<String> configs = StatsInfoUtil.instance().listAllPipelineConfigs();
    //// System.out.println(configs);
    //// return configs.toString();
    // }
}
