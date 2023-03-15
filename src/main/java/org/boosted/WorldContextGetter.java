package org.boosted;

public interface WorldContextGetter {
    default BoostedWorldContext getBoostedWorldContext() {
        throw new UnsupportedOperationException("WorldContextGetter.getBoostedWorldContext has not been implemented");
    }
}
