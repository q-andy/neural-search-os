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
 *  ClearNeuralStatsNodeRequest represents the request to an individual node
 */
public class ClearNeuralStatsNodeRequest extends TransportRequest {
    @Getter
    private ClearNeuralStatsRequest request;

    /**
     * Constructor
     */
    public ClearNeuralStatsNodeRequest() {
        super();
    }

    /**
     * Constructor
     *
     * @param in input stream
     * @throws IOException in case of I/O errors
     */
    public ClearNeuralStatsNodeRequest(StreamInput in) throws IOException {
        super(in);
        request = new ClearNeuralStatsRequest(in);
    }

    /**
     * Constructor
     *
     * @param request ClearNeuralStatsRequest
     */
    public ClearNeuralStatsNodeRequest(ClearNeuralStatsRequest request) {
        this.request = request;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        request.writeTo(out);
    }
}
