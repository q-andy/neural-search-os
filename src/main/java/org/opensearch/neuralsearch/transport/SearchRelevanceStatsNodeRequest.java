/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import lombok.Getter;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.transport.TransportRequest;

import java.io.IOException;

/**
 *  SearchRelevanceStatsNodeRequest represents the request to an individual node
 */
public class SearchRelevanceStatsNodeRequest extends TransportRequest {
    @Getter
    private SearchRelevanceStatsRequest request;

    /**
     * Constructor
     */
    public SearchRelevanceStatsNodeRequest() {
        super();
    }

    /**
     * Constructor
     *
     * @param in input stream
     * @throws IOException in case of I/O errors
     */
    public SearchRelevanceStatsNodeRequest(StreamInput in) throws IOException {
        super(in);
        request = new SearchRelevanceStatsRequest(in);
    }

    /**
     * Constructor
     *
     * @param request NeuralStatsRequest
     */
    public SearchRelevanceStatsNodeRequest(SearchRelevanceStatsRequest request) {
        this.request = request;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        request.writeTo(out);
    }
}
