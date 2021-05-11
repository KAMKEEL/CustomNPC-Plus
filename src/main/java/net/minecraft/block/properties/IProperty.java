package net.minecraft.block.properties;

import java.util.Collection;

/** This code is owned by Minecraft */

public interface IProperty<T extends Comparable<T>>
{
    String getName();

    Collection<T> getAllowedValues();

    Class<T> getValueClass();

    /**
     * Get the name for the given value.
     */
    String getName(T value);
}