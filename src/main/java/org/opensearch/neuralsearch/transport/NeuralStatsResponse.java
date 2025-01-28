/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.FailedNodeException;
import org.opensearch.action.support.nodes.BaseNodesResponse;
import org.opensearch.cluster.ClusterName;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * NeuralStatsResponse consists of the aggregated responses from the nodes
 */
public class NeuralStatsResponse extends BaseNodesResponse<NeuralStatsNodeResponse> implements ToXContentObject {

    private static final String NODES_KEY = "nodes";
    private Map<String, Object> clusterStats;

    /**
     * Constructor
     *
     * @param in StreamInput
     * @throws IOException thrown when unable to read from stream
     */
    public NeuralStatsResponse(StreamInput in) throws IOException {
        super(new ClusterName(in), in.readList(NeuralStatsNodeResponse::readStats), in.readList(FailedNodeException::new));
        clusterStats = new TreeMap<>(in.readMap());
    }

    /**
     * Constructor
     *
     * @param clusterName name of cluster
     * @param nodes List of NeuralStatsNodeResponses
     * @param failures List of failures from nodes
     * @param clusterStats Cluster level stats only obtained from a single node
     */
    public NeuralStatsResponse(
        ClusterName clusterName,
        List<NeuralStatsNodeResponse> nodes,
        List<FailedNodeException> failures,
        Map<String, Object> clusterStats
    ) {
        super(clusterName, nodes, failures);
        this.clusterStats = new TreeMap<>(clusterStats);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeMap(clusterStats);
    }

    @Override
    public void writeNodesTo(StreamOutput out, List<NeuralStatsNodeResponse> nodes) throws IOException {
        out.writeList(nodes);
    }

    @Override
    public List<NeuralStatsNodeResponse> readNodesFrom(StreamInput in) throws IOException {
        return in.readList(NeuralStatsNodeResponse::readStats);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        // Return cluster level stats
        for (Map.Entry<String, Object> clusterStat : clusterStats.entrySet()) {
            builder.field(clusterStat.getKey(), clusterStat.getValue());
        }

        // Return node level stats
        String nodeId;
        DiscoveryNode node;
        builder.startObject(NODES_KEY);
        for (NeuralStatsNodeResponse neuralStats : getNodes()) {
            node = neuralStats.getNode();
            nodeId = node.getId();
            builder.startObject(nodeId);
            neuralStats.toXContent(builder, params);
            builder.endObject();
            System.out.println("Timothy");
            System.out.println(neuralStats.getStatsMap());
        }
        builder.endObject();
        return builder;
    }
}
