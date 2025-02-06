/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public enum StatName {
    // TODO : stat type not currently used
    EVENT_STAT("example.counter", StatType.COUNTER_EVENT),
    INFO_DERIVED_STAT("example.counter", StatType.INFO_DERIVED),
    STAT_DERIVED_STAT("example.counter", StatType.STAT_DERIVED),

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
