/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import java.util.HashSet;
import java.util.Set;

public enum StatNames {
    NEURAL_QUERY_COUNT("neural_query_count"),
    HYBRID_QUERY_COUNT("hybrid_query_count");

    private String name;

    StatNames(String name) {
        this.name = name;
    }

    /**
     * Get stat name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get all stat names
     *
     * @return set of all stat names
     */
    public static Set<String> getNames() {
        Set<String> names = new HashSet<>();

        for (StatNames statName : StatNames.values()) {
            names.add(statName.getName());
        }
        return names;
    }
}
