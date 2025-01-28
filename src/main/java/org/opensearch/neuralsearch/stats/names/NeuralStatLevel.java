/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

public enum NeuralStatLevel {
    CLUSTER,
    NODE,
    SEARCH_PROCESSOR,
    INGEST_PROCESSOR;

    public static NeuralStatLevel from(String value) {
        try {
            return NeuralStatLevel.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("No such neural stat level");
        }
    }
}
