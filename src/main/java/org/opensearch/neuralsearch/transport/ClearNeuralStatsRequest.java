/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.action.support.nodes.BaseNodesRequest;
import org.opensearch.core.common.io.stream.StreamInput;

import java.io.IOException;

public class ClearNeuralStatsRequest extends BaseNodesRequest<ClearNeuralStatsRequest> {
    public ClearNeuralStatsRequest(StreamInput in) throws IOException {
        super(in);
    }

    /**
     * Constructor
     *
     * @param nodeIds NodeIDs from which to retrieve stats
     */
    public ClearNeuralStatsRequest(String[] nodeIds) {
        super(nodeIds);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
