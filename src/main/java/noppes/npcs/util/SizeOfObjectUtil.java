package noppes.npcs.util;

import java.lang.instrument.Instrumentation;

public class SizeOfObjectUtil {
    private static volatile Instrumentation instrumentation;

    public static void initializeInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    public static long sizeOfObject(Object obj) {
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation is not initialized");
        }
        return instrumentation.getObjectSize(obj);
    }
}
