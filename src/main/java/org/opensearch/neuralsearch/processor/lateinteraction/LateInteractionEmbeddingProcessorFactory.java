/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.processor.lateinteraction;

import org.opensearch.cluster.service.ClusterService;
import org.opensearch.env.Environment;
import org.opensearch.ingest.AbstractBatchingProcessor;
import org.opensearch.neuralsearch.ml.MLCommonsClientAccessor;
import org.opensearch.neuralsearch.processor.optimization.TextEmbeddingInferenceFilter;
import org.opensearch.transport.client.OpenSearchClient;

import java.util.Map;

import static org.opensearch.ingest.ConfigurationUtils.readMap;
import static org.opensearch.ingest.ConfigurationUtils.readStringProperty;
import static org.opensearch.neuralsearch.processor.InferenceProcessor.FIELD_MAP_FIELD;
import static org.opensearch.neuralsearch.processor.InferenceProcessor.MODEL_ID_FIELD;

/**
 * Factory for text embedding ingest processor for ingestion pipeline. Instantiates processor based on user provided input.
 */
public final class LateInteractionEmbeddingProcessorFactory extends AbstractBatchingProcessor.Factory {

    private final OpenSearchClient openSearchClient;

    private final MLCommonsClientAccessor clientAccessor;

    private final Environment environment;

    private final ClusterService clusterService;

    public LateInteractionEmbeddingProcessorFactory(
        final OpenSearchClient openSearchClient,
        final MLCommonsClientAccessor clientAccessor,
        final Environment environment,
        final ClusterService clusterService
    ) {
        super(LateInteractionEmbeddingProcessor.TYPE);
        this.openSearchClient = openSearchClient;
        this.clientAccessor = clientAccessor;
        this.environment = environment;
        this.clusterService = clusterService;
    }

    @Override
    protected AbstractBatchingProcessor newProcessor(String tag, String description, int batchSize, Map<String, Object> config) {
        String modelId = readStringProperty(LateInteractionEmbeddingProcessor.TYPE, tag, config, MODEL_ID_FIELD);
        Map<String, Object> fieldMap = readMap(LateInteractionEmbeddingProcessor.TYPE, tag, config, FIELD_MAP_FIELD);
        boolean skipExisting = true;
        return new LateInteractionEmbeddingProcessor(
            tag,
            description,
            batchSize,
            modelId,
            fieldMap,
            skipExisting,
            skipExisting ? new TextEmbeddingInferenceFilter(fieldMap) : null,
            openSearchClient,
            clientAccessor,
            environment,
            clusterService
        );
    }
}
