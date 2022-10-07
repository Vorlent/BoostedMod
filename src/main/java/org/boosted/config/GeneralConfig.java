package org.boosted.config;

import java.util.List;
import java.util.Set;
//import org.jmt.mcmt.Constants;

public class GeneralConfig {

    // Actual config stuff
    //////////////////////

    // General
    public static boolean disabled = false;

    // Parallelism
    public static int paraMax;
    public static ParaMaxMode paraMaxMode;

    // World
    public static boolean disableWorld = false;
    public static boolean disableWorldPostTick = true;

    //WorldChunk
    public static boolean disableMultiChunk = false;

    // Entity
    public static boolean disableEntity = true;

    // TE
    public static boolean disableTileEntity = true;
    // Misc
    public static boolean disableEnvironment = true;
    public static boolean disableChunkProvider;

    //Debug
    public static boolean enableChunkTimeout;
    public static boolean enableTimeoutRegen;
    public static boolean enableBlankReturn;
    public static int timeoutCount;

    // More Debug
    public static boolean opsTracing;

    public enum ParaMaxMode {
        Standard,
        Override,
        Reduction
    }

    // Functions intended for usage
    ///////////////////////////////

    public static int getParallelism() {
        switch (GeneralConfig.paraMaxMode) {
            case Standard:
                return GeneralConfig.paraMax <= 1 ?
                        Runtime.getRuntime().availableProcessors() :
                        Math.max(2, Math.min(Runtime.getRuntime().availableProcessors(), GeneralConfig.paraMax));
            case Override:
                return GeneralConfig.paraMax <= 1 ?
                        Runtime.getRuntime().availableProcessors() :
                        Math.max(2, GeneralConfig.paraMax);
            case Reduction:
                return Math.max(
                        Runtime.getRuntime().availableProcessors() - Math.max(0, GeneralConfig.paraMax),
                        2);
        }
        // Unsure quite how this is "Reachable code" but ok I guess
        return Runtime.getRuntime().availableProcessors();
    }

}
