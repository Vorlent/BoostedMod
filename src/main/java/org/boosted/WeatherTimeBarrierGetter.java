package org.boosted;

import org.boosted.util.WeatherTimeBarrier;

public interface WeatherTimeBarrierGetter {
    default WeatherTimeBarrier getWeatherTimeBarrier() {
        throw new UnsupportedOperationException("WeatherTimeBarrierGetter.getWeatherTimeBarrier() has not been implemented");
    }
}
