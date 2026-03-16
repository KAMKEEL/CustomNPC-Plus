package noppes.npcs.controllers.data;

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
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumRoleType;
import java.util.Set;

public class DataTransform {
    public INbt display;
    public INbt ai;
    public INbt advanced;
    public INbt inv;
    public INbt stats;
    public INbt role;
    public INbt job;

    public boolean hasDisplay, hasAi, hasAdvanced, hasInv, hasStats, hasRole, hasJob, isActive;

    private EntityNPCInterface npc;

    public boolean editingModus = false;

    public DataTransform(EntityNPCInterface npc) {
        this.npc = npc;
    }


    public INbt writeToNBT(INbt compound) {
        compound.setBoolean("TransformIsActive", isActive);
        writeOptions(compound);
        if (hasDisplay)
            compound.setTag("TransformDisplay", display);
        if (hasAi)
            compound.setTag("TransformAI", ai);
        if (hasAdvanced)
            compound.setTag("TransformAdvanced", advanced);
        if (hasInv)
            compound.setTag("TransformInv", inv);
        if (hasStats)
            compound.setTag("TransformStats", stats);
        if (hasRole)
            compound.setTag("TransformRole", role);
        if (hasJob)
            compound.setTag("TransformJob", job);

        return compound;
    }

    public INbt writeOptions(INbt compound) {
        compound.setBoolean("TransformHasDisplay", hasDisplay);
        compound.setBoolean("TransformHasAI", hasAi);
        compound.setBoolean("TransformHasAdvanced", hasAdvanced);
        compound.setBoolean("TransformHasInv", hasInv);
        compound.setBoolean("TransformHasStats", hasStats);
        compound.setBoolean("TransformHasRole", hasRole);
        compound.setBoolean("TransformHasJob", hasJob);
        compound.setBoolean("TransformEditingModus", editingModus);
        return compound;
    }


    public void readToNBT(INbt compound) {
        isActive = compound.getBoolean("TransformIsActive");
        readOptions(compound);
        display = hasDisplay ? compound.getCompoundTag("TransformDisplay") : getDisplay();
        ai = hasAi ? compound.getCompoundTag("TransformAI") : npc.ais.writeToNBT(new INbt());
        advanced = hasAdvanced ? compound.getCompoundTag("TransformAdvanced") : getAdvanced();
        inv = hasInv ? compound.getCompoundTag("TransformInv") : npc.inventory.writeEntityToNBT(new INbt());
        stats = hasStats ? compound.getCompoundTag("TransformStats") : npc.stats.writeToNBT(new INbt());
        job = hasJob ? compound.getCompoundTag("TransformJob") : getJob();
        role = hasRole ? compound.getCompoundTag("TransformRole") : getRole();
    }

    public INbt getJob() {
        INbt compound = new INbt();

        compound.setInteger("NpcJob", npc.advanced.job.ordinal());
        if (npc.advanced.job != EnumJobType.None && npc.jobInterface != null) {
            npc.jobInterface.writeToNBT(compound);
        }

        return compound;
    }

    public INbt getRole() {
        INbt compound = new INbt();

        compound.setInteger("Role", npc.advanced.role.ordinal());
        if (npc.advanced.role != EnumRoleType.None && npc.roleInterface != null) {
            npc.roleInterface.writeToNBT(compound);
        }

        return compound;
    }

    public INbt getDisplay() {
        INbt compound = npc.display.writeToNBT(new INbt());
        if (npc instanceof EntityCustomNpc) {
            compound.setTag("ModelData", ((EntityCustomNpc) npc).modelData.writeToNBT());
        }

        return compound;
    }

    public INbt getAdvanced() {
        EnumJobType jopType = npc.advanced.job;
        EnumRoleType roleType = npc.advanced.role;

        npc.advanced.job = EnumJobType.None;
        npc.advanced.role = EnumRoleType.None;

        INbt compound = npc.advanced.writeToNBT(new INbt());
        compound.removeTag("Role");
        compound.removeTag("NpcJob");

        npc.advanced.job = jopType;
        npc.advanced.role = roleType;

        return compound;
    }


    public void readOptions(INbt compound) {
        boolean hadDisplay = hasDisplay;
        boolean hadAI = hasAi;
        boolean hadAdvanced = hasAdvanced;
        boolean hadInv = hasInv;
        boolean hadStats = hasStats;
        boolean hadRole = hasRole;
        boolean hadJob = hasJob;

        hasDisplay = compound.getBoolean("TransformHasDisplay");
        hasAi = compound.getBoolean("TransformHasAI");
        hasAdvanced = compound.getBoolean("TransformHasAdvanced");
        hasInv = compound.getBoolean("TransformHasInv");
        hasStats = compound.getBoolean("TransformHasStats");
        hasRole = compound.getBoolean("TransformHasRole");
        hasJob = compound.getBoolean("TransformHasJob");
        editingModus = compound.getBoolean("TransformEditingModus");

        if (hasDisplay && !hadDisplay) {
            display = getDisplay();
        }
        if (hasAi && !hadAI)
            ai = npc.ais.writeToNBT(new INbt());
        if (hasStats && !hadStats)
            stats = npc.stats.writeToNBT(new INbt());
        if (hasInv && !hadInv)
            inv = npc.inventory.writeEntityToNBT(new INbt());
        if (hasAdvanced && !hadAdvanced)
            advanced = getAdvanced();
        if (hasJob && !hadJob)
            job = getJob();
        if (hasRole && !hadRole)
            role = getRole();
    }

    public boolean isValid() {
        return hasAdvanced || hasAi || hasDisplay || hasInv || hasStats || hasJob || hasRole;
    }


    public INbt processAdvanced(INbt compoundAdv,
                                          INbt compoundRole, INbt compoundJob) {

        if (hasAdvanced)
            compoundAdv = advanced;
        if (hasRole)
            compoundRole = role;
        if (hasJob)
            compoundJob = job;

        Set<String> names = compoundRole.func_150296_c();
        for (String name : names)
            compoundAdv.setTag(name, compoundRole.getTag(name));

        names = compoundJob.func_150296_c();
        for (String name : names)
            compoundAdv.setTag(name, compoundJob.getTag(name));

        return compoundAdv;
    }

    public void transform(boolean isActive) {
        if (this.isActive == isActive)
            return;
        if (hasDisplay) {
            INbt compound = getDisplay();
            npc.display.readToNBT(NBTTags.NBTMerge(compound, display));
            if (npc instanceof EntityCustomNpc) {
                ((EntityCustomNpc) npc).modelData.readFromNBT(NBTTags.NBTMerge(compound.getCompoundTag("ModelData"), display.getCompoundTag("ModelData")));
            }
            display = compound;
        }
        if (hasStats) {
            INbt compound = npc.stats.writeToNBT(new INbt());
            npc.stats.readToNBT(NBTTags.NBTMerge(compound, stats));
            stats = compound;
        }
        if (hasAdvanced || hasJob || hasRole) {
            INbt compoundAdv = getAdvanced();
            INbt compoundRole = getRole();
            INbt compoundJob = getJob();

            INbt compound = processAdvanced(compoundAdv, compoundRole, compoundJob);
            npc.advanced.readToNBT(compound);
            if (npc.advanced.role != EnumRoleType.None && npc.roleInterface != null)
                npc.roleInterface.readFromNBT(NBTTags.NBTMerge(compoundRole, compound));
            if (npc.advanced.job != EnumJobType.None && npc.jobInterface != null)
                npc.jobInterface.readFromNBT(NBTTags.NBTMerge(compoundJob, compound));

            if (hasAdvanced)
                advanced = compoundAdv;
            if (hasRole)
                role = compoundRole;
            if (hasJob)
                job = compoundJob;
        }
        if (hasAi) {
            INbt compound = npc.ais.writeToNBT(new INbt());
            npc.ais.readToNBT(NBTTags.NBTMerge(compound, ai));
            ai = compound;
            npc.setCurrentAnimation(EnumAnimation.NONE);
        }
        if (hasInv) {
            INbt compound = npc.inventory.writeEntityToNBT(new INbt());
            npc.inventory.readEntityFromNBT(NBTTags.NBTMerge(compound, inv));
            inv = compound;
        }
        npc.updateHitbox();
        npc.updateAI = true;
        this.isActive = isActive;
        npc.updateClient = true;
    }
}
