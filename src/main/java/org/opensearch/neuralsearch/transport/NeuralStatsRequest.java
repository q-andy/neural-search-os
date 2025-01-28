/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import lombok.Getter;
import org.opensearch.action.support.nodes.BaseNodesRequest;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * NeuralStatsRequest gets node (cluster) level Stats for KNN
 * By default, all parameters will be true
 */
public class NeuralStatsRequest extends BaseNodesRequest<NeuralStatsRequest> {

    /**
     * Key indicating all stats should be retrieved
     */
    public static final String ALL_STATS_KEY = "_all";
    @Getter
    private final Set<String> statsToBeRetrieved;

    /**
     * Empty constructor needed for NeuralStatsTransportAction
     */
    public NeuralStatsRequest() {
        super((String[]) null);
        statsToBeRetrieved = new HashSet<>();
    }

    /**
     * Constructor
     *
     * @param in input stream
     * @throws IOException in case of I/O errors
     */
    public NeuralStatsRequest(StreamInput in) throws IOException {
        super(in);
        statsToBeRetrieved = in.readSet(StreamInput::readString);
    }

    /**
     * Constructor
     *
     * @param nodeIds NodeIDs from which to retrieve stats
     */
    public NeuralStatsRequest(String... nodeIds) {
        super(nodeIds);
        statsToBeRetrieved = new HashSet<>();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeStringCollection(statsToBeRetrieved);
    }
}
