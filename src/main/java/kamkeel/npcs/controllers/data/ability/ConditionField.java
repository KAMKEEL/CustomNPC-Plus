package kamkeel.npcs.controllers.data.ability;

import net.minecraft.nbt.NBTTagCompound;

public abstract class ConditionField<T> {
    protected final String key;
    protected final String label;
    protected final Class<T> type; // Float.class, Integer.class, etc
    protected T value;

    public ConditionField(String key, String label, Class<T> type, T defaultValue) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public Class<T> getType() {
        return type;
    }

    public abstract void writeNBT(NBTTagCompound nbt);

    public abstract void readNBT(NBTTagCompound nbt);

    public static ConditionField<Integer> intField(String key, String label, int defaultValue) {
        return intField(key, label, defaultValue, 0, Integer.MAX_VALUE);
    }

    public static ConditionField<Integer> intField(String key, String label, int defaultValue, int min, int max) {
        return new NumberField<>(key, label, Integer.class, defaultValue, min, max);
    }

    public static ConditionField<Double> doubleField(String key, String label, double defaultValue) {
        return doubleField(key, label, defaultValue, 0, Double.MAX_VALUE);
    }

    public static ConditionField<Double> doubleField(String key, String label, double defaultValue, double min, double max) {
        return new NumberField<>(key, label, Double.class, defaultValue, min, max);
    }

    public static ConditionField<Float> floatField(String key, String label, float defaultValue) {
        return floatField(key, label, defaultValue, 0, Float.MAX_VALUE);
    }

    public static ConditionField<Float> floatField(String key, String label, float defaultValue, float min, float max) {
        return new NumberField<>(key, label, Float.class, defaultValue, min, max);
    }

    public static ConditionField<String> stringField(String key, String label) {
        return new StringField(key, label, "");
    }

    public static ConditionField<String> stringField(String key, String label, String defaultValue) {
        return new StringField(key, label, defaultValue);
    }

    public static ConditionField<Boolean> booleanField(String key, String label, boolean defaultValue) {
        return new BoolField(key, label, defaultValue);
    }

    public static <E extends Enum<E>> ConditionField<E> enumField(String key, String label, E defaultValue, Class<E> enumClass) {
        return new EnumField<>(key, label, defaultValue, enumClass);
    }

    public static <E extends Enum<E>> ConditionField<E> enumField(String key, String label, E defaultValue) {
        return new EnumField<>(key, label, defaultValue, defaultValue.getDeclaringClass());
    }

    private static class NumberField<T extends Number & Comparable<T>> extends ConditionField<T> {
        private T min;
        private T max;

        public NumberField(String key, String label, Class<T> type, T defaultValue, T min, T max) {
            super(key, label, type, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void setValue(T value) {
            if (value.compareTo(min) < 0) {
                this.value = min;
            } else if (value.compareTo(max) > 0) {
                this.value = max;
            } else {
                this.value = value;
            }
        }

        public NumberField<T> min(T value) {
            this.min = value;
            return this;
        }

        public NumberField<T> max(T value) {
            this.max = value;
            return this;
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            if (value instanceof Integer) nbt.setInteger(key, (Integer) value);
            else if (value instanceof Float) nbt.setFloat(key, (Float) value);
            else if (value instanceof Double) nbt.setDouble(key, (Double) value);
            else if (value instanceof Long) nbt.setLong(key, (Long) value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readNBT(NBTTagCompound nbt) {
            if (!nbt.hasKey(key)) return;
            T v;
            if (type == Integer.class) v = (T) (Integer) nbt.getInteger(key);
            else if (type == Float.class) v = (T) (Float) nbt.getFloat(key);
            else if (type == Double.class) v = (T) (Double) nbt.getDouble(key);
            else if (type == Long.class) v = (T) (Long) nbt.getLong(key);
            else return;
            setValue(v);
        }
    }

    private static class StringField extends ConditionField<String> {
        public StringField(String key, String label, String defaultValue) {
            super(key, label, String.class, defaultValue);
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setString(key, value);
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            setValue(nbt.getString(key));
        }
    }

    private static class BoolField extends ConditionField<Boolean> {
        public BoolField(String key, String label, boolean defaultValue) {
            super(key, label, Boolean.class, defaultValue);
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setBoolean(key, value);
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            setValue(nbt.getBoolean(key));
        }
    }

    private static class EnumField<E extends Enum<E>> extends ConditionField<E> {
        private final E[] options;

        public EnumField(String key, String label, E defaultValue, Class<E> enumClass) {
            super(key, label, enumClass, defaultValue);
            this.options = enumClass.getEnumConstants();
        }

        public E[] getOptions() {
            return options;
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setString(key, value.name());
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            if (!nbt.hasKey(key)) return;
            try {
                setValue(Enum.valueOf(type, nbt.getString(key)));
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
