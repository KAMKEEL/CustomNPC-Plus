package noppes.npcs;

import noppes.npcs.constants.EnumScriptType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LogWriter {
	private final static String name = "CustomNPCs";
	private final static Logger logger = Logger.getLogger(name);

	private final static WeakHashMap<UUID, NPCStamp> InitCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> TickCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> InteractCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> DialogCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> DamagedCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> KilledCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> AttackCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> TargetCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> CollideCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> KillsCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> DialogCloseCacheMap = new WeakHashMap<UUID, NPCStamp>();
	private final static WeakHashMap<UUID, NPCStamp> TimerCacheMap = new WeakHashMap<UUID, NPCStamp>();

	private final static SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	private static Handler handler;
	static {
		try {
			File dir = new File("logs");
			if(!dir.exists())
				dir.mkdir();
			File file = new File(dir, name + "-latest.log");
			File lock = new File(dir, name + "-latest.log.lck");
			File file1 = new File(dir, name + "-1.log");
			File file2 = new File(dir, name + "-2.log");
			File file3 = new File(dir, name + "-3.log");

			if(lock.exists())
				lock.delete();

			if(file3.exists())
				file3.delete();
			if(file2.exists())
				file2.renameTo(file3);
			if(file1.exists())
				file1.renameTo(file2);
			if(file.exists())
				file.renameTo(file1);

			handler = new StreamHandler(new FileOutputStream(file), new Formatter() {
				@Override
				public String format(LogRecord record) {
					String line = "";
					if (record.getLevel() != NPCLogLevel.CNPCLog){
						StackTraceElement element = Thread.currentThread().getStackTrace()[8];
						line = "[" + element.getClassName() + ":" + element.getLineNumber() + "] ";
					}
					String time = "[" + dateformat.format(new Date(record.getMillis())) + "][" + record.getLevel() + "]" + line;
					if(record.getThrown() != null){
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						record.getThrown().printStackTrace(pw);
						return time + sw.toString();
					}
					return time + record.getMessage() + System.getProperty("line.separator");
				}
			});
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(handler.getFormatter());
			consoleHandler.setLevel(Level.ALL);
			logger.addHandler(consoleHandler);
			logger.setLevel(Level.ALL);
			info(new Date().toString());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void postScriptLog(UUID npcUUID, EnumScriptType type, String message) {
		switch (type){
			case INIT:
				if(!CustomNpcs.InitIgnore){
					scriptLogCalculator(InitCacheMap, npcUUID, message);
				}
				break;
			case TICK:
				if(!CustomNpcs.TickIgnore){
					scriptLogCalculator(TickCacheMap, npcUUID, message);
				}
				break;
			case INTERACT:
				if(!CustomNpcs.InteractIgnore){
					scriptLogCalculator(InteractCacheMap, npcUUID, message);
				}
				break;
			case DIALOG:
				if(!CustomNpcs.DialogIgnore){
					scriptLogCalculator(DialogCacheMap, npcUUID, message);
				}
				break;
			case DAMAGED:
				if(!CustomNpcs.DamagedIgnore){
					scriptLogCalculator(DamagedCacheMap, npcUUID, message);
				}
				break;
			case KILLED:
				if(!CustomNpcs.KilledIgnore){
					scriptLogCalculator(KilledCacheMap, npcUUID, message);
				}
				break;
			case ATTACK:
				if(!CustomNpcs.AttackIgnore){
					scriptLogCalculator(AttackCacheMap, npcUUID, message);
				}
				break;
			case TARGET:
				if(!CustomNpcs.TargetIgnore){
					scriptLogCalculator(TargetCacheMap, npcUUID, message);
				}
				break;
			case COLLIDE:
				if(!CustomNpcs.CollideIgnore){
					scriptLogCalculator(CollideCacheMap, npcUUID, message);
				}
				break;
			case KILLS:
				if(!CustomNpcs.KillsIgnore){
					scriptLogCalculator(KillsCacheMap, npcUUID, message);
				}
			case DIALOG_CLOSE:
				if(!CustomNpcs.DialogCloseIgnore){
					scriptLogCalculator(DialogCloseCacheMap, npcUUID, message);
				}
				break;
			case TIMER:
				if(!CustomNpcs.TimerIgnore){
					scriptLogCalculator(TimerCacheMap, npcUUID, message);
				}
				break;
			default:
				break;
		}
	}

	public static void scriptLogCalculator(WeakHashMap<UUID, NPCStamp> map, UUID npcUUID, String message) {
		if(map.containsKey(npcUUID)){
			NPCStamp stamp = map.get(npcUUID);
			long secondsSinceFirst = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - stamp.makeDate.getTime());
			long millisecSinceLast = TimeUnit.MILLISECONDS.toMillis(stamp.recentDate.getTime() - stamp.makeDate.getTime());

			double frequency = (float)CustomNpcs.ScriptFrequency / 60;
			// Reset Log if 2 Minutes Pass
			if (secondsSinceFirst > 120) {
				LogWriter.script(message);
				stamp.counter = 1;
				stamp.makeDate = new Date();
				stamp.recentDate = new Date();
			}
			// ALWAYS Log the First 3 of Event
			else if(secondsSinceFirst < 10 && stamp.counter < 3) {
				LogWriter.script(message);
				stamp.recentDate = new Date();
			}
			// IF event occurs to QUICKLY Ignore.
			else if (millisecSinceLast < CustomNpcs.ScriptIgnoreTime){
				stamp.recentDate = new Date();
			}
			else if(secondsSinceFirst > 10 && (double)stamp.counter / secondsSinceFirst > frequency){
				// WARN
				LogWriter.script("[SPAM]:" + message);
				stamp.counter = 0;
				stamp.recentDate = new Date();
			}
			stamp.counter++;
		} else {
			map.put(npcUUID, new NPCStamp());
			LogWriter.script(message);
		}
	}

	public static void script(Object msg) {
		logger.log(NPCLogLevel.CNPCLog, msg.toString());
		handler.flush();
	}

	public static void info(Object msg) {
		logger.log(Level.INFO, msg.toString());
		handler.flush();
	}

	public static void error(Object msg) {
		logger.log(Level.SEVERE, msg.toString());
		handler.flush();
	}

	public static void error(Object msg, Exception e) {
		logger.log(Level.SEVERE, msg.toString());
		logger.log(Level.SEVERE, e.getMessage(), e);
		handler.flush();
	}

	public static void except(Exception e) {
		logger.log(Level.SEVERE, e.getMessage(), e);
		handler.flush();
	}
}

class NPCStamp
{
	public Date makeDate = new Date();
	public Date recentDate = new Date();
	public int counter = 1;
};