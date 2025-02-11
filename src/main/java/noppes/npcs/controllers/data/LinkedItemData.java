package noppes.npcs.controllers.data;

public class LinkedItemData {

    // Utilitzed within a Scripted Item
    //
    public int version = 1;

    public boolean durabilityShow = false;
    public double durabilityValue = 1.0D;
    public int durabilityColor = -1;
    public int itemColor = 0x8B4513;
    public int stackSize = 64;

    public int maxItemUseDuration = 20;
    public int itemUseAction = 0;

    public boolean isNormalItem = false;
    public boolean isTool = false;
    public int digSpeed = 1;
    public int armorType = -2; //-2: Fits in no armor slot,  -1: Fits in all slots, 0 - 4: Fits in Head -> Boots slot respectively
    public int enchantability;

    public String texture = "minecraft:textures/items/iron_pickaxe.png";

    public float translateX, translateY, translateZ;
    public float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
    public float rotationX, rotationY, rotationZ;
    public float rotationXRate, rotationYRate, rotationZRate;
}
