package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.AbstractNpcAPI;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.event.ICustomNPCsEvent;
import noppes.npcs.api.handler.data.INaturalSpawn;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

public class CustomNPCsEvent extends Event implements ICustomNPCsEvent {
    public final AbstractNpcAPI API = AbstractNpcAPI.Instance();

    public String getHookName() {
        return "CNPCEvent";
    }

    public CustomNPCsEvent() {
    }

    public void setCancelled(boolean bo){
        this.setCanceled(bo);
    }

    public boolean isCancelled(){return this.isCanceled();}

    @Cancelable
    public static class CNPCNaturalSpawnEvent extends CustomNPCsEvent implements ICustomNPCsEvent.CNPCNaturalSpawnEvent {
        public final IEntityLivingBase<?> entity;
        public final INaturalSpawn naturalSpawn;
        public IPos attemptPosition;
        public final boolean animalSpawnPassed;
        public final boolean monsterSpawnPassed;
        public final boolean liquidSpawnPassed;
        public final boolean airSpawnPassed;

        public CNPCNaturalSpawnEvent(EntityLivingBase entity, INaturalSpawn naturalSpawn, IPos attemptPosition, boolean animalSpawnPassed, boolean monsterSpawnPassed, boolean liquidSpawnPassed, boolean airSpawnPassed) {
            this.entity = (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity);
            this.naturalSpawn = naturalSpawn;
            this.attemptPosition = attemptPosition;

            this.animalSpawnPassed = animalSpawnPassed;
            this.monsterSpawnPassed = monsterSpawnPassed;
            this.liquidSpawnPassed = liquidSpawnPassed;
            this.airSpawnPassed = airSpawnPassed;
        }

        public String getHookName() {
            return EnumScriptType.CNPC_NATURAL_SPAWN.function;
        }

        public IEntityLivingBase<?> getEntity() {
            return entity;
        }

        public INaturalSpawn getNaturalSpawn() {
            return this.naturalSpawn;
        }

        public void setAttemptPosition(IPos attemptPosition) {
            this.attemptPosition = attemptPosition;
        }

        public IPos getAttemptPosition() {
            return this.attemptPosition;
        }

        public boolean animalSpawnPassed() {
            return this.animalSpawnPassed;
        }

        public boolean monsterSpawnPassed() {
            return this.monsterSpawnPassed;
        }

        public boolean liquidSpawnPassed() {
            return this.liquidSpawnPassed;
        }

        public boolean airSpawnPassed() {
            return this.airSpawnPassed;
        }
    }

    public static class ScriptedCommandEvent extends CustomNPCsEvent implements ICustomNPCsEvent.ScriptedCommandEvent {

        public IWorld world;
        public IPos pos;
        public String senderName;
        public String id;
        public String[] args;
        public String replyMessage = "";

        public ScriptedCommandEvent(IWorld world, IPos pos, String senderName, String id, String[] args){
            this.world = world;
            this.pos = pos;
            this.senderName = senderName;
            this.id = id;
            this.args = args;
        }

        public String getHookName() {
            return EnumScriptType.SCRIPT_COMMAND.function;
        }

        @Override
        public IWorld getSenderWorld() {
            return world;
        }

        @Override
        public IPos getSenderPosition() {
            return pos;
        }

        @Override
        public String getSenderName() {
            return senderName;
        }

        @Override
        public void setReplyMessage(String message) {
            replyMessage = message;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String[] getArgs() {
            return args;
        }
    }
}
