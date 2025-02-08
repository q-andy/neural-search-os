/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public enum DerivedStatName {
    // Cluster info
    CLUSTER_VERSION("cluster_version", StatType.DERIVED_INFO_COUNTER),

    // Search processor info

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
