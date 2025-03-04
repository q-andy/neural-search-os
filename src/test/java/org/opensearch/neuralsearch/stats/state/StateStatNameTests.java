/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.events;

import org.opensearch.neuralsearch.stats.state.StateStatName;
import org.opensearch.test.OpenSearchTestCase;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StateStatNameTests extends OpenSearchTestCase {
    public static final EnumSet<StateStatName> STATE_STATS = EnumSet.allOf(StateStatName.class);

    public void test_from_valid() {
        String validStatName = StateStatName.TEXT_EMBEDDING_PROCESSORS.getNameString();
        StateStatName result = StateStatName.from(validStatName);
        assertEquals(StateStatName.TEXT_EMBEDDING_PROCESSORS, result);
    }

    public void test_from_invalid() {
        assertThrows(IllegalArgumentException.class, () -> { StateStatName.from("non_existent_stat"); });
    }

    public void test_unique_names() {
        Set<String> names = new HashSet<>();
        for (StateStatName statName : STATE_STATS) {
            String name = statName.getNameString().toLowerCase(Locale.ROOT);
            assertFalse(String.format("Checking name uniqueness for %s", name), names.contains(name));
            names.add(name);
        }
    }

    public void test_unique_paths() {
        Set<String> paths = new HashSet<>();
        for (StateStatName statName : STATE_STATS) {
            String path = statName.getPath().toLowerCase(Locale.ROOT);
            assertFalse(String.format("Checking path uniqueness for %s", path), paths.contains(path));
            paths.add(path);
        }

        // Check possible path collisions
        // i.e. a full path is a terminal path that should not have any children
        for (StateStatName statName : STATE_STATS) {
            String path = statName.getFullPath().toLowerCase(Locale.ROOT);
            assertFalse(String.format("Checking full path uniqueness for %s", path), paths.contains(path));
            paths.add(path);
        }
    }

    /**
     * Tests if there are any path prefix collisions
     * i.e. every full stat path should be terminal.
     * There should be no other paths that start with another full stat path
     */
    public void test_noPathCollisions() {
        // Convert paths to list and sort them
        List<String> sortedPaths = new ArrayList<>();
        for (StateStatName stat : STATE_STATS) {
            sortedPaths.add(stat.getFullPath().toLowerCase(Locale.ROOT));
        }
        sortedPaths.sort(String::compareTo);

        // Check adjacent paths for collisions
        // When sorted alphabetically, we can reduce the number of path collision comparisons
        for (int i = 0; i < sortedPaths.size() - 1; i++) {
            String currentPath = sortedPaths.get(i);
            String nextPath = sortedPaths.get(i + 1);

            // Check for prefix collision
            assertFalse(
                String.format("Path collision found: %s is a prefix of %s", currentPath, nextPath),
                isPathPrefixOf(currentPath, nextPath)
            );
        }
    }

    private boolean isPathPrefixOf(String path1, String path2) {
        if (path2.startsWith(path1)) {
            if (path1.length() == path2.length()) {
                return false;
            }
            return path2.charAt(path1.length()) == '.';
        }
        return false;
    }

}
