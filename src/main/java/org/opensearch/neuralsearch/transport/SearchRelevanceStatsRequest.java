/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import lombok.Getter;
import org.opensearch.action.support.nodes.BaseNodesRequest;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.neuralsearch.stats.SearchRelevanceStatsInput;

import java.io.IOException;

/**
 * NeuralStatsRequest gets node (cluster) level Stats for Neural
 * By default, all parameters will be true
 */
public class SearchRelevanceStatsRequest extends BaseNodesRequest<SearchRelevanceStatsRequest> {

    /**
     * Key indicating all stats should be retrieved
     */
    @Getter
    private final SearchRelevanceStatsInput searchRelevanceStatsInput;

    /**
     * Empty constructor needed for SearchRelevanceStatsTransportAction
     */
    public SearchRelevanceStatsRequest() {
        super((String[]) null);
        this.searchRelevanceStatsInput = new SearchRelevanceStatsInput();
    }

    /**
     * Constructor
     *
     * @param in input stream
     * @throws IOException in case of I/O errors
     */
    public SearchRelevanceStatsRequest(StreamInput in) throws IOException {
        super(in);
        this.searchRelevanceStatsInput = new SearchRelevanceStatsInput(in);
    }

    /**
     * Constructor
     *
     * @param nodeIds NodeIDs from which to retrieve stats
     */
    public SearchRelevanceStatsRequest(String[] nodeIds, SearchRelevanceStatsInput searchRelevanceStatsInput) {
        super(nodeIds);
        this.searchRelevanceStatsInput = searchRelevanceStatsInput;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        searchRelevanceStatsInput.writeTo(out);
    }
}
