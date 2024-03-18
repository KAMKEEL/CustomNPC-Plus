package noppes.npcs.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomNPCsThreader {
	public static final Executor customNPCThread = Executors.newSingleThreadExecutor();
}
