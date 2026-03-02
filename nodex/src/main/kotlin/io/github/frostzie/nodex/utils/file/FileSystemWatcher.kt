package io.github.frostzie.nodex.utils.file

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.FileDeleted
import io.github.frostzie.nodex.events.FileModified
import io.github.frostzie.nodex.events.FileMoved
import io.github.frostzie.nodex.utils.LoggerProvider
import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class FileSystemUpdate(val path: Path)

class FileSystemWatcher(private val watchPath: Path) {
    private val logger = LoggerProvider.getLogger("FileSystemWatcher")
    private var watcher: DirectoryWatcher? = null
    private var watchThread: Thread? = null
    private var debounceExecutor: ScheduledExecutorService? = null
    private var scheduledUpdate: ScheduledFuture<*>? = null
    private val debounceDelayMs = 500L
    @Volatile
    private var isWindowFocused = true
    @Volatile
    private var pendingUpdate = false
    private val pendingEvents = ConcurrentLinkedQueue<Any>()

    private val ignoredPaths = ConcurrentHashMap<Path, Long>()
    private val ignoreDurationMs = 2000L
    private val pendingDeletes = ConcurrentHashMap<Path, ScheduledFuture<*>>()
    private val renameDelayMs = 150L

    fun start() {
        stop()

        try {
            debounceExecutor = Executors.newSingleThreadScheduledExecutor()

            watcher = DirectoryWatcher.builder()
                .path(watchPath)
                .listener { event ->
                    if (shouldIgnore(event.path())) {
                        logger.debug("Ignoring internal change for: {}", event.path())
                        return@listener
                    }

                    when (event.eventType()) {
                        DirectoryChangeEvent.EventType.MODIFY -> {
                            postOrQueue(FileModified(event.path()))
                        }
                        DirectoryChangeEvent.EventType.DELETE -> {
                            val future = debounceExecutor?.schedule({
                                pendingDeletes.remove(event.path())
                                postOrQueue(FileDeleted(event.path()))
                            }, renameDelayMs, TimeUnit.MILLISECONDS)
                            if (future != null) {
                                pendingDeletes[event.path()] = future
                            }
                        }
                        DirectoryChangeEvent.EventType.CREATE -> {
                            // Try to match with a pending delete (rename/move)
                            // Prefer matching filename (move), otherwise take any (rename)
                            val oldPath = pendingDeletes.keys.find { it.fileName == event.path().fileName }
                                ?: pendingDeletes.keys.firstOrNull()

                            if (oldPath != null) {
                                val future = pendingDeletes.remove(oldPath)
                                future?.cancel(false)
                                postOrQueue(FileMoved(oldPath, event.path()))
                            }
                        }
                        else -> {}
                    }

                    logger.debug("File system event: {} - {}", event.eventType(), event.path())
                    scheduleUpdate()
                }
                .build()

            watchThread = thread(name = "FileSystemWatcher") {
                logger.debug("Started watching directory: {}", watchPath)
                try {
                    watcher?.watch()
                } catch (e: Exception) {
                    // DirectoryWatcher throws on close, so we check if we are stopping
                    if (watcher != null) {
                        logger.error("Watcher terminated unexpectedly", e)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to start file system watcher", e)
        }
    }

    fun stop() {
        watchThread?.interrupt()
        watchThread?.join(5000) // Wait up to 5 seconds for thread to terminate
        watchThread = null
        scheduledUpdate?.cancel(false)
        debounceExecutor?.shutdown()
        try {
            if (debounceExecutor?.awaitTermination(5, TimeUnit.SECONDS) == false) {
                debounceExecutor?.shutdownNow()
            }
        } catch (ie: InterruptedException) {
            debounceExecutor?.shutdownNow()
            Thread.currentThread().interrupt()
        }

        try {
            watcher?.close()
        } catch (e: Exception) {
            logger.warn("Failed to close watcher cleanly", e)
        }
        watcher = null
        logger.info("Stopped file system watcher")
    }

    fun setWindowFocused(focused: Boolean) {
        isWindowFocused = focused
        if (focused) {
            while (!pendingEvents.isEmpty()) {
                EventBus.post(pendingEvents.poll())
            }

            if (pendingUpdate) {
                try {
                    triggerUpdate()
                } finally {
                    pendingUpdate = false
                }
            }
        }
    }

    fun ignoreChanges(path: Path) {
        ignoredPaths[path] = System.currentTimeMillis()
    }

    private fun shouldIgnore(path: Path): Boolean {
        val now = System.currentTimeMillis()
        val iterator = ignoredPaths.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > ignoreDurationMs) {
                iterator.remove()
                continue
            }
            if (path.startsWith(entry.key)) {
                return true
            }
        }
        return false
    }

    private fun scheduleUpdate() {
        scheduledUpdate?.cancel(false)
        scheduledUpdate = debounceExecutor?.schedule({
            if (isWindowFocused) {
                triggerUpdate()
            } else {
                pendingUpdate = true
            }
        }, debounceDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun postOrQueue(event: Any) {
        if (isWindowFocused) {
            EventBus.post(event)
        } else {
            pendingEvents.add(event)
        }
    }

    private fun triggerUpdate() {
        logger.info("Triggering directory refresh due to file system changes")
        EventBus.post(FileSystemUpdate(watchPath))
    }
}