//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.scripted.interfaces.*;
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;
import noppes.npcs.scripted.interfaces.entity.IPlayer;
import noppes.npcs.scripted.interfaces.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;

public class NpcEvent extends CustomNPCsEvent {
    public final ICustomNpc npc;

    public NpcEvent(ICustomNpc npc) {
        this.npc = npc;
    }

    public static class TimerEvent extends NpcEvent {
        public final int id;

        public TimerEvent(ICustomNpc npc, int id) {
            super(npc);
            this.id = id;
        }

        public int getId(){
            return id;
        }
    }

    public static class CollideEvent extends NpcEvent {
        public final IEntity entity;

        public CollideEvent(ICustomNpc npc, Entity entity) {
            super(npc);
            this.entity = NpcAPI.Instance().getIEntity(entity);
        }
    }

    @Cancelable
    public static class DamagedEvent extends NpcEvent {
        public final IDamageSource damageSource;
        public final IEntity source;
        public float damage;
        private boolean clearTarget = false;

        public boolean clear = false;
        public final DamageSource damagesource;

        public DamagedEvent(ICustomNpc npc, Entity source, float damage, DamageSource damagesource) {
            super(npc);
            this.source = NpcAPI.Instance().getIEntity(source);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);

            //Old CNPC Script compatibility
            this.damagesource = damagesource;
        }

        /**
         * @return The source of the damage
         */
        public IEntity getSource(){
            return source;
        }

        /**
         * @return Returns the damage value
         */
        public float getDamage(){
            return damage;
        }

        /**
         * @param damage The new damage value
         */
        public void setDamage(float damage){
            this.damage = damage;
        }

        public void setClearTarget(boolean bo){
            this.clearTarget = bo;
            this.clear = bo;
        }

        public boolean getClearTarget(){
            return clearTarget && clear;
        }

        public String getType(){
            return damageSource.getMCDamageSource().damageType;
        }
    }

    public static class RangedLaunchedEvent extends NpcEvent {
        public final IEntityLivingBase target;
        public float damage;
        //public List<IProjectile> projectiles = new ArrayList();

        public RangedLaunchedEvent(ICustomNpc npc, float damage, EntityLivingBase target) {
            super(npc);
            this.target = (IEntityLivingBase)NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
        }
    }

    @Cancelable
    public static class MeleeAttackEvent extends NpcEvent {
        public final IEntityLivingBase target;
        public float damage;

        public MeleeAttackEvent(ICustomNpc npc, float damage, EntityLivingBase target) {
            super(npc);
            this.target = (IEntityLivingBase)NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
        }
    }

    public static class KilledEntityEvent extends NpcEvent {
        public final IEntityLivingBase entity;

        public KilledEntityEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase)NpcAPI.Instance().getIEntity(entity);
        }
    }

    public static class DiedEvent extends NpcEvent {
        public final IDamageSource damageSource;
        public final String type;
        public final IEntity source;
        public IItemStack[] droppedItems;
        public int expDropped;

        public DiedEvent(ICustomNpc npc, DamageSource damagesource, Entity entity, ArrayList<ItemStack> droppedItems, int expDropped) {
            super(npc);
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
            this.type = damagesource.damageType;
            this.source = NpcAPI.Instance().getIEntity(entity);

            ArrayList<IItemStack> iItemStacks = new ArrayList<>();
            for (ItemStack itemStack : droppedItems) {
                IItemStack iItemStack = NpcAPI.Instance().getIItemStack(itemStack);
                iItemStacks.add(iItemStack);
            }
            this.droppedItems = iItemStacks.toArray(new IItemStack[0]);
            this.expDropped = expDropped;
        }

        /**
         * @return The source of the damage
         */
        public IEntity getSource(){
            return source;
        }

        public IDamageSource getDamageSource() { return damageSource; }

        /**
         * @return Returns the damage type
         */
        public String getType(){
            return damageSource.getMCDamageSource().damageType;
        }
    }

    @Cancelable
    public static class InteractEvent extends NpcEvent {
        public final IPlayer player;

        public InteractEvent(ICustomNpc npc, EntityPlayer player) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
        }
    }

    public static class DialogEvent extends NpcEvent {
        public final IPlayer player;
        public final int id;
        public final int optionId;
        public final Dialog dialogObj;

        public final int dialog;
        public final int option;

        public DialogEvent(ICustomNpc npc, EntityPlayer player, int id, int optionId, Dialog dialog) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
            this.id = id;
            this.optionId = optionId;
            this.dialogObj = dialog;

            this.dialog = id;
            this.option = optionId;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public Dialog getDialog() {
            return dialogObj;
        }

        public int getDialogId() {
            return dialog;
        }

        public int getOptionId() {
            return option;
        }

        /**
         * @deprecated
         */
        public boolean isClosing(){
            return true;
        }
    }

    public static class DialogClosedEvent extends NpcEvent {
        public final IPlayer player;
        public final int id;
        public final int optionId;
        public final Dialog dialog;

        public DialogClosedEvent(ICustomNpc npc, EntityPlayer player, int id, int optionId, Dialog dialog) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
            this.id = id;
            this.optionId = optionId;
            this.dialog = dialog;
        }
    }

    @Cancelable
    public static class TargetLostEvent extends NpcEvent {
        public final IEntityLivingBase entity;

        public TargetLostEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
        }
    }

    @Cancelable
    public static class TargetEvent extends NpcEvent {
        public IEntityLivingBase entity;

        public TargetEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
        }

        /**
         * @return The target source
         */
        public IEntityLivingBase getTarget(){
            return entity;
        }

        public void setTarget(IEntityLivingBase entity){
            this.entity = entity;
        }
    }

    public static class UpdateEvent extends NpcEvent {
        public UpdateEvent(ICustomNpc npc) {
            super(npc);
        }
    }

    public static class InitEvent extends NpcEvent {
        public InitEvent(ICustomNpc npc) {
            super(npc);
        }
    }

    public static class AttackEvent extends NpcEvent{
        private float damage;
        private IEntityLivingBase target;
        private boolean isRanged;

        public AttackEvent(ICustomNpc npc, float damage, EntityLivingBase target, boolean isRanged){
            super(npc);
            this.damage = damage;
            this.isRanged = isRanged;
            this.target = (IEntityLivingBase) NpcAPI.Instance().getIEntity(target);
        }

        /**
         * @return The source of the damage
         * @deprecated
         */
        public IEntityLivingBase getTarget(){
            return target;
        }


        /**
         * @return Returns the damage value
         */
        public float getDamage(){
            return damage;
        }

        /**
         * @param damage The new damage value
         */
        public void setDamage(float damage){
            this.damage = damage;
        }

        /**
         * @return Returns the damage type
         */
        public boolean isRange(){
            return isRanged;
        }
    }
}
