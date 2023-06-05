package noppes.npcs.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomNPCsThreader {
	public static final Executor playerDataThread = Executors.newSingleThreadExecutor();
}
