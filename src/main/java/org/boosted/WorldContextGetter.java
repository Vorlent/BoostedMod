package org.boosted;

import org.boosted.BoostedWorldContext;

public interface WorldContextGetter {
    default BoostedWorldContext getBoostedWorldContext() {
        throw new UnsupportedOperationException();
    }
}
