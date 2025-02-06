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
import org.opensearch.neuralsearch.stats.NeuralStats;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.TransportService;

import java.io.IOException;
import java.util.List;

/**
 *  ClearNeuralStatsTransportAction contains the logic to clear all nodes stats
 */
public class ClearNeuralStatsTransportAction extends TransportNodesAction<
    ClearNeuralStatsRequest,
    ClearNeuralStatsResponse,
    ClearNeuralStatsNodeRequest,
    ClearNeuralStatsNodeResponse> {
    /**
     * Constructor
     *
     * @param threadPool ThreadPool to use
     * @param clusterService ClusterService
     * @param transportService TransportService
     * @param actionFilters Action Filters
     */
    @Inject
    public ClearNeuralStatsTransportAction(
        ThreadPool threadPool,
        ClusterService clusterService,
        TransportService transportService,
        ActionFilters actionFilters
    ) {
        super(
            ClearNeuralStatsAction.NAME,
            threadPool,
            clusterService,
            transportService,
            actionFilters,
            ClearNeuralStatsRequest::new,
            ClearNeuralStatsNodeRequest::new,
            ThreadPool.Names.MANAGEMENT,
            ClearNeuralStatsNodeResponse.class
        );
    }

    @Override
    protected ClearNeuralStatsResponse newResponse(
        ClearNeuralStatsRequest request,
        List<ClearNeuralStatsNodeResponse> responses,
        List<FailedNodeException> failures
    ) {
        return new ClearNeuralStatsResponse(clusterService.getClusterName(), responses, failures);
    }

    @Override
    protected ClearNeuralStatsNodeRequest newNodeRequest(ClearNeuralStatsRequest request) {
        return new ClearNeuralStatsNodeRequest(request);
    }

    @Override
    protected ClearNeuralStatsNodeResponse newNodeResponse(StreamInput in) throws IOException {
        return new ClearNeuralStatsNodeResponse(in);
    }

    @Override
    protected ClearNeuralStatsNodeResponse nodeOperation(ClearNeuralStatsNodeRequest request) {
        NeuralStats.instance().resetStats();
        return new ClearNeuralStatsNodeResponse(clusterService.localNode());
    }
}
