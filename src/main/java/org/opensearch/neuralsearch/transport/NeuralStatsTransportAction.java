/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.FailedNodeException;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.nodes.TransportNodesAction;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.inject.Inject;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.neuralsearch.stats.DerivedStats;
import org.opensearch.neuralsearch.stats.NeuralStats;
import org.opensearch.transport.TransportService;
import org.opensearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  NeuralStatsTransportAction contains the logic to extract the stats from the nodes
 */
public class NeuralStatsTransportAction extends TransportNodesAction<
    NeuralStatsRequest,
    NeuralStatsResponse,
    NeuralStatsNodeRequest,
    NeuralStatsNodeResponse> {

    private NeuralStats neuralStats;

    /**
     * Constructor
     *
     * @param threadPool ThreadPool to use
     * @param clusterService ClusterService
     * @param transportService TransportService
     * @param actionFilters Action Filters
     * @param neuralStats NeuralStats object
     */
    @Inject
    public NeuralStatsTransportAction(
        ThreadPool threadPool,
        ClusterService clusterService,
        TransportService transportService,
        ActionFilters actionFilters,
        NeuralStats neuralStats
    ) {
        super(
            NeuralStatsAction.NAME,
            threadPool,
            clusterService,
            transportService,
            actionFilters,
            NeuralStatsRequest::new,
            NeuralStatsNodeRequest::new,
            ThreadPool.Names.MANAGEMENT,
            NeuralStatsNodeResponse.class
        );
        // TODO : inject rather than singleton here
        // this.neuralStats = neuralStats;
        this.neuralStats = NeuralStats.instance();
    }

    @Override
    protected NeuralStatsResponse newResponse(
        NeuralStatsRequest request,
        List<NeuralStatsNodeResponse> responses,
        List<FailedNodeException> failures
    ) {

        Map<String, Object> clusterStats = new HashMap<>();

        clusterStats.put("cluster_level_stat_1", "Yay!");
        // for (String statName : neuralStats.getStats().keySet()) {
        // clusterStats.put(statName, neuralStats.getStats().get(statName).getValue());
        // }'
        DerivedStats derivedStats = DerivedStats.instance();
        clusterStats.putAll(derivedStats.addDerivedStats(responses.stream().map(NeuralStatsNodeResponse::getStatsMap).toList()));

        System.out.println(clusterStats);

        return new NeuralStatsResponse(clusterService.getClusterName(), responses, failures, clusterStats);
    }

    @Override
    protected NeuralStatsNodeRequest newNodeRequest(NeuralStatsRequest request) {
        return new NeuralStatsNodeRequest(request);
    }

    @Override
    protected NeuralStatsNodeResponse newNodeResponse(StreamInput in) throws IOException {
        return new NeuralStatsNodeResponse(in);
    }

    @Override
    protected NeuralStatsNodeResponse nodeOperation(NeuralStatsNodeRequest request) {
        // Reads from NeuralStats to node level stats on an individual node
        Map<String, Long> statValues = new HashMap<>();

        for (String statName : neuralStats.getStats().keySet()) {
            statValues.put(statName, neuralStats.getStats().get(statName).getValue());
        }
        System.out.println("Transport_Action node operation for (ta_node_pasta)" + clusterService.localNode());
        System.out.println(statValues);
        return new NeuralStatsNodeResponse(clusterService.localNode(), statValues);
    }
}
