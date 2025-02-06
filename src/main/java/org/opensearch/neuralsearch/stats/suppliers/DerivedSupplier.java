/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats.suppliers;

import org.opensearch.neuralsearch.stats.DerivedStats;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * DerivedSupplier a derived stat value
 */
public class DerivedSupplier<T> implements Supplier<T> {
    private DerivedStats derivedStats;
    private Function<DerivedStats, T> getter;

    /**
     * Constructor
     */
    public DerivedSupplier(DerivedStats derivedStats, Function<DerivedStats, T> getter) {
        this.derivedStats = derivedStats;
        this.getter = getter;
    }

    @Override
    public T get() {
        return getter.apply(derivedStats);
    }
}
