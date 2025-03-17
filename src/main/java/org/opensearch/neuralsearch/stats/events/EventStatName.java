/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.events;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.opensearch.neuralsearch.stats.common.StatName;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum that contains all event stat names, paths, and types
 */
@Getter
public enum EventStatName implements StatName {
    TEXT_EMBEDDING_PROCESSOR_EXECUTIONS("text_embedding_executions", "processors.ingest", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_2("performance_2", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_3("performance_3", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_4("performance_4", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_5("performance_5", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_6("performance_6", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_7("performance_7", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_8("performance_8", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_9("performance_9", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_10("performance_10", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_11("performance_11", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_12("performance_12", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_13("performance_13", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_14("performance_14", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_15("performance_15", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_16("performance_16", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_17("performance_17", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_18("performance_18", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_19("performance_19", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_20("performance_20", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER),
    PERFORMANCE_STAT_21("performance_21", "test.performance", EventStatType.TIMESTAMPED_EVENT_COUNTER);

    private final String nameString;
    private final String path;
    private final EventStatType statType;
    private EventStat eventStat;

    /**
     * Enum lookup table by nameString
     */
    private static final Map<String, EventStatName> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toMap(stat -> stat.nameString, stat -> stat));

    /**
     * Constructor
     * @param nameString the unique name of the stat.
     * @param path the unique path of the stat
     * @param statType the category of stat
     */
    EventStatName(String nameString, String path, EventStatType statType) {
        this.nameString = nameString;
        this.path = path;
        this.statType = statType;

        switch (statType) {
            case EventStatType.TIMESTAMPED_EVENT_COUNTER:
                eventStat = new TimestampedEventStat(this);
                break;
        }

        // Validates all event stats are instantiated correctly. This is covered by unit tests as well.
        if (eventStat == null) {
            throw new IllegalArgumentException(
                String.format(Locale.ROOT, "Unable to initialize event stat [%s]. Unrecognized event stat type: [%s]", nameString, statType)
            );
        }
    }

    /**
     * Gets the StatName associated with a unique string name
     * @throws IllegalArgumentException if stat name does not exist
     * @param name the string name of the stat
     * @return the StatName enum associated with that String name
     */
    public static EventStatName from(String name) {
        if (BY_NAME.containsKey(name) == false) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Event stat not found: %s", name));
        }
        return BY_NAME.get(name);
    }

    /**
     * Gets the full dot notation path of the stat, defining its location in the response body
     * @return the destination dot notation path of the stat value
     */
    public String getFullPath() {
        if (StringUtils.isBlank(path)) {
            return nameString;
        }
        return String.join(".", path, nameString);
    }

    @Override
    public String toString() {
        return getNameString();
    }
}
