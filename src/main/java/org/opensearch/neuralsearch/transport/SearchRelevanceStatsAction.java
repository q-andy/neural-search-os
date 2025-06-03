/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.transport;

import org.opensearch.action.ActionType;
import org.opensearch.core.common.io.stream.Writeable;

/**
 * SearchRelevanceStatsAction class
 */
public class SearchRelevanceStatsAction extends ActionType<SearchRelevanceStatsResponse> {

    public static final SearchRelevanceStatsAction INSTANCE = new SearchRelevanceStatsAction();
    public static final String NAME = "cluster:admin/neural_stats_action";

    /**
     * Constructor
     */
    private SearchRelevanceStatsAction() {
        super(NAME, SearchRelevanceStatsResponse::new);
    }

    @Override
    public Writeable.Reader<SearchRelevanceStatsResponse> getResponseReader() {
        return SearchRelevanceStatsResponse::new;
    }
}
