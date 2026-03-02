package io.github.frostzie.nodex.utils

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object JavaFXInitializer {
    private val logger = LoggerProvider.getLogger("JavaFXInitializer")
    private var isJavaFXAvailable: Boolean? = null
    private var platformClass: Class<*>? = null
    private var startupMethod: Method? = null
    private var runLaterMethod: Method? = null
    private var setImplicitExitMethod: Method? = null

    fun isJavaFXAvailable(): Boolean {
        if (isJavaFXAvailable != null) return isJavaFXAvailable!!

        try {
            val classLoaders = listOf(
                Thread.currentThread().contextClassLoader,
                JavaFXInitializer::class.java.classLoader,
                ClassLoader.getSystemClassLoader()
            )

            var lastException: Exception? = null

            for (classLoader in classLoaders) {
                try {
                    platformClass = Class.forName("javafx.application.Platform", true, classLoader)
                    startupMethod = platformClass?.getMethod("startup", Runnable::class.java)
                    runLaterMethod = platformClass?.getMethod("runLater", Runnable::class.java)
                    setImplicitExitMethod = platformClass?.getMethod("setImplicitExit", Boolean::class.javaPrimitiveType)

                    isJavaFXAvailable = true
                    logger.info("JavaFX is available and loaded successfully using classloader: ${classLoader.javaClass.simpleName}")
                    return true
                } catch (e: ClassNotFoundException) {
                    lastException = e
                    logger.debug("JavaFX not found with classloader: ${classLoader.javaClass.simpleName}")
                    continue
                }
            }

            logger.error("JavaFX is not available on the classpath with any classloader", lastException)
            isJavaFXAvailable = false
            return false
        } catch (e: Exception) {
            logger.error("Error checking JavaFX availability", e)
            isJavaFXAvailable = false
            return false
        }
    }

    fun startup(runnable: Runnable) {
        if (!isJavaFXAvailable()) {
            logger.error("Cannot start JavaFX - not available")
            return
        }

        try {
            startupMethod?.invoke(null, runnable)
        } catch (e: Exception) {
            val isAlreadyInitialized = (e is InvocationTargetException && e.cause is IllegalStateException) || (e is IllegalStateException)

            if (isAlreadyInitialized) {
                logger.info("JavaFX Platform already initialized")
                runLater(runnable)
            } else {
                logger.error("Error starting JavaFX platform", e)
            }
        }
    }

    fun runLater(runnable: Runnable) {
        if (!isJavaFXAvailable()) {
            logger.error("Cannot run JavaFX task - not available")
            return
        }

        try {
            runLaterMethod?.invoke(null, runnable)
        } catch (e: Exception) {
            logger.error("Error running JavaFX task", e)
        }
    }

    fun setImplicitExit(implicitExit: Boolean) {
        if (!isJavaFXAvailable()) return

        try {
            setImplicitExitMethod?.invoke(null, implicitExit)
        } catch (e: Exception) {
            logger.error("Error setting JavaFX implicit exit", e)
        }
    }
}