/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.ingest.IngestService;
import org.opensearch.search.pipeline.PipelineConfiguration;
import org.opensearch.search.pipeline.SearchPipelineService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class abstracts information related to underlying OpenSearch cluster
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class StatsInfoUtil {
    private ClusterService clusterService;
    private SearchPipelineService searchPipelineService;
    private IngestService ingestService;

    private static StatsInfoUtil instance;

    /**
     * Return instance of the cluster context, must be initialized first for proper usage
     * @return instance of cluster context
     */
    public static synchronized StatsInfoUtil instance() {
        if (instance == null) {
            instance = new StatsInfoUtil();
        }
        return instance;
    }

    /**
     * Initializes instance of info util by injecting dependencies
     * @param clusterService
     */
    public void initialize(ClusterService clusterService, SearchPipelineService searchPipelineService, IngestService ingestService) {
        this.clusterService = clusterService;
        this.searchPipelineService = searchPipelineService;
        this.ingestService = ingestService;
        System.out.println("info_pasta");
        System.out.println(clusterService);
        System.out.println(searchPipelineService);
        System.out.println(ingestService);
    }

    public List<Map<String, Object>> listAllPipelineConfigs() {
        List<String> info = new ArrayList<>();
        System.out.println("info_pasta");

        System.out.println(clusterService);
        System.out.println(searchPipelineService);
        System.out.println(ingestService);

        for (PipelineConfiguration pipelineConfiguration : SearchPipelineService.getPipelines(clusterService.state())) {
            for (Map.Entry<String, Object> entry : pipelineConfiguration.getConfigAsMap().entrySet()) {
                info.add(entry.toString());
            }
        }

        System.out.println(info);
        return SearchPipelineService.getPipelines(clusterService.state()).stream().map(pipelineConfiguration -> {
            return pipelineConfiguration.getConfigAsMap();
        }).collect(Collectors.toList());
    }
}
