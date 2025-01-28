/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

public enum NeuralClusterLevelStat {
    FORCE_INFERENCE;

    public static NeuralClusterLevelStat from(String value) {
        try {
            return NeuralClusterLevelStat.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("No such neural cluster level stat");
        }
    }
}
