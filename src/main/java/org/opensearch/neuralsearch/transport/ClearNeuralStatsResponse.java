/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.FailedNodeException;
import org.opensearch.action.support.nodes.BaseNodesResponse;
import org.opensearch.cluster.ClusterName;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;

public class ClearNeuralStatsResponse extends BaseNodesResponse<ClearNeuralStatsNodeResponse> implements ToXContentObject {
    private static final String NODES_KEY = "nodes";

    /**
     * Constructor
     *
     * @param in StreamInput
     * @throws IOException thrown when unable to read from stream
     */
    public ClearNeuralStatsResponse(StreamInput in) throws IOException {
        super(new ClusterName(in), in.readList(ClearNeuralStatsNodeResponse::new), in.readList(FailedNodeException::new));
    }

    /**
     * Constructor
     *
     * @param clusterName name of cluster
     * @param nodes List of NeuralStatsNodeResponses
     * @param failures List of failures from nodes
     */
    public ClearNeuralStatsResponse(ClusterName clusterName, List<ClearNeuralStatsNodeResponse> nodes, List<FailedNodeException> failures) {
        super(clusterName, nodes, failures);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }

    @Override
    public void writeNodesTo(StreamOutput out, List<ClearNeuralStatsNodeResponse> nodes) throws IOException {
        out.writeList(nodes);
    }

    @Override
    public List<ClearNeuralStatsNodeResponse> readNodesFrom(StreamInput in) throws IOException {
        return in.readList(ClearNeuralStatsNodeResponse::new);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        // TODO : response should go here
        return builder;
    }
}
