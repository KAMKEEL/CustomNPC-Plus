package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Interface for ability usage conditions.
 * Conditions determine when an ability is eligible to be used.
 */
public interface Condition {

    /**
     * Check if this condition is met.
     *
     * @param npc The NPC that would use the ability
     * @param target The current target (may be null for self-targeting abilities)
     * @return true if the condition is satisfied
     */
    boolean check(EntityNPCInterface npc, EntityLivingBase target);

    /**
     * Get the condition type ID for serialization.
     */
    String getTypeId();

    /**
     * Write this condition to NBT.
     */
    NBTTagCompound writeNBT();

    /**
     * Read this condition from NBT.
     */
    void readNBT(NBTTagCompound nbt);

    /**
     * Create a Condition from NBT data.
     * Uses the "type" field to determine which implementation to create.
     */
    static Condition fromNBT(NBTTagCompound nbt) {
        if (nbt == null || !nbt.hasKey("type")) {
            return null;
        }

        String type = nbt.getString("type");
        Condition condition = null;

        switch (type) {
            case "hp_above":
                condition = new ConditionHPAbove();
                break;
            case "hp_below":
                condition = new ConditionHPBelow();
                break;
            case "target_hp_above":
                condition = new ConditionTargetHPAbove();
                break;
            case "target_hp_below":
                condition = new ConditionTargetHPBelow();
                break;
            case "hit_count":
                condition = new ConditionHitCount();
                break;
            default:
                return null;
        }

        condition.readNBT(nbt);
        return condition;
    }

    // ═══════════════════════════════════════════════════════════════════
    // BUILT-IN CONDITION IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Condition: NPC HP is above a percentage threshold.
     */
    class ConditionHPAbove implements Condition {
        private float threshold = 0.5f; // 50%

        public ConditionHPAbove() {}

        public ConditionHPAbove(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean check(EntityNPCInterface npc, EntityLivingBase target) {
            return npc.getHealth() / npc.getMaxHealth() > threshold;
        }

        @Override
        public String getTypeId() { return "hp_above"; }

        @Override
        public NBTTagCompound writeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", getTypeId());
            nbt.setFloat("threshold", threshold);
            return nbt;
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            threshold = nbt.getFloat("threshold");
        }
    }

    /**
     * Condition: NPC HP is below a percentage threshold.
     */
    class ConditionHPBelow implements Condition {
        private float threshold = 0.5f;

        public ConditionHPBelow() {}

        public ConditionHPBelow(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean check(EntityNPCInterface npc, EntityLivingBase target) {
            return npc.getHealth() / npc.getMaxHealth() < threshold;
        }

        @Override
        public String getTypeId() { return "hp_below"; }

        @Override
        public NBTTagCompound writeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", getTypeId());
            nbt.setFloat("threshold", threshold);
            return nbt;
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            threshold = nbt.getFloat("threshold");
        }
    }

    /**
     * Condition: Target HP is above a percentage threshold.
     */
    class ConditionTargetHPAbove implements Condition {
        private float threshold = 0.5f;

        public ConditionTargetHPAbove() {}

        public ConditionTargetHPAbove(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean check(EntityNPCInterface npc, EntityLivingBase target) {
            if (target == null) return false;
            return target.getHealth() / target.getMaxHealth() > threshold;
        }

        @Override
        public String getTypeId() { return "target_hp_above"; }

        @Override
        public NBTTagCompound writeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", getTypeId());
            nbt.setFloat("threshold", threshold);
            return nbt;
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            threshold = nbt.getFloat("threshold");
        }
    }

    /**
     * Condition: Target HP is below a percentage threshold.
     */
    class ConditionTargetHPBelow implements Condition {
        private float threshold = 0.5f;

        public ConditionTargetHPBelow() {}

        public ConditionTargetHPBelow(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean check(EntityNPCInterface npc, EntityLivingBase target) {
            if (target == null) return false;
            return target.getHealth() / target.getMaxHealth() < threshold;
        }

        @Override
        public String getTypeId() { return "target_hp_below"; }

        @Override
        public NBTTagCompound writeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", getTypeId());
            nbt.setFloat("threshold", threshold);
            return nbt;
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            threshold = nbt.getFloat("threshold");
        }
    }

    /**
     * Condition: NPC has been hit X times within a time window.
     * Useful for reactive abilities that trigger after receiving multiple hits.
     */
    class ConditionHitCount implements Condition {
        private int requiredHits = 3;
        private int withinTicks = 60;

        public ConditionHitCount() {}

        public ConditionHitCount(int requiredHits, int withinTicks) {
            this.requiredHits = requiredHits;
            this.withinTicks = withinTicks;
        }

        @Override
        public boolean check(EntityNPCInterface npc, EntityLivingBase target) {
            // Check NPC's recent hit count from DataAbilities
            if (npc.abilities != null) {
                return npc.abilities.getRecentHitCount(withinTicks) >= requiredHits;
            }
            return false;
        }

        @Override
        public String getTypeId() { return "hit_count"; }

        @Override
        public NBTTagCompound writeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", getTypeId());
            nbt.setInteger("requiredHits", requiredHits);
            nbt.setInteger("withinTicks", withinTicks);
            return nbt;
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            requiredHits = nbt.getInteger("requiredHits");
            withinTicks = nbt.getInteger("withinTicks");
        }

        public int getRequiredHits() { return requiredHits; }
        public void setRequiredHits(int hits) { this.requiredHits = hits; }

        public int getWithinTicks() { return withinTicks; }
        public void setWithinTicks(int ticks) { this.withinTicks = ticks; }
    }
}
