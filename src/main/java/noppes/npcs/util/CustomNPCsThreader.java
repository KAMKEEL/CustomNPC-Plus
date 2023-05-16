package noppes.npcs.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomNPCsThreader {
	private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	public static final Executor playerDataThread = Executors.newSingleThreadExecutor();
}
