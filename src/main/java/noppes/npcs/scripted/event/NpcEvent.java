//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.*;

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
        public boolean clearTarget = false;

        public DamagedEvent(ICustomNpc npc, Entity source, float damage, DamageSource damagesource) {
            super(npc);
            this.source = NpcAPI.Instance().getIEntity(source);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
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

        public DiedEvent(ICustomNpc npc, DamageSource damagesource, Entity entity) {
            super(npc);
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
            this.type = damagesource.damageType;
            this.source = NpcAPI.Instance().getIEntity(entity);
        }

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
        public final Dialog dialog;

        public DialogEvent(ICustomNpc npc, EntityPlayer player, int id, int optionId, Dialog dialog) {
            super(npc);
            this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
            this.id = id;
            this.optionId = optionId;
            this.dialog = dialog;
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
            this.entity = (IEntityLivingBase)NpcAPI.Instance().getIEntity(entity);
        }
    }

    @Cancelable
    public static class TargetEvent extends NpcEvent {
        public IEntityLivingBase entity;

        public TargetEvent(ICustomNpc npc, EntityLivingBase entity) {
            super(npc);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
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
}
