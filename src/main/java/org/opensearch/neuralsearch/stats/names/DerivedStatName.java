/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

import lombok.Getter;
import org.opensearch.neuralsearch.processor.ExplanationResponseProcessor;

import java.util.HashSet;
import java.util.Set;

@Getter
public enum DerivedStatName {
    // Cluster info
    CLUSTER_VERSION("cluster_version", StatType.DERIVED_INFO_COUNTER),

    // Search processor info
    SEARCH_NEURAL_QUERY_ENRICHER_PROCESSOR_COUNT(
        "pipelines.search.request_processors.neural_query_enricher.count",
        StatType.DERIVED_INFO_COUNTER
    ),
    SEARCH_EXPLANATION_PROCESSOR_COUNT(
        "pipelines.search.response_processors.hybrid_score_explanation.count",
        StatType.DERIVED_INFO_COUNTER
    ),
    SEARCH_RRF_PROCESSOR_COUNT("pipelines.search.phase_results_processors.score_ranker_processor.count", StatType.DERIVED_INFO_COUNTER),
    SEARCH_NORMALIZATION_COMBINATION_TECHNIQUE_RRF_COUNT(
        "pipelines.search.normalization.combination.techniques.rrf",
        StatType.DERIVED_INFO_COUNTER
    ),

    // Ingest processor info
    INGEST_TEXT_CHUNKING_PROCESSOR_COUNT("pipelines.ingest.processors.text_chunking.count", StatType.DERIVED_INFO_COUNTER),
    INGEST_TEXT_CHUNKING_ALGORITHM_FIXED_LENGTH(
        "pipelines.ingest.processors.text_chunking.algorithm.fixed_length",
        StatType.DERIVED_INFO_COUNTER
    ),
    INGEST_TEXT_CHUNKING_ALGORITHM_DELIMITER(
        "pipelines.ingest.processors.text_chunking.algorithm.delimiter",
        StatType.DERIVED_INFO_COUNTER
    ),
    INGEST_TEXT_CHUNKING_TOKENIZER_STANDARD("pipelines.ingest.processors.text_chunking.tokenizer.standard", StatType.DERIVED_INFO_COUNTER),
    INGEST_TEXT_CHUNKING_TOKENIZER_LETTER("pipelines.ingest.processors.text_chunking.tokenizer.letter", StatType.DERIVED_INFO_COUNTER),
    INGEST_TEXT_CHUNKING_TOKENIZER_LOWERCASE(
        "pipelines.ingest.processors.text_chunking.tokenizer.lowercase",
        StatType.DERIVED_INFO_COUNTER
    ),
    INGEST_TEXT_CHUNKING_TOKENIZER_WHITESPACE(
        "pipelines.ingest.processors.text_chunking.tokenizer.whitespace",
        StatType.DERIVED_INFO_COUNTER
    );

    private final String name;
    private final StatType statType;

    DerivedStatName(String name, StatType statType) {
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
        for (DerivedStatName eventStatNames : DerivedStatName.values()) {
            names.add(eventStatNames.getName());
        }
        return names;
    }

    @Override
    public String toString() {
        return getName();
    }
}
