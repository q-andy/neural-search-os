/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.stats;

import java.util.function.Supplier;

public class NeuralStat<T> {
    private Supplier<T> supplier;

    public NeuralStat(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getValue() {
        return supplier.get();
    }
}
