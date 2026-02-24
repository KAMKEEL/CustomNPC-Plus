package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

public class ConditionItem extends AbilityCondition {

    public enum UsageType {
        ARMOR, HOLDING, AMMO, OFFHAND;

        public static UsageType fromOrdinal(int ordinal) {
            UsageType[] values = values();
            return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : HOLDING;
        }
    }

    public enum ArmorSlot {
        BOOTS, LEGS, CHEST, HELMET;

        public static ArmorSlot fromOrdinal(int ordinal) {
            ArmorSlot[] values = values();
            return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : BOOTS;
        }
    }

    // Shared
    private UsageType usageType = UsageType.HOLDING;
    private String itemName = "";

    // ARMOR only
    private ArmorSlot armorSlot = ArmorSlot.BOOTS;
    private boolean fullArmorSet = false;

    // AMMO + PLAYER only
    private int requiredCount = 1;

    public ConditionItem() {
        this.typeId = "condition.cnpc.item";
        this.name = "condition.item";
    }

    @Override
    protected boolean checkEntity(EntityLivingBase entity) {
        switch (usageType) {
            case HOLDING:
                return checkHolding(entity);
            case ARMOR:
                return checkArmor(entity);
            case AMMO:
                return checkAmmo(entity);
            case OFFHAND:
                return checkOffhand(entity);
            default:
                return false;
        }
    }

    private boolean checkHolding(EntityLivingBase entity) {
        ItemStack held = entity.getHeldItem();
        return matchesItem(held);
    }

    private boolean checkArmor(EntityLivingBase entity) {
        // MC getEquipmentInSlot: 1=boots, 2=legs, 3=chest, 4=helmet
        int slot = armorSlot.ordinal() + 1; // BOOTS=0, LEGS=1, CHEST=2, HELMET=3
        return matchesItem(entity.getEquipmentInSlot(slot));
    }

    private boolean checkFullArmor(EntityLivingBase entity) {
        for (int i = 0; i < 4; i++) {
            int slot = i + 1;
            if (!matchesItem(i, entity.getEquipmentInSlot(slot)))
                return false;
        }

        return true;
    }

    private boolean checkAmmo(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return checkPlayerInventoryCount((EntityPlayer) entity);
        }
        if (entity instanceof EntityNPCInterface) {
            return checkNPCProjectile((EntityNPCInterface) entity);
        }

        return false;
    }

    private boolean checkOffhand(EntityLivingBase entity) {
        // No offhand for players
        if (entity instanceof EntityPlayer) return false;
        if (!(entity instanceof EntityNPCInterface)) return false;

        ItemStack item = ((EntityNPCInterface) entity).getOffHand();
        return matchesItem(item);
    }

    private boolean checkPlayerInventoryCount(EntityPlayer player) {
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (matchesItem(stack)) {
                count += stack.stackSize;
            }
        }
        return count >= requiredCount;
    }

    private boolean checkNPCProjectile(EntityNPCInterface npc) {
        ItemStack projectile = npc.inventory.getProjectile();
        return matchesItem(projectile);
    }

    private boolean matchesItem(ItemStack stack) {
        return matchesItem(0, stack);
    }

    private boolean matchesItem(int index, ItemStack stack) {
        if (stack == null || itemName == null || itemName.isEmpty()) return false;
        String registryName = net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem());
        return itemName.equals(registryName);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("condition.usage_type", UsageType.class,
            this::getUsageType, this::setUsageType));

        defs.add(FieldDef.stringField("condition.item_name",
                this::getItemName, this::setItemName));
//            .hover("condition.hover.item_name"));



        defs.add(FieldDef.enumField("condition.armor_slot", ArmorSlot.class,
                this::getArmorSlot, this::setArmorSlot)
            .visibleWhen(() -> usageType == UsageType.ARMOR));
//            .hover("condition.hover.armor_slots"));

        defs.add(FieldDef.intField("condition.item_count",
                this::getRequiredCount, this::setRequiredCount)
            .range(1, 64)
            .visibleWhen(() -> usageType == UsageType.AMMO && userType.allowsPlayer()));
//            .hover("condition.hover.required_count"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getConditionSummary() {
        String filterLabel = StatCollector.translateToLocal(getFilter().toString());
        String usage = usageType.name();
        String item = itemName.isEmpty() ? "None" : itemName;
        return "[" + filterLabel + "] " + usage + ": " + item;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setString("itemName", itemName);
        nbt.setInteger("usageType", usageType.ordinal());
        nbt.setInteger("armorSlot", armorSlot.ordinal());
        nbt.setInteger("requiredCount", requiredCount);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        itemName = nbt.getString("itemName");
        usageType = UsageType.fromOrdinal(nbt.getInteger("usageType"));
        armorSlot = ArmorSlot.fromOrdinal(nbt.getInteger("armorSlot"));
        requiredCount = nbt.getInteger("requiredCount");
    }


    @Override
    public boolean isConfigured() {
        return itemName != null && !itemName.isEmpty();
    }

    public UsageType getUsageType() { return usageType; }
    public void setUsageType(UsageType usageType) { this.usageType = usageType; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public ArmorSlot getArmorSlot() { return armorSlot; }
    public void setArmorSlot(ArmorSlot armorSlot) { this.armorSlot = armorSlot; }

    public boolean isFullArmorSet() {
        return fullArmorSet;
    }

    public void setFullArmorSet(boolean fullArmorSet) {
        this.fullArmorSet = fullArmorSet;
    }

    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = Math.max(1, requiredCount); }
}
