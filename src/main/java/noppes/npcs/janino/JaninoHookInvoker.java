package noppes.npcs.janino;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

final class JaninoHookInvoker {
    private static final MethodType HOOK_INVOKER_TYPE = MethodType.methodType(Object.class, Object.class, Object[].class);
    private static final MethodType HOOK_INVOKER_VOID_TYPE = MethodType.methodType(void.class, Object.class, Object[].class);

    private final Map<Method, HookInvokerEntry> invokerCache = new HashMap<>();
    private Class<?> lastInvokerClass;

    HookInvokerEntry getOrCreate(Method method, Class<?> compiledClass, JaninoScript<?> owner) {
        if (compiledClass != lastInvokerClass) {
            invokerCache.clear();
            lastInvokerClass = compiledClass;
        }

        HookInvokerEntry cached = invokerCache.get(method);
        if (cached != null)
            return cached;

        try {
            MethodHandle handle = unreflectMethod(method);
            int paramCount = method.getParameterTypes().length;
            boolean isStatic = Modifier.isStatic(method.getModifiers());

            MethodHandle spreader;
            if (isStatic) {
                spreader = handle.asSpreader(Object[].class, paramCount);
                spreader = MethodHandles.dropArguments(spreader, 0, Object.class);
            } else {
                MethodType type = handle.type();
                if (type.parameterType(0) != Object.class)
                    handle = handle.asType(type.changeParameterType(0, Object.class));

                spreader = handle.asSpreader(Object[].class, paramCount);
            }

            HookInvokerEntry entry = new HookInvokerEntry();
            if (method.getReturnType() == void.class) {
                MethodHandle target = spreader.asType(HOOK_INVOKER_VOID_TYPE);
                CallSite site = LambdaMetafactory.metafactory(
                    MethodHandles.lookup(),
                    "invoke",
                    MethodType.methodType(HookInvokerVoid.class),
                    HOOK_INVOKER_VOID_TYPE,
                    target,
                    HOOK_INVOKER_VOID_TYPE
                );
                entry.voidInvoker = (HookInvokerVoid) site.getTarget().invokeExact();
                entry.returnsVoid = true;
            } else {
                MethodHandle target = spreader.asType(HOOK_INVOKER_TYPE);
                CallSite site = LambdaMetafactory.metafactory(
                    MethodHandles.lookup(),
                    "invoke",
                    MethodType.methodType(HookInvoker.class),
                    HOOK_INVOKER_TYPE,
                    target,
                    HOOK_INVOKER_TYPE
                );
                entry.invoker = (HookInvoker) site.getTarget().invokeExact();
            }

            invokerCache.put(method, entry);
            return entry;
        } catch (Throwable e) {
            owner.appendConsole("Error binding hook " + method.getName() + ": " + e.getMessage());
            return null;
        }
    }

    void clear() {
        invokerCache.clear();
        lastInvokerClass = null;
    }

    private static MethodHandle unreflectMethod(Method method) throws IllegalAccessException {
        try {
            return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        }
    }

    interface HookInvoker {
        Object invoke(Object target, Object[] args) throws Throwable;
    }

    interface HookInvokerVoid {
        void invoke(Object target, Object[] args) throws Throwable;
    }

    static final class HookInvokerEntry {
        HookInvoker invoker;
        HookInvokerVoid voidInvoker;
        boolean returnsVoid;
    }
}
