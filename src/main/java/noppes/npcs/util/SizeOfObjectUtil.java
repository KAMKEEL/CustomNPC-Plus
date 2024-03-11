package noppes.npcs.util;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public class SizeOfObjectUtil {
    public static long sizeOfObject(Object obj) {
        long size = 0;
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (type == int.class || type == Integer.class) {
                    size += Integer.BYTES;
                } else if (type == long.class || type == Long.class) {
                    size += Long.BYTES;
                } else if (type == byte.class || type == Byte.class) {
                    size += Byte.BYTES;
                } else if (type == boolean.class || type == Boolean.class) {
                    size += 1; // Assuming 1 byte for boolean
                } // Add more cases for other primitive types if needed
                else {
                    // If it's a reference type, recursively calculate its size
                    Object value = field.get(obj);
                    if (value != null) {
                        size += sizeOfObject(value);
                    }
                }
            } catch (IllegalAccessException e) {
                // Handle exception
            }
        }
        return size;
    }
}
