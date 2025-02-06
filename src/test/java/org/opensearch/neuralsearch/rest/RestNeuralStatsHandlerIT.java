/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.opensearch.client.Response;
import org.opensearch.client.Request;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.core.xcontent.MediaTypeRegistry;
import org.opensearch.neuralsearch.BaseNeuralSearchIT;
import org.opensearch.neuralsearch.plugin.NeuralSearch;
import org.opensearch.neuralsearch.query.NeuralQueryBuilder;
import org.opensearch.neuralsearch.stats.NeuralStats;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class RestNeuralStatsHandlerIT extends BaseNeuralSearchIT {
    private static final String INDEX_NAME = "text_embedding_index";

    private static final String INGEST_PIPELINE_NAME = "ingest-pipeline-1";
    private static final String SEARCH_PIPELINE_NAME = "search-pipeline-1";
    protected static final String QUERY_TEXT = "hello";
    protected static final String LEVEL_1_FIELD = "nested_passages";
    protected static final String LEVEL_2_FIELD = "level_2";
    protected static final String LEVEL_3_FIELD_TEXT = "level_3_text";
    protected static final String LEVEL_3_FIELD_CONTAINER = "level_3_container";
    protected static final String LEVEL_3_FIELD_EMBEDDING = "level_3_embedding";
    protected static final String TEXT_FIELD_VALUE_1 = "hello";
    protected static final String TEXT_FIELD_VALUE_2 = "clown";
    protected static final String TEXT_FIELD_VALUE_3 = "abc";
    private final String INGEST_DOC1 = Files.readString(Path.of(classLoader.getResource("processor/ingest_doc1.json").toURI()));
    private final String INGEST_DOC2 = Files.readString(Path.of(classLoader.getResource("processor/ingest_doc2.json").toURI()));
    private final String INGEST_DOC3 = Files.readString(Path.of(classLoader.getResource("processor/ingest_doc3.json").toURI()));
    private final String INGEST_DOC4 = Files.readString(Path.of(classLoader.getResource("processor/ingest_doc4.json").toURI()));
    private final String INGEST_DOC5 = Files.readString(Path.of(classLoader.getResource("processor/ingest_doc5.json").toURI()));

    private final String TITLE_KNN_FIELD = "title_knn";

    public RestNeuralStatsHandlerIT() throws IOException, URISyntaxException {}

    @Before
    public void setUp() throws Exception {
        super.setUp();
        updateClusterSettings();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        executeClearNeuralStatRequest(Collections.emptyList());
    }

    public void test_happyCase_textEmbedding() throws Exception {
        NeuralStats.instance().resetStats();

        String modelId = null;
        try {
            modelId = uploadTextEmbeddingModel();
            loadModel(modelId);
            createPipelineProcessor(modelId, INGEST_PIPELINE_NAME, ProcessorType.TEXT_EMBEDDING);
            createIndexWithPipeline(INDEX_NAME, "IndexMappings.json", INGEST_PIPELINE_NAME);
            ingestDocument(INDEX_NAME, INGEST_DOC1);
            ingestDocument(INDEX_NAME, INGEST_DOC2);
            ingestDocument(INDEX_NAME, INGEST_DOC3);
            assertEquals(3, getDocCount(INDEX_NAME));

            Response response = executeNeuralStatRequest(new ArrayList<>(), new ArrayList<>());
            String responseBody = EntityUtils.toString(response.getEntity());
            List<Map<String, Object>> nodesStats = parseNodeStatsResponse(responseBody);

            log.info(nodesStats);
            assertEquals(3, getNestedValue(nodesStats.getFirst(), "ingest_processor.text_embedding.executions"));

        } finally {
            wipeOfTestResources(INDEX_NAME, INGEST_PIPELINE_NAME, modelId, null);
        }
    }

    public void test_happyCase_clearNeuralStats() throws Exception {
        NeuralStats.instance().resetStats();

        String modelId = null;
        try {
            modelId = uploadTextEmbeddingModel();
            loadModel(modelId);
            createPipelineProcessor(modelId, INGEST_PIPELINE_NAME, ProcessorType.TEXT_EMBEDDING);
            createIndexWithPipeline(INDEX_NAME, "IndexMappings.json", INGEST_PIPELINE_NAME);
            ingestDocument(INDEX_NAME, INGEST_DOC1);
            ingestDocument(INDEX_NAME, INGEST_DOC2);
            assertEquals(2, getDocCount(INDEX_NAME));

            Response response = executeNeuralStatRequest(new ArrayList<>(), new ArrayList<>());
            String responseBody = EntityUtils.toString(response.getEntity());
            List<Map<String, Object>> nodesStats = parseNodeStatsResponse(responseBody);

            log.info(nodesStats);
            assertEquals(2, getNestedValue(nodesStats.getFirst(), "ingest_processor.text_embedding.executions"));

            executeClearNeuralStatRequest(Collections.emptyList());

            response = executeNeuralStatRequest(new ArrayList<>(), new ArrayList<>());
            responseBody = EntityUtils.toString(response.getEntity());
            nodesStats = parseNodeStatsResponse(responseBody);

            log.info(nodesStats);
            assertEquals(0, getNestedValue(nodesStats.getFirst(), "ingest_processor.text_embedding.executions"));

        } finally {
            wipeOfTestResources(INDEX_NAME, INGEST_PIPELINE_NAME, modelId, null);
        }
    }

    public void test_happyCase_neuralQueryEnricher() throws Exception {
        NeuralStats.instance().resetStats();

        String modelId = null;
        try {
            modelId = prepareModel();
            createSearchRequestProcessor(modelId, SEARCH_PIPELINE_NAME);
            createPipelineProcessor(modelId, INGEST_PIPELINE_NAME, ProcessorType.TEXT_EMBEDDING);
            createIndexWithPipeline(INDEX_NAME, "IndexMappings.json", INGEST_PIPELINE_NAME);

            ingestDocument(INDEX_NAME, INGEST_DOC1);
            ingestDocument(INDEX_NAME, INGEST_DOC2);

            updateIndexSettings(INDEX_NAME, Settings.builder().put("index.search.default_pipeline", SEARCH_PIPELINE_NAME));
            NeuralQueryBuilder neuralQueryBuilder = NeuralQueryBuilder.builder()
                .fieldName(TITLE_KNN_FIELD)
                .queryText("Second")
                .k(10)
                .build();

            Map<String, Object> response = search(INDEX_NAME, neuralQueryBuilder, 2);
            log.info(response);
            assertFalse(response.isEmpty());

            // Stats request
            Response statResponse = executeNeuralStatRequest(new ArrayList<>(), new ArrayList<>());
            String responseBody = EntityUtils.toString(statResponse.getEntity());
            Map<String, Object> nodeStats = parseNodeStatsResponse(responseBody).getFirst();

            log.info(nodeStats);
            assertEquals(2, getNestedValue(nodeStats, "ingest_processor.text_embedding.executions"));
            assertEquals(1, getNestedValue(nodeStats, "search_processor.neural_query_enricher.executions"));
        } finally {
            wipeOfTestResources(INDEX_NAME, INGEST_PIPELINE_NAME, modelId, SEARCH_PIPELINE_NAME);
        }
    }

    protected String uploadTextEmbeddingModel() throws Exception {
        String requestBody = Files.readString(Path.of(classLoader.getResource("processor/UploadModelRequestBody.json").toURI()));
        return registerModelGroupAndUploadModel(requestBody);
    }

    protected Response executeNeuralStatRequest(List<String> nodeIds, List<String> stats) throws IOException {
        String nodePrefix = "";
        if (!nodeIds.isEmpty()) {
            nodePrefix = "/" + String.join(",", nodeIds);
        }

        String statsSuffix = "";
        if (!stats.isEmpty()) {
            statsSuffix = "/" + String.join(",", stats);
        }

        Request request = new Request("GET", NeuralSearch.NEURAL_BASE_URI + nodePrefix + "/stats" + statsSuffix);

        Response response = client().performRequest(request);
        assertEquals(RestStatus.OK, RestStatus.fromCode(response.getStatusLine().getStatusCode()));
        return response;
    }

    protected Response executeClearNeuralStatRequest(List<String> nodeIds) throws IOException {
        String nodePrefix = "";
        if (!nodeIds.isEmpty()) {
            nodePrefix = "/" + String.join(",", nodeIds);
        }

        Request request = new Request("GET", NeuralSearch.NEURAL_BASE_URI + nodePrefix + "/stats/" + RestNeuralStatsHandler.CLEAR_PARAM);

        Response response = client().performRequest(request);
        assertEquals(RestStatus.OK, RestStatus.fromCode(response.getStatusLine().getStatusCode()));
        return response;
    }

    protected Map<String, Object> parseStatsResponse(String responseBody) throws IOException {
        Map<String, Object> responseMap = createParser(MediaTypeRegistry.getDefaultMediaType().xContent(), responseBody).map();
        responseMap.remove("cluster_name");
        responseMap.remove("_nodes");
        return responseMap;
    }

    protected List<Map<String, Object>> parseNodeStatsResponse(String responseBody) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) createParser(
            MediaTypeRegistry.getDefaultMediaType().xContent(),
            responseBody
        ).map().get("nodes");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodeResponses = responseMap.keySet()
            .stream()
            .map(key -> (Map<String, Object>) responseMap.get(key))
            .collect(Collectors.toList());

        return nodeResponses;
    }

    public Object getNestedValue(Map<String, Object> map, String path) {
        String[] keys = path.split("\\.");
        return getNestedValueHelper(map, keys, 0);
    }

    private Object getNestedValueHelper(Map<String, Object> map, String[] keys, int depth) {
        if (map == null) {
            return null;
        }

        Object value = map.get(keys[depth]);

        if (depth == keys.length - 1) {
            return value;
        }

        if (value instanceof Map) {
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            return getNestedValueHelper(nestedMap, keys, depth + 1);
        }

        return null;
    }
}
