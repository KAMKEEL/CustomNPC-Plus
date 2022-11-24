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
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.event.INpcEvent;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;

public class NpcEvent extends CustomNPCsEvent implements INpcEvent {
    public final ICustomNpc npc;

    public NpcEvent(ICustomNpc npc) {
        this.npc = npc;
    }

    public ICustomNpc getNpc() {
        return npc;
    }

    public static class TimerEvent extends NpcEvent implements INpcEvent.TimerEvent {
        public final int id;

        public TimerEvent(ICustomNpc npc, int id) {
            super(npc);
            this.id = id;
        }

        public String getHookName() {
            return EnumScriptType.TIMER.function;
        }

        public int getId(){
            return id;
        }
    }

    public static class CollideEvent extends NpcEvent implements INpcEvent.CollideEvent {
        public final IEntity entity;

        public CollideEvent(ICustomNpc npc, Entity entity) {
            super(npc);
            this.entity = NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.COLLIDE.function;
        }

        public IEntity getEntity() {
            return entity;
        }
    }

    @Cancelable
    public static class DamagedEvent extends NpcEvent implements INpcEvent.DamagedEvent {
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

        public String getHookName() {
            return EnumScriptType.DAMAGED.function;
        }

        /**
         * @return The source of the damage
         */
        public IEntity getSource(){
            return source;
        }

        public IDamageSource getDamageSource(){
            return damageSource;
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

    @Cancelable
    public static class RangedLaunchedEvent extends NpcEvent implements INpcEvent.RangedLaunchedEvent {
        public final IEntityLivingBase target;
        public float damage;
        //public List<IProjectile> projectiles = new ArrayList();

        public RangedLaunchedEvent(ICustomNpc npc, float damage, EntityLivingBase target) {
            super(npc);
            this.target = (IEntityLivingBase)NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
        }

        public String getHookName() {
            return EnumScriptType.RANGED_LAUNCHED.function;
        }

        /**
         * @return The source of the damage
         * @deprecated
         */
        public IEntityLivingBase getTarget(){
            return target;
        }

        /**
         * @param damage The new damage value
         */
        public void setDamage(float damage){
            this.damage = damage;
        }


        /**
         * @return Returns the damage value
         */
        public float getDamage(){
            return damage;
        }

        /**
         * @return Returns the damage type
         */
        public boolean isRange(){
            return true;
        }
    }

    @Cancelable
    public static class MeleeAttackEvent extends NpcEvent implements INpcEvent.MeleeAttackEvent {
        public final IEntityLivingBase target;
        public float damage;

        public MeleeAttackEvent(ICustomNpc npc, float damage, EntityLivingBase target) {
            super(npc);
            this.target = (IEntityLivingBase)NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
        }

        public String getHookName() {
            return EnumScriptType.ATTACK_MELEE.function;
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
            return false;
        }
    }

    public static class KilledEntityEvent extends NpcEvent implements INpcEvent.KilledEntityEvent {
        public final IEntityLivingBase entity;

        public KilledEntityEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase)NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.KILLS.function;
        }

        public IEntityLivingBase getEntity() {
            return entity;
        }
    }

    @Cancelable
    public static class DiedEvent extends NpcEvent implements INpcEvent.DiedEvent {
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

        public String getHookName() {
            return EnumScriptType.KILLED.function;
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

        public void setDroppedItems(IItemStack[] droppedItems) {
            this.droppedItems = droppedItems;
        }

        public IItemStack[] getDroppedItems() {
            return droppedItems;
        }

        public void setExpDropped(int expDropped) {
            this.expDropped = expDropped;
        }

        public int getExpDropped() {
            return expDropped;
        }
    }

    @Cancelable
    public static class InteractEvent extends NpcEvent implements INpcEvent.InteractEvent {
        public final IPlayer player;

        public InteractEvent(ICustomNpc npc, EntityPlayer player) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
        }

        public String getHookName() {
            return EnumScriptType.INTERACT.function;
        }

        public IPlayer getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class DialogEvent extends NpcEvent implements INpcEvent.DialogEvent {
        public final IPlayer player;
        public final int id;
        public final int optionId;
        public final IDialog dialogObj;

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

        public String getHookName() {
            return EnumScriptType.DIALOG.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public IDialog getDialog() {
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

    public static class DialogClosedEvent extends NpcEvent implements INpcEvent.DialogClosedEvent {
        public final IPlayer player;
        public final int id;
        public final int optionId;
        public final Dialog dialogObj;

        public final int dialog;
        public final int option;

        public DialogClosedEvent(ICustomNpc npc, EntityPlayer player, int id, int optionId, Dialog dialog) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
            this.id = id;
            this.optionId = optionId;
            this.dialogObj = dialog;

            this.dialog = id;
            this.option = optionId;
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_CLOSE.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public Dialog getDialog() {
            return dialogObj;
        }

        public int getDialogId() {
            return option;
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

    @Cancelable
    public static class TargetLostEvent extends NpcEvent implements INpcEvent.TargetLostEvent {
        public final IEntityLivingBase entity;

        public TargetLostEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.TARGET_LOST.function;
        }

        public IEntityLivingBase getTarget() {
            return entity;
        }
    }

    @Cancelable
    public static class TargetEvent extends NpcEvent implements INpcEvent.TargetEvent {
        public IEntityLivingBase entity;

        public TargetEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.TARGET.function;
        }

        public void setTarget(IEntityLivingBase entity){
            this.entity = entity;
        }

        /**
         * @return The target source
         */
        public IEntityLivingBase getTarget(){
            return entity;
        }
    }

    public static class UpdateEvent extends NpcEvent implements INpcEvent.UpdateEvent {
        public UpdateEvent(ICustomNpc npc) {
            super(npc);
        }

        public String getHookName() {
            return EnumScriptType.TICK.function;
        }
    }

    public static class InitEvent extends NpcEvent implements INpcEvent.InitEvent {
        public InitEvent(ICustomNpc npc) {
            super(npc);
        }

        public String getHookName() {
            return EnumScriptType.INIT.function;
        }
    }
}
