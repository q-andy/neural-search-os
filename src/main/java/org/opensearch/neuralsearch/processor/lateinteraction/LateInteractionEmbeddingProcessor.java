/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.processor.lateinteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.opensearch.cluster.service.ClusterService;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.common.util.CollectionUtils;
import org.opensearch.env.Environment;
import org.opensearch.ingest.IngestDocument;
import org.opensearch.ingest.IngestDocumentWrapper;
import org.opensearch.neuralsearch.ml.MLCommonsClientAccessor;

import lombok.extern.log4j.Log4j2;
import org.opensearch.neuralsearch.processor.InferenceProcessor;
import org.opensearch.neuralsearch.processor.TextInferenceRequest;
import org.opensearch.neuralsearch.processor.optimization.TextEmbeddingInferenceFilter;
import org.opensearch.transport.client.OpenSearchClient;

/**
 * This processor is used for user input data text embedding processing, model_id can be used to indicate which model user use,
 * and field_map can be used to indicate which fields needs text embedding and the corresponding keys for the text embedding results.
 */
@Log4j2
public final class LateInteractionEmbeddingProcessor extends InferenceProcessor {
    public static final String TYPE = "late_interaction_embedding";
    public static final String LIST_TYPE_NESTED_MAP_KEY = "knn";
    private final OpenSearchClient openSearchClient;
    private final boolean skipExisting;

    public LateInteractionEmbeddingProcessor(
        String tag,
        String description,
        int batchSize,
        String modelId,
        Map<String, Object> fieldMap,
        boolean skipExisting,
        TextEmbeddingInferenceFilter textEmbeddingInferenceFilter,
        OpenSearchClient openSearchClient,
        MLCommonsClientAccessor clientAccessor,
        Environment environment,
        ClusterService clusterService
    ) {
        super(tag, description, batchSize, TYPE, LIST_TYPE_NESTED_MAP_KEY, modelId, fieldMap, clientAccessor, environment, clusterService);
        this.skipExisting = skipExisting;
        this.openSearchClient = openSearchClient;
    }

    @Override
    public void doExecute(
        IngestDocument ingestDocument,
        Map<String, Object> processMap,
        List<String> inferenceList,
        BiConsumer<IngestDocument, Exception> handler
    ) {
        mlCommonsClientAccessor.inferenceSentencesForLateInteraction(
            TextInferenceRequest.builder().modelId(this.modelId).inputTexts(inferenceList).build(),
            ActionListener.wrap(multiVectors -> {
                // Only care about first multivector for pOC
                List<List<Number>> multiVector = multiVectors.getFirst();
                List<List<Number>> projectedMultiVector = projectVectors(multiVector, 128);
                setVectorFieldsToDocument(ingestDocument, processMap, List.of(projectedMultiVector));
                setAverageVectorField(ingestDocument, multiVector);
                handler.accept(ingestDocument, null);
            }, e -> { handler.accept(null, e); })
        );
    }

    @Override
    public void doBatchExecute(List<String> inferenceList, Consumer<List<?>> handler, Consumer<Exception> onException) {
        mlCommonsClientAccessor.inferenceSentences(
            TextInferenceRequest.builder().modelId(this.modelId).inputTexts(inferenceList).build(),
            ActionListener.wrap(handler::accept, onException)
        );
    }

    @Override
    public void subBatchExecute(List<IngestDocumentWrapper> ingestDocumentWrappers, Consumer<List<IngestDocumentWrapper>> handler) {
        try {
            if (CollectionUtils.isEmpty(ingestDocumentWrappers)) {
                handler.accept(ingestDocumentWrappers);
                return;
            }
            List<DataForInference> dataForInferences = getDataForInference(ingestDocumentWrappers);
            List<String> inferenceList = constructInferenceTexts(dataForInferences);
            if (inferenceList.isEmpty()) {
                handler.accept(ingestDocumentWrappers);
                return;
            }
            doSubBatchExecute(ingestDocumentWrappers, inferenceList, dataForInferences, handler);
        } catch (Exception e) {
            updateWithExceptions(ingestDocumentWrappers, handler, e);
        }
    }

    private List<List<Number>> projectVectors(List<List<Number>> vectors, int targetDims) {
        List<List<Number>> projected = new ArrayList<>();
        for (List<Number> vector : vectors) {
            projected.add(new ArrayList<>(vector.subList(0, targetDims)));
        }
        return projected;
    }

    private void setAverageVectorField(IngestDocument ingestDocument, List<List<Number>> vectors) {
        String targetField = "average_vector";
        List<Number> averageVector = new ArrayList<>();

        // Assumes all vectors are same length
        int dimensions = vectors.getFirst().size();
        for (int d = 0; d < dimensions; d++) {
            // Careful on how to handle this. There may be precision loss if we divide too early, or overflow
            // For now, we'll add first then divide
            float total = 0;
            for (List<Number> vector : vectors) {
                total += vector.get(d).floatValue();
            }
            averageVector.add(total / vectors.size());
        }

        ingestDocument.appendFieldValue(targetField, averageVector);
    }
}
