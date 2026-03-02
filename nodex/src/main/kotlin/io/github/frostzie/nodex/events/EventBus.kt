package io.github.frostzie.nodex.events

import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.utils.LoggerProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

object EventBus {
    val logger = LoggerProvider.getLogger("EventBus")
    private val listeners = ConcurrentHashMap<Class<*>, CopyOnWriteArrayList<(Any) -> Unit>>()
    private val handlerMap = ConcurrentHashMap<Any, MutableList<Pair<Class<*>, (Any) -> Unit>>>()

    fun register(handler: Any) {
        if (handlerMap.containsKey(handler)) {
            logger.warn("Handler ${handler::class.simpleName} is already registered")
            return
        }

        val methods = handler::class.memberFunctions.filter {
            it.findAnnotation<SubscribeEvent>() != null
        }

        val registered = mutableListOf<Pair<Class<*>, (Any) -> Unit>>()

        for (method in methods) {
            val parameters = method.parameters
            if (parameters.size != 2) continue

            val eventType = parameters[1].type.classifier as? KClass<*> ?: continue
            val eventClass = eventType.java

            method.isAccessible = true
            val fn: (Any) -> Unit = { event -> method.call(handler, event) }

            listeners.getOrPut(eventClass) { CopyOnWriteArrayList() }.add(fn)
            registered += eventClass to fn

            logger.debug("Registered listener ${method.name} for event ${eventClass.simpleName}")
        }

        handlerMap[handler] = registered
        logger.debug("Registered handler ${handler::class.simpleName} with ${registered.size} subscribers")
    }

    fun unregister(handler: Any) {
        val registered = handlerMap.remove(handler) ?: return
        for ((eventClass, fn) in registered) {
            listeners[eventClass]?.remove(fn)
            if (listeners[eventClass]?.isEmpty() == true) listeners.remove(eventClass)
        }
        logger.debug("Unregistered handler ${handler::class.simpleName}")
    }

    fun post(event: Any) {
        listeners[event::class.java]?.forEach { l ->
            try {
                l(event)
            } catch (t: Throwable) {
                logger.error("Error delivering ${event::class.simpleName} to a subscriber", t)
            }
        }
        logger.debug("Posted Event: ${event::class.simpleName}")
    }

    fun clear() {
        listeners.clear()
        handlerMap.clear()
        logger.debug("Cleared all event listeners")
    }

    /**
     * Call when the component is no longer needed
     */
    fun cleanup() {
        clear()
    }
}