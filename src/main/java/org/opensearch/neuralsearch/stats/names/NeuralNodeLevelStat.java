/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

public enum NeuralNodeLevelStat {
    NEURAL_QUERY_COUNT,
    HYBRID_QUERY_COUNT;

    public static NeuralNodeLevelStat from(String value) {
        try {
            return NeuralNodeLevelStat.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("No such neural node level stat");
        }
    }
}
