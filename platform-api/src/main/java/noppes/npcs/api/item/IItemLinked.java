package noppes.npcs.api.item;

import noppes.npcs.api.handler.data.ILinkedItem;

public interface IItemLinked extends IItemCustomizable {

    public ILinkedItem getLinkedItem();

    /**
     * Sets the current durability value for this linked item stack.
     * This is a per-stack override, not changing the linked item template.
     *
     * @param durabilityValue The durability value
     */
    void setDurabilityValue(float durabilityValue);

}
