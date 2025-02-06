/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.ActionType;

public class ClearNeuralStatsAction extends ActionType<ClearNeuralStatsResponse> {
    public static final ClearNeuralStatsAction INSTANCE = new ClearNeuralStatsAction();
    public static final String NAME = "cluster:admin/clear_neural_stats_action"; // TODO : figure this out

    /**
     * Constructor
     */
    private ClearNeuralStatsAction() {
        super(NAME, ClearNeuralStatsResponse::new);
    }
}
