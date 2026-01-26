package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityHeal;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Heal ability: Restore health to self or allies.
 * Can heal a fixed amount or percentage of max health.
 */
public class AbilityHeal extends Ability {

    // Type-specific parameters
    private float healAmount = 10.0f;
    private float healPercent = 0.0f;
    private boolean healSelf = true;
    private boolean healAllies = false;
    private float healRadius = 0.0f;
    private boolean instantHeal = true;

    // Runtime state
    private transient List<EntityLivingBase> healedAllies = new ArrayList<>();

    public AbilityHeal() {
        this.typeId = "ability.cnpc.heal";
        this.name = "Heal";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = true;
        this.cooldownTicks = 200;
        this.windUpTicks = 30;
        this.activeTicks = 10;
        this.recoveryTicks = 10;
        // No telegraph for heal - it's a self/ally buff
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityHeal(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.SELF};
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) return;

        healedAllies.clear();

        // Always find allies if we're healing them (needed for both instant and HoT)
        if (healAllies && healRadius > 0) {
            findAlliesInRadius(npc, world);
        }

        if (instantHeal) {
            // Instant heal - apply all healing now
            if (healSelf) {
                healEntity(npc);
                spawnHealParticles(world, npc);
            }

            for (EntityLivingBase ally : healedAllies) {
                healEntity(ally);
                spawnHealParticles(world, ally);
            }
        }
        // If not instant, HoT healing is applied in onActiveTick
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote || instantHeal) return;

        // Heal over time - distribute heal across active ticks
        if (tick % 10 == 0) {
            // Calculate tick-based heal for fixed amount
            float tickHeal = (healAmount / (float) activeTicks) * 10;

            if (healSelf) {
                float selfTickHeal = tickHeal;
                // Add percentage-based heal portion
                if (healPercent > 0) {
                    selfTickHeal += (npc.getMaxHealth() * healPercent / (float) activeTicks) * 10;
                }
                npc.heal(selfTickHeal);
                if (tick % 20 == 0) {
                    spawnHealParticles(world, npc);
                }
            }

            if (healAllies && healRadius > 0) {
                for (EntityLivingBase ally : healedAllies) {
                    if (!ally.isDead) {
                        float allyTickHeal = tickHeal;
                        // Add percentage-based heal portion for each ally
                        if (healPercent > 0) {
                            allyTickHeal += (ally.getMaxHealth() * healPercent / (float) activeTicks) * 10;
                        }
                        ally.heal(allyTickHeal);
                        if (tick % 20 == 0) {
                            spawnHealParticles(world, ally);
                        }
                    }
                }
            }
        }
    }

    /**
     * Find all allies in radius and add them to healedAllies list.
     */
    private void findAlliesInRadius(EntityNPCInterface npc, World world) {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            npc.posX - healRadius, npc.posY - 2, npc.posZ - healRadius,
            npc.posX + healRadius, npc.posY + 3, npc.posZ + healRadius
        );

        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            if (entity == npc) continue;

            EntityLivingBase living = (EntityLivingBase) entity;

            // Only heal other NPCs (allies)
            if (living instanceof EntityNPCInterface) {
                float dist = npc.getDistanceToEntity(living);
                if (dist <= healRadius) {
                    healedAllies.add(living);
                }
            }
        }
    }

    private void healEntity(EntityLivingBase entity) {
        float totalHeal = healAmount;
        if (healPercent > 0) {
            totalHeal += entity.getMaxHealth() * healPercent;
        }
        entity.heal(totalHeal);
    }

    private void spawnHealParticles(World world, EntityLivingBase entity) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (world.rand.nextDouble() - 0.5) * entity.width;
            double offsetY = world.rand.nextDouble() * entity.height;
            double offsetZ = (world.rand.nextDouble() - 0.5) * entity.width;
            world.spawnParticle("happyVillager",
                entity.posX + offsetX,
                entity.posY + offsetY,
                entity.posZ + offsetZ,
                0, 0.1, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        healedAllies.clear();
    }

    @Override
    public float getTelegraphRadius() {
        return healRadius > 0 ? healRadius : 0;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("healAmount", healAmount);
        nbt.setFloat("healPercent", healPercent);
        nbt.setBoolean("healSelf", healSelf);
        nbt.setBoolean("healAllies", healAllies);
        nbt.setFloat("healRadius", healRadius);
        nbt.setBoolean("instantHeal", instantHeal);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.healAmount = nbt.hasKey("healAmount") ? nbt.getFloat("healAmount") : 10.0f;
        this.healPercent = nbt.hasKey("healPercent") ? nbt.getFloat("healPercent") : 0.0f;
        this.healSelf = !nbt.hasKey("healSelf") || nbt.getBoolean("healSelf");
        this.healAllies = nbt.hasKey("healAllies") && nbt.getBoolean("healAllies");
        this.healRadius = nbt.hasKey("healRadius") ? nbt.getFloat("healRadius") : 0.0f;
        this.instantHeal = !nbt.hasKey("instantHeal") || nbt.getBoolean("instantHeal");
    }

    // Getters & Setters
    public float getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(float healAmount) {
        this.healAmount = healAmount;
    }

    public float getHealPercent() {
        return healPercent;
    }

    public void setHealPercent(float healPercent) {
        this.healPercent = healPercent;
    }

    public boolean isHealSelf() {
        return healSelf;
    }

    public void setHealSelf(boolean healSelf) {
        this.healSelf = healSelf;
    }

    public boolean isHealAllies() {
        return healAllies;
    }

    public void setHealAllies(boolean healAllies) {
        this.healAllies = healAllies;
    }

    public float getHealRadius() {
        return healRadius;
    }

    public void setHealRadius(float healRadius) {
        this.healRadius = healRadius;
    }

    public boolean isInstantHeal() {
        return instantHeal;
    }

    public void setInstantHeal(boolean instantHeal) {
        this.instantHeal = instantHeal;
    }
}
