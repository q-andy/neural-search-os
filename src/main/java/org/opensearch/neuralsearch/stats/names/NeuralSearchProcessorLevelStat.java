/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.names;

public enum NeuralSearchProcessorLevelStat {
    TEXT_EMBEDDING_EXECUTION_COUNT,
    TEXT_IMAGE_EMBEDDING_EXECUTION_COUNT,
    TEXT_IMAGE_EMBEDDING_IMAGE_INFERENCE_COUNT,
    TEXT_IMAGE_EMBEDDING_TEXT_INFERENCE_COUNT,
    TEXT_CHUNKING_EXECUTION_COUNT;

    public static NeuralSearchProcessorLevelStat from(String value) {
        try {
            return NeuralSearchProcessorLevelStat.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("No such neural cluster level stat");
        }
    }
}
