/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.rest;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.neuralsearch.processor.InferenceProcessorTestCase;
import org.opensearch.neuralsearch.settings.NeuralSearchSettingsAccessor;
import org.opensearch.neuralsearch.stats.SearchRelevanceStatsInput;
import org.opensearch.neuralsearch.stats.events.EventStatName;
import org.opensearch.neuralsearch.stats.info.InfoStatName;
import org.opensearch.neuralsearch.transport.SearchRelevanceStatsAction;
import org.opensearch.neuralsearch.transport.SearchRelevanceStatsRequest;
import org.opensearch.neuralsearch.transport.SearchRelevanceStatsResponse;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestChannel;
import org.opensearch.rest.RestRequest;
import org.opensearch.test.rest.FakeRestRequest;
import org.opensearch.threadpool.TestThreadPool;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.client.node.NodeClient;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestSearchRelevanceStatsActionTests extends InferenceProcessorTestCase {
    private NodeClient client;
    private ThreadPool threadPool;

    @Mock
    RestChannel channel;

    @Mock
    private NeuralSearchSettingsAccessor settingsAccessor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        threadPool = new TestThreadPool(this.getClass().getSimpleName() + "ThreadPool");
        client = spy(new NodeClient(Settings.EMPTY, threadPool));

        doAnswer(invocation -> {
            ActionListener<SearchRelevanceStatsResponse> actionListener = invocation.getArgument(2);
            return null;
        }).when(client).execute(eq(SearchRelevanceStatsAction.INSTANCE), any(), any());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        threadPool.shutdown();
        client.close();
    }

    public void test_execute() throws Exception {
        when(settingsAccessor.isStatsEnabled()).thenReturn(true);
        RestSearchRelevanceStatsAction restSearchRelevanceStatsAction = new RestSearchRelevanceStatsAction(settingsAccessor);

        RestRequest request = getRestRequest();
        restSearchRelevanceStatsAction.handleRequest(request, channel, client);

        ArgumentCaptor<SearchRelevanceStatsRequest> argumentCaptor = ArgumentCaptor.forClass(SearchRelevanceStatsRequest.class);
        verify(client, times(1)).execute(eq(SearchRelevanceStatsAction.INSTANCE), argumentCaptor.capture(), any());

        SearchRelevanceStatsInput capturedInput = argumentCaptor.getValue().getSearchRelevanceStatsInput();
        assertEquals(capturedInput.getEventStatNames(), EnumSet.allOf(EventStatName.class));
        assertEquals(capturedInput.getInfoStatNames(), EnumSet.allOf(InfoStatName.class));
    }

    public void test_handleRequest_disabledForbidden() throws Exception {
        when(settingsAccessor.isStatsEnabled()).thenReturn(false);
        RestSearchRelevanceStatsAction restSearchRelevanceStatsAction = new RestSearchRelevanceStatsAction(settingsAccessor);

        RestRequest request = getRestRequest();
        restSearchRelevanceStatsAction.handleRequest(request, channel, client);

        verify(client, never()).execute(eq(SearchRelevanceStatsAction.INSTANCE), any(), any());

        ArgumentCaptor<BytesRestResponse> responseCaptor = ArgumentCaptor.forClass(BytesRestResponse.class);
        verify(channel).sendResponse(responseCaptor.capture());

        BytesRestResponse response = responseCaptor.getValue();
        assertEquals(RestStatus.FORBIDDEN, response.status());
    }

    public void test_handleRequest_invalidStatParameter() throws Exception {
        when(settingsAccessor.isStatsEnabled()).thenReturn(true);
        RestSearchRelevanceStatsAction restSearchRelevanceStatsAction = new RestSearchRelevanceStatsAction(settingsAccessor);

        // Create request with invalid stat parameter
        Map<String, String> params = new HashMap<>();
        params.put("stat", "INVALID_STAT");
        RestRequest request = new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY)
                .withParams(params)
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> restSearchRelevanceStatsAction.handleRequest(request, channel, client)
        );

        verify(client, never()).execute(eq(SearchRelevanceStatsAction.INSTANCE), any(), any());
    }

    private RestRequest getRestRequest() {
        Map<String, String> params = new HashMap<>();
        return new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY).withParams(params).build();
    }
}
