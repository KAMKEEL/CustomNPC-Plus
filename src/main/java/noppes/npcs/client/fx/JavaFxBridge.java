package noppes.npcs.client.fx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import noppes.npcs.LogWriter;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Small bridge that starts JavaFX once and opens the Nodex-style IDE window.
 */
public final class JavaFxBridge {

    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static final AtomicBoolean failed = new AtomicBoolean(false);

    private JavaFxBridge() {
    }

    public static boolean isJavaFxAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean openScriptIde(File rootDir) {
        if (rootDir == null) {
            LogWriter.error("Cannot open JavaFX script IDE: script directory is null");
            return false;
        }

        return openScriptIde(rootDir.toPath());
    }

    public static boolean openScriptIde(Path rootPath) {
        return openScriptIde(rootPath, null);
    }

    public static boolean openScriptIde(File rootDir, NodexScriptBinding binding) {
        if (rootDir == null) {
            LogWriter.error("Cannot open JavaFX script IDE: script directory is null");
            return false;
        }
        return openScriptIde(rootDir.toPath(), binding);
    }

    public static boolean openScriptIde(Path rootPath, NodexScriptBinding binding) {
        if (rootPath == null) {
            LogWriter.error("Cannot open JavaFX script IDE: root path is null");
            return false;
        }

        if (!isJavaFxAvailable()) {
            LogWriter.error("JavaFX is not available in this Java runtime.");
            return false;
        }

        if (!ensureJavaFxStarted()) {
            return false;
        }

        Platform.runLater(() -> {
            try {
                NodexFxWindow.get().open(rootPath, binding);
            } catch (Throwable t) {
                LogWriter.error("Failed to open JavaFX Nodex window", asException(t));
            }
        });

        return true;
    }

    private static boolean ensureJavaFxStarted() {
        if (failed.get()) {
            return false;
        }

        if (started.compareAndSet(false, true)) {
            try {
                // JavaFX 8 bootstrap path.
                new JFXPanel();
                Platform.setImplicitExit(false);
            } catch (IllegalStateException alreadyStarted) {
                // JavaFX already running, continue.
            } catch (Throwable t) {
                failed.set(true);
                LogWriter.error("Failed to initialize JavaFX platform", asException(t));
                return false;
            }
        }

        return !failed.get();
    }

    private static Exception asException(Throwable t) {
        if (t instanceof Exception) {
            return (Exception) t;
        }
        return new Exception(t);
    }
}
