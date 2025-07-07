package noppes.npcs.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomNPCsThreader {
    /**
     * Executor used for all asynchronous profile and player data saves.
     * It is single-threaded so submitted tasks run sequentially and the
     * save order for a given player is preserved.
     */
    public static final Executor customNPCThread = Executors.newSingleThreadExecutor();
}
