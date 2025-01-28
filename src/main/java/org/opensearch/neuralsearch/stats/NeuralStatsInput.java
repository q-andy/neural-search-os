/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import lombok.Builder;
import lombok.Getter;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.common.io.stream.Writeable;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.neuralsearch.stats.names.NeuralClusterLevelStat;
import org.opensearch.neuralsearch.stats.names.NeuralNodeLevelStat;
import org.opensearch.neuralsearch.stats.names.NeuralStatLevel;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;

@Getter
public class NeuralStatsInput implements ToXContentObject, Writeable {
    public static final String TARGET_STAT_LEVEL = "target_stat_levels";
    public static final String CLUSTER_LEVEL_STATS = "cluster_level_stats";
    public static final String NODE_LEVEL_STATS = "node_level_stats";
    public static final String NODE_IDS = "node_ids";

    /**
     * Determines levels of stats to retrieve. If empty, will not retrieve any
     */
    private EnumSet<NeuralStatLevel> targetStatLevels;
    /**
     * Which cluster level stats will be retrieved.
     */
    private EnumSet<NeuralClusterLevelStat> clusterLevelStats;

    /**
     * Which node level stats will be retrieved.
     */
    private EnumSet<NeuralNodeLevelStat> nodeLevelStats;

    /**
     * Which node's stats will be retrieved.
     */
    private Set<String> nodeIds;

    @Builder
    public NeuralStatsInput(
        EnumSet<NeuralStatLevel> targetStatLevels,
        EnumSet<NeuralClusterLevelStat> clusterLevelStats,
        EnumSet<NeuralNodeLevelStat> nodeLevelStats,
        Set<String> nodeIds
    ) {
        this.targetStatLevels = targetStatLevels;
        this.clusterLevelStats = clusterLevelStats;
        this.nodeLevelStats = nodeLevelStats;
        this.nodeIds = nodeIds;
    }

    public NeuralStatsInput() {
        this.targetStatLevels = EnumSet.noneOf(NeuralStatLevel.class);
        this.clusterLevelStats = EnumSet.noneOf(NeuralClusterLevelStat.class);
        this.nodeLevelStats = EnumSet.noneOf(NeuralNodeLevelStat.class);
        this.nodeIds = new HashSet<>();
    }

    public NeuralStatsInput(StreamInput input) throws IOException {
        targetStatLevels = input.readBoolean() ? input.readEnumSet(NeuralStatLevel.class) : EnumSet.noneOf(NeuralStatLevel.class);
        clusterLevelStats = input.readBoolean()
            ? input.readEnumSet(NeuralClusterLevelStat.class)
            : EnumSet.noneOf(NeuralClusterLevelStat.class);
        nodeLevelStats = input.readBoolean() ? input.readEnumSet(NeuralNodeLevelStat.class) : EnumSet.noneOf(NeuralNodeLevelStat.class);
        nodeIds = input.readBoolean() ? new HashSet<>(input.readStringList()) : new HashSet<>();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        writeEnumSet(out, targetStatLevels);
        writeEnumSet(out, clusterLevelStats);
        writeEnumSet(out, nodeLevelStats);
        out.writeOptionalStringCollection(nodeIds);
    }

    private void writeEnumSet(StreamOutput out, EnumSet<?> set) throws IOException {
        if (set != null && set.size() > 0) {
            out.writeBoolean(true);
            out.writeEnumSet(set);
        } else {
            out.writeBoolean(false);
        }
    }

    public static NeuralStatsInput parse(XContentParser parser) throws IOException {
        EnumSet<NeuralStatLevel> targetStatLevels = EnumSet.noneOf(NeuralStatLevel.class);
        EnumSet<NeuralClusterLevelStat> clusterLevelStats = EnumSet.noneOf(NeuralClusterLevelStat.class);
        EnumSet<NeuralNodeLevelStat> nodeLevelStats = EnumSet.noneOf(NeuralNodeLevelStat.class);
        Set<String> nodeIds = new HashSet<>();

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case TARGET_STAT_LEVEL:
                    parseField(
                        parser,
                        targetStatLevels,
                        input -> NeuralStatLevel.from(input.toUpperCase(Locale.ROOT)),
                        NeuralStatLevel.class
                    );
                    break;
                case CLUSTER_LEVEL_STATS:
                    parseField(
                        parser,
                        clusterLevelStats,
                        input -> NeuralClusterLevelStat.from(input.toUpperCase(Locale.ROOT)),
                        NeuralClusterLevelStat.class
                    );
                    break;
                case NODE_LEVEL_STATS:
                    parseField(
                        parser,
                        nodeLevelStats,
                        input -> NeuralNodeLevelStat.from(input.toUpperCase(Locale.ROOT)),
                        NeuralNodeLevelStat.class
                    );
                    break;
                case NODE_IDS:
                    parseArrayField(parser, nodeIds);
                    break;
                default:
                    parser.skipChildren();
                    break;
            }
        }
        return NeuralStatsInput.builder()
            .targetStatLevels(targetStatLevels)
            .clusterLevelStats(clusterLevelStats)
            .nodeLevelStats(nodeLevelStats)
            .nodeIds(nodeIds)
            .build();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        if (targetStatLevels != null) {
            builder.field(TARGET_STAT_LEVEL, targetStatLevels);
        }
        if (clusterLevelStats != null) {
            builder.field(CLUSTER_LEVEL_STATS, clusterLevelStats);
        }
        if (nodeLevelStats != null) {
            builder.field(NODE_LEVEL_STATS, nodeLevelStats);
        }
        if (nodeIds != null) {
            builder.field(NODE_IDS, nodeIds);
        }
        builder.endObject();
        return builder;
    }

    public boolean retrieveAllClusterLevelStats() {
        return clusterLevelStats == null || clusterLevelStats.size() == 0;
    }

    public boolean retrieveAllNodeLevelStats() {
        return nodeLevelStats == null || nodeLevelStats.size() == 0;
    }

    public boolean retrieveStatsOnAllNodes() {
        return nodeIds == null || nodeIds.size() == 0;
    }

    public boolean retrieveStat(Enum<?> key) {
        if (key instanceof NeuralClusterLevelStat) {
            return retrieveAllClusterLevelStats() || clusterLevelStats.contains(key);
        }
        if (key instanceof NeuralNodeLevelStat) {
            return retrieveAllNodeLevelStats() || nodeLevelStats.contains(key);
        }
        return false;
    }

    public boolean onlyRetrieveClusterLevelStats() {
        if (targetStatLevels == null || targetStatLevels.size() == 0) {
            return false;
        }
        return !targetStatLevels.contains(NeuralStatLevel.NODE);
    }

    public static void parseArrayField(XContentParser parser, Set<String> set) throws IOException {
        parseField(parser, set, null, String.class);
    }

    public static <T> void parseField(XContentParser parser, Set<T> set, Function<String, T> function, Class<T> clazz) throws IOException {
        ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
            String value = parser.text();
            if (function != null) {
                set.add(function.apply(value));
            } else {
                if (clazz.isInstance(value)) {
                    set.add(clazz.cast(value));
                }
            }
        }
    }
}
