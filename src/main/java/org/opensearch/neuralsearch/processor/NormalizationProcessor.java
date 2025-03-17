/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.processor;

import static org.opensearch.neuralsearch.search.util.HybridSearchResultFormatUtil.isHybridQueryStartStopElement;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_10;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_11;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_12;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_13;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_14;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_15;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_16;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_17;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_18;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_19;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_2;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_20;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_21;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_3;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_4;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_5;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_6;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_7;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_8;
import static org.opensearch.neuralsearch.stats.events.EventStatName.PERFORMANCE_STAT_9;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opensearch.action.search.QueryPhaseResultConsumer;
import org.opensearch.action.search.SearchPhaseContext;
import org.opensearch.action.search.SearchPhaseName;
import org.opensearch.action.search.SearchPhaseResults;
import org.opensearch.neuralsearch.processor.combination.ScoreCombinationTechnique;
import org.opensearch.neuralsearch.processor.normalization.ScoreNormalizationTechnique;
import org.opensearch.neuralsearch.stats.events.EventStatsManager;
import org.opensearch.search.SearchPhaseResult;
import org.opensearch.search.fetch.FetchSearchResult;
import org.opensearch.search.pipeline.PipelineProcessingContext;
import org.opensearch.search.query.QuerySearchResult;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Processor for score normalization and combination on post query search results. Updates query results with
 * normalized and combined scores for next phase (typically it's FETCH)
 */
@Log4j2
@AllArgsConstructor
public class NormalizationProcessor extends AbstractScoreHybridizationProcessor {
    public static final String TYPE = "normalization-processor";

    private final String tag;
    private final String description;
    private final ScoreNormalizationTechnique normalizationTechnique;
    private final ScoreCombinationTechnique combinationTechnique;
    private final NormalizationProcessorWorkflow normalizationWorkflow;

    @Override
    <Result extends SearchPhaseResult> void hybridizeScores(
        SearchPhaseResults<Result> searchPhaseResult,
        SearchPhaseContext searchPhaseContext,
        Optional<PipelineProcessingContext> requestContextOptional
    ) {
        if (shouldSkipProcessor(searchPhaseResult)) {
            log.debug("Query results are not compatible with normalization processor");
            return;
        }

        EventStatsManager.increment(PERFORMANCE_STAT_2);
        EventStatsManager.increment(PERFORMANCE_STAT_3);
        EventStatsManager.increment(PERFORMANCE_STAT_4);
        EventStatsManager.increment(PERFORMANCE_STAT_5);
        EventStatsManager.increment(PERFORMANCE_STAT_6);
        EventStatsManager.increment(PERFORMANCE_STAT_7);
        EventStatsManager.increment(PERFORMANCE_STAT_8);
        EventStatsManager.increment(PERFORMANCE_STAT_9);
        EventStatsManager.increment(PERFORMANCE_STAT_10);
        EventStatsManager.increment(PERFORMANCE_STAT_11);
        EventStatsManager.increment(PERFORMANCE_STAT_12);
        EventStatsManager.increment(PERFORMANCE_STAT_13);
        EventStatsManager.increment(PERFORMANCE_STAT_14);
        EventStatsManager.increment(PERFORMANCE_STAT_15);
        EventStatsManager.increment(PERFORMANCE_STAT_16);
        EventStatsManager.increment(PERFORMANCE_STAT_17);
        EventStatsManager.increment(PERFORMANCE_STAT_18);
        EventStatsManager.increment(PERFORMANCE_STAT_19);
        EventStatsManager.increment(PERFORMANCE_STAT_20);
        EventStatsManager.increment(PERFORMANCE_STAT_21);

        List<QuerySearchResult> querySearchResults = getQueryPhaseSearchResults(searchPhaseResult);
        Optional<FetchSearchResult> fetchSearchResult = getFetchSearchResults(searchPhaseResult);
        boolean explain = Objects.nonNull(searchPhaseContext.getRequest().source().explain())
            && searchPhaseContext.getRequest().source().explain();
        NormalizationProcessorWorkflowExecuteRequest request = NormalizationProcessorWorkflowExecuteRequest.builder()
            .querySearchResults(querySearchResults)
            .fetchSearchResultOptional(fetchSearchResult)
            .normalizationTechnique(normalizationTechnique)
            .combinationTechnique(combinationTechnique)
            .explain(explain)
            .pipelineProcessingContext(requestContextOptional.orElse(null))
            .searchPhaseContext(searchPhaseContext)
            .build();
        normalizationWorkflow.execute(request);
    }

    @Override
    public SearchPhaseName getBeforePhase() {
        return SearchPhaseName.QUERY;
    }

    @Override
    public SearchPhaseName getAfterPhase() {
        return SearchPhaseName.FETCH;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isIgnoreFailure() {
        return false;
    }

    private <Result extends SearchPhaseResult> boolean shouldSkipProcessor(SearchPhaseResults<Result> searchPhaseResult) {
        if (Objects.isNull(searchPhaseResult) || !(searchPhaseResult instanceof QueryPhaseResultConsumer)) {
            return true;
        }

        QueryPhaseResultConsumer queryPhaseResultConsumer = (QueryPhaseResultConsumer) searchPhaseResult;
        return queryPhaseResultConsumer.getAtomicArray().asList().stream().filter(Objects::nonNull).noneMatch(this::isHybridQuery);
    }

    /**
     * Return true if results are from hybrid query.
     * @param searchPhaseResult
     * @return true if results are from hybrid query
     */
    private boolean isHybridQuery(final SearchPhaseResult searchPhaseResult) {
        // check for delimiter at the end of the score docs.
        return Objects.nonNull(searchPhaseResult.queryResult())
            && Objects.nonNull(searchPhaseResult.queryResult().topDocs())
            && Objects.nonNull(searchPhaseResult.queryResult().topDocs().topDocs.scoreDocs)
            && searchPhaseResult.queryResult().topDocs().topDocs.scoreDocs.length > 0
            && isHybridQueryStartStopElement(searchPhaseResult.queryResult().topDocs().topDocs.scoreDocs[0]);
    }

    private <Result extends SearchPhaseResult> List<QuerySearchResult> getQueryPhaseSearchResults(
        final SearchPhaseResults<Result> results
    ) {
        return results.getAtomicArray()
            .asList()
            .stream()
            .map(result -> result == null ? null : result.queryResult())
            .collect(Collectors.toList());
    }

    private <Result extends SearchPhaseResult> Optional<FetchSearchResult> getFetchSearchResults(
        final SearchPhaseResults<Result> searchPhaseResults
    ) {
        Optional<Result> optionalFirstSearchPhaseResult = searchPhaseResults.getAtomicArray().asList().stream().findFirst();
        return optionalFirstSearchPhaseResult.map(SearchPhaseResult::fetchResult);
    }
}
