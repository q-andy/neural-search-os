/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

import lombok.Getter;
import org.opensearch.neuralsearch.processor.ExplanationResponseProcessor;

import java.util.HashSet;
import java.util.Set;

public enum StatName {
    CLUSTER_VERSION("cluster_version", StatType.INFO_DERIVED),
    SEARCH_PIPELINE_NEURAL_QUERY_ENRICHER_PROCESSOR_COUNT(
        "pipelines.search.request_processors.neural_query_enricher.count",
        StatType.INFO_DERIVED
    ),
    SEARCH_PIPELINE_EXPLANATION_PROCESSOR_COUNT(
        "pipelines.search.response_processors.hybrid_score_explanation.count",
        StatType.INFO_DERIVED
    ),
    SEARCH_PIPELINE_RRF_PROCESSOR_COUNT("pipelines.search.phase_results_processors.score_ranker_processor.count", StatType.INFO_DERIVED),
    SEARCH_PIPELINE_NORMALIZATION_COMBINATION_TECHNIQUE_RRF_COUNT(
        "pipelines.search.normalization.combination.techniques.rrf.count",
        StatType.INFO_DERIVED
    ),
    TEXT_EMBEDDING_PROCESSOR_EXECUTIONS("ingest_processor.text_embedding.executions", StatType.COUNTER_EVENT);

    @Getter
    private final String name;
    @Getter
    private final StatType statType;

    StatName(String name, StatType statType) {
        this.name = name;
        this.statType = statType;
    }

    /**
     * Get all stat names
     *
     * @return set of all stat names
     */
    public static Set<String> getNames() {
        Set<String> names = new HashSet<>();
        String m = ExplanationResponseProcessor.TYPE;
        for (StatName statName : StatName.values()) {
            names.add(statName.getName());
        }
        return names;
    }

    @Override
    public String toString() {
        return getName();
    }
}
