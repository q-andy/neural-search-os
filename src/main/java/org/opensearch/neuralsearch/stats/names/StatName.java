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
    // Cluster info
    CLUSTER_VERSION("cluster_version", StatType.INFO_DERIVED),

    // Search processor info
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
        "pipelines.search.normalization.combination.techniques.rrf",
        StatType.INFO_DERIVED
    ),

    // Ingest processor info
    INGEST_PIPELINE_TEXT_CHUNKING_PROCESSOR_COUNT("pipelines.ingest.processors.text_chunking.count", StatType.INFO_DERIVED),
    INGEST_PIPELINE_TEXT_CHUNKING_ALGORITHM_FIXED_LENGTH(
        "pipelines.ingest.processors.text_chunking.algorithm.fixed_length",
        StatType.INFO_DERIVED
    ),
    INGEST_PIPELINE_TEXT_CHUNKING_ALGORITHM_DELIMITER(
        "pipelines.ingest.processors.text_chunking.algorithm.delimiter",
        StatType.INFO_DERIVED
    ),
    INGEST_PIPELINE_TEXT_CHUNKING_TOKENIZER_STANDARD("pipelines.ingest.processors.text_chunking.tokenizer.standard", StatType.INFO_DERIVED),
    INGEST_PIPELINE_TEXT_CHUNKING_TOKENIZER_LETTER("pipelines.ingest.processors.text_chunking.tokenizer.letter", StatType.INFO_DERIVED),
    INGEST_PIPELINE_TEXT_CHUNKING_TOKENIZER_LOWERCASE(
        "pipelines.ingest.processors.text_chunking.tokenizer.lowercase",
        StatType.INFO_DERIVED
    ),
    INGEST_PIPELINE_TEXT_CHUNKING_TOKENIZER_WHITESPACE(
        "pipelines.ingest.processors.text_chunking.tokenizer.whitespace",
        StatType.INFO_DERIVED
    ),

    // Search processor events
    // Ingest processor events
    TEXT_EMBEDDING_PROCESSOR_EXECUTIONS("ingest_processor.text_embedding.executions", StatType.COUNTER_EVENT),
    TEXT_CHUNKING_PROCESSOR_EXECUTIONS("ingest_processor.text_chunking.executions", StatType.COUNTER_EVENT),
    TEXT_CHUNKING_ALGORITHM_FIXED_LENGTH_EXECUTIONS("ingest_processor.text_chunking.algorithm.fixed_length", StatType.COUNTER_EVENT),
    TEXT_CHUNKING_ALGORITHM_DELIMITER_EXECUTIONS("ingest_processor.text_chunking.algorithm.delimiter", StatType.COUNTER_EVENT),;

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
