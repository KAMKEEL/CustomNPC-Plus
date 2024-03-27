package noppes.npcs.scripted.event;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;

public class WorldEvent extends CustomNPCsEvent {

    public final IWorld world;

    public WorldEvent(IWorld world) {
        this.world = world;
    }


    public static class ScriptCommandEvent extends WorldEvent {
        public final String[] arguments;

        public final IPos pos;

        public ScriptCommandEvent(IWorld world, IPos pos, String[] arguments) {
            super(world);
            this.arguments = arguments;
            this.pos = pos;
        }
    }

}
