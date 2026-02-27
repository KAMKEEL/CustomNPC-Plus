package noppes.npcs.scripted.event;

import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IChainedAbility;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IChainEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

/**
 * Unified chain events for both NPCs and Players.
 */
public class ChainEvent extends CustomNPCsEvent implements IChainEvent {
    protected final IEntityLivingBase entity;
    public final IPlayer player;
    public final ICustomNpc<?> npc;
    protected final ChainedAbility chain;
    protected final IEntityLivingBase target;
    protected final int entryIndex;

    public ChainEvent(EntityLivingBase entity, ChainedAbility chain, int entryIndex, EntityLivingBase target) {
        this.entity = entity != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity) : null;
        this.chain = chain;
        this.entryIndex = entryIndex;
        this.target = target != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(target) : null;
        this.player = (this.entity instanceof IPlayer) ? (IPlayer) this.entity : null;
        this.npc = (this.entity instanceof ICustomNpc) ? (ICustomNpc<?>) this.entity : null;
    }

    @Override
    public IEntityLivingBase getEntity() {
        return entity;
    }

    @Override
    public IPlayer getPlayer() {
        return player;
    }

    @Override
    public ICustomNpc<?> getNpc() {
        return npc;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof ICustomNpc;
    }

    @Override
    public IChainedAbility getChain() {
        return chain;
    }

    @Override
    public IEntityLivingBase getTarget() {
        return target;
    }

    @Override
    public int getEntryIndex() {
        return entryIndex;
    }

    @Override
    public String getHookName() {
        return "chainEvent";
    }

    public static class StartEvent extends ChainEvent implements IChainEvent.StartEvent {
        public StartEvent(EntityLivingBase entity, ChainedAbility chain, int entryIndex, EntityLivingBase target) {
            super(entity, chain, entryIndex, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.CHAIN_START.function;
        }
    }

    public static class NextEvent extends ChainEvent implements IChainEvent.NextEvent {
        public NextEvent(EntityLivingBase entity, ChainedAbility chain, int entryIndex, EntityLivingBase target) {
            super(entity, chain, entryIndex, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.CHAIN_NEXT.function;
        }
    }

    public static class CompleteEvent extends ChainEvent implements IChainEvent.CompleteEvent {
        public CompleteEvent(EntityLivingBase entity, ChainedAbility chain, int entryIndex, EntityLivingBase target) {
            super(entity, chain, entryIndex, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.CHAIN_COMPLETE.function;
        }
    }

    public static class InterruptEvent extends ChainEvent implements IChainEvent.InterruptEvent {
        private final IDamageSource damageSource;
        private final float damage;

        public InterruptEvent(EntityLivingBase entity, ChainedAbility chain, int entryIndex, EntityLivingBase target,
                              DamageSource source, float damage) {
            super(entity, chain, entryIndex, target);
            this.damageSource = source != null ? NpcAPI.Instance().getIDamageSource(source) : null;
            this.damage = damage;
        }

        @Override
        public IDamageSource getDamageSource() {
            return damageSource;
        }

        @Override
        public float getDamage() {
            return damage;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.CHAIN_INTERRUPT.function;
        }
    }
}
