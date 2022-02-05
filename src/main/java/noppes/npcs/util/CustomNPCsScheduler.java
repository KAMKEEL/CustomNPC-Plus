package noppes.npcs.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomNPCsScheduler {
	private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	
	public static void runTack(Runnable task, int delay){
		executor.schedule(task, delay, TimeUnit.MILLISECONDS);		
	}
	
	public static void runTack(Runnable task){
		executor.schedule(task, 0, TimeUnit.MILLISECONDS);		
	}
}
