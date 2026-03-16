package kamkeel.npcs.controllers.data.ability.conditions;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import java.util.List;

public class ConditionItem extends AbilityCondition {

    public enum UsageType {
        ARMOR, HOLDING, COUNT, OFFHAND;

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

    // COUNT + PLAYER only
    private int requiredCount = 1;

    public ConditionItem() {
        this.typeId = "condition.cnpc.item";
        this.name = "condition.item";
    }

    @Override
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        switch (usageType) {
            case HOLDING:
                return checkHolding(IEntity);
            case ARMOR:
                return checkArmor(IEntity);
            case COUNT:
                return checkCount(IEntity);
            case OFFHAND:
                return checkOffhand(IEntity);
            default:
                return false;
        }
    }

    private boolean checkHolding(IEntityLivingBase IEntity) {
        IItemStack held = IEntity.getHeldItem();
        return matchesItem(held);
    }

    private boolean checkArmor(IEntityLivingBase IEntity) {
        // MC getEquipmentInSlot: 1=boots, 2=legs, 3=chest, 4=helmet
        int slot = armorSlot.ordinal() + 1; // BOOTS=0, LEGS=1, CHEST=2, HELMET=3
        return matchesItem(IEntity.getEquipmentInSlot(slot));
    }

    private boolean checkCount(IEntityLivingBase IEntity) {
        if (IEntity instanceof IPlayer) {
            return checkPlayerInventoryCount((IPlayer) IEntity);
        }
        if (IEntity instanceof EntityNPCInterface) {
            return checkNPCProjectile((EntityNPCInterface) IEntity);
        }

        return false;
    }

    private boolean checkOffhand(IEntityLivingBase IEntity) {
        // No offhand for players
        if (IEntity instanceof IPlayer) return false;
        if (!(IEntity instanceof EntityNPCInterface)) return false;

        IItemStack item = ((EntityNPCInterface) IEntity).getOffHand();
        return matchesItem(item);
    }

    private boolean checkPlayerInventoryCount(IPlayer player) {
        int count = 0;
        for (IItemStack stack : player.inventory.mainInventory) {
            if (matchesItem(stack)) {
                count += stack.stackSize;
            }
        }
        return count >= requiredCount;
    }

    private boolean checkNPCProjectile(EntityNPCInterface npc) {
        IItemStack projectile = npc.inventory.getProjectile();
        return matchesItem(projectile);
    }

    private boolean matchesItem(IItemStack stack) {
        if (stack == null || itemName == null || itemName.isEmpty()) return false;
        String registryName = net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem());
        return itemName.equals(registryName);
    }

    @ClientOnly
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("condition.usage_type", UsageType.class,
            this::getUsageType, this::setUsageType));

        defs.add(FieldDef.stringField("condition.item_name",
                this::getItemName, this::setItemName)
            .hover("condition.hover.item_name"));

        defs.add(FieldDef.enumField("condition.armor_slot", ArmorSlot.class,
                this::getArmorSlot, this::setArmorSlot)
            .visibleWhen(() -> usageType == UsageType.ARMOR)
            .hover("condition.hover.armor_slots"));

        defs.add(FieldDef.intField("condition.item_count",
                this::getRequiredCount, this::setRequiredCount)
            .range(1, 64)
            .visibleWhen(() -> usageType == UsageType.COUNT && userType.allowsPlayer())
            .hover("condition.hover.required_count"));
    }

    @ClientOnly
    @Override
    public String getConditionSummary() {
        String filterLabel = PlatformServiceHolder.get().translateToLocal(getFilter().toString());
        String usage = usageType.name();
        String item = itemName.isEmpty() ? "None" : itemName;
        return "[" + filterLabel + "] " + usage + ": " + item;
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        nbt.setString("itemName", itemName);
        nbt.setInteger("usageType", usageType.ordinal());
        nbt.setInteger("armorSlot", armorSlot.ordinal());
        nbt.setInteger("requiredCount", requiredCount);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
        itemName = nbt.getString("itemName");
        usageType = UsageType.fromOrdinal(nbt.getInteger("usageType"));
        armorSlot = ArmorSlot.fromOrdinal(nbt.getInteger("armorSlot"));
        requiredCount = Math.max(1, nbt.getInteger("requiredCount"));
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
