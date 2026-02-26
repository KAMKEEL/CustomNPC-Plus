package kamkeel.npcs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Debug logging system for CNPC+ entities.
 * Writes to logs/cnpc_debug/[type]_[side].log (file only, no console output).
 * Toggle at runtime via /cnpcdebugger [type] (client) or /kam debug [type] (server).
 */
public class CNPCDebug {

    private static final String DIR = "logs/cnpc_debug";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    // Runtime toggle state per type, separate for client and server
    private static final Map<String, Boolean> serverToggles = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> clientToggles = new ConcurrentHashMap<>();

    // Per-type file handlers (lazily initialized)
    private static final Map<String, Handler> serverHandlers = new ConcurrentHashMap<>();
    private static final Map<String, Handler> clientHandlers = new ConcurrentHashMap<>();

    // ==================== TOGGLE API ====================

    /**
     * Toggle server-side debug for given type. Returns new state.
     */
    public static boolean toggleServer(String type) {
        type = type.toLowerCase();
        boolean newState = !isServerEnabled(type);
        serverToggles.put(type, newState);
        if (newState) {
            ensureHandler(type, false);
        }
        return newState;
    }

    /**
     * Toggle client-side debug for given type. Returns new state.
     */
    public static boolean toggleClient(String type) {
        type = type.toLowerCase();
        boolean newState = !isClientEnabled(type);
        clientToggles.put(type, newState);
        if (newState) {
            ensureHandler(type, true);
        }
        return newState;
    }

    public static boolean isServerEnabled(String type) {
        Boolean val = serverToggles.get(type.toLowerCase());
        return val != null && val;
    }

    public static boolean isClientEnabled(String type) {
        Boolean val = clientToggles.get(type.toLowerCase());
        return val != null && val;
    }

    // ==================== LOGGING API ====================

    /**
     * Log a debug message for the given type on the given side.
     * Callers should guard with isServerEnabled/isClientEnabled for performance.
     */
    public static void log(String type, boolean client, String msg) {
        type = type.toLowerCase();
        Map<String, Boolean> toggles = client ? clientToggles : serverToggles;
        Boolean enabled = toggles.get(type);
        if (enabled == null || !enabled) return;

        Handler handler = ensureHandler(type, client);
        if (handler == null) return;

        String side = client ? "CLIENT" : "SERVER";
        String timestamp;
        synchronized (DATE_FORMAT) {
            timestamp = DATE_FORMAT.format(new Date());
        }
        String formatted = "[" + timestamp + "][" + side + "][DEBUG-" + type.toUpperCase() + "] " + msg + System.lineSeparator();

        LogRecord record = new LogRecord(Level.INFO, formatted);
        handler.publish(record);
        handler.flush();
    }

    /**
     * Log on server side.
     */
    public static void logServer(String type, String msg) {
        log(type, false, msg);
    }

    /**
     * Log on client side.
     */
    public static void logClient(String type, String msg) {
        log(type, true, msg);
    }

    // ==================== INTERNALS ====================

    private static Handler ensureHandler(String type, boolean client) {
        Map<String, Handler> handlers = client ? clientHandlers : serverHandlers;
        Handler existing = handlers.get(type);
        if (existing != null) return existing;

        synchronized (CNPCDebug.class) {
            existing = handlers.get(type);
            if (existing != null) return existing;

            try {
                File dir = new File(DIR);
                if (!dir.exists()) dir.mkdirs();

                String side = client ? "client" : "server";
                File file = new File(dir, type + "_" + side + ".log");

                // Rotate old log
                if (file.exists()) {
                    File backup = new File(dir, type + "_" + side + "-prev.log");
                    if (backup.exists()) backup.delete();
                    file.renameTo(backup);
                }

                Handler handler = new StreamHandler(new FileOutputStream(file), new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return record.getMessage();
                    }
                });
                handler.setLevel(Level.ALL);
                handlers.put(type, handler);

                // Log header
                String timestamp;
                synchronized (DATE_FORMAT) {
                    timestamp = DATE_FORMAT.format(new Date());
                }
                LogRecord header = new LogRecord(Level.INFO,
                    "[" + timestamp + "][" + (client ? "CLIENT" : "SERVER") + "][DEBUG-" + type.toUpperCase() + "] Debug logging started" + System.lineSeparator());
                handler.publish(header);
                handler.flush();

                return handler;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
