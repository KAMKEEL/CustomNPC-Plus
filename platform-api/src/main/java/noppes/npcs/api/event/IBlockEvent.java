package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;

/**
 * Events fired for scripted blocks, including interactions, redstone, and entity collisions.
 */
public interface IBlockEvent extends ICustomNPCsEvent {
    /** @return the scripted block associated with this event. */
    IBlock getBlock();

    /**
     * @hookName fallenUpon
     */
    @Cancelable
    interface EntityFallenUponEvent extends IBlockEvent {
        IEntity getEntity();

        float getDistanceFallen();
    }

    @Cancelable
    interface InteractEvent extends IBlockEvent {
        IPlayer getPlayer();

        float getHitX();

        float getHitY();

        float getHitZ();

        int getSide();
    }

    interface RedstoneEvent extends IBlockEvent {
        int getPrevPower();

        int getPower();
    }

    /**
     * @hookName broken
     */
    interface BreakEvent extends IBlockEvent {

    }

    @Cancelable
    interface ExplodedEvent extends IBlockEvent {

    }

    /**
     * @hookName rainFilled
     */
    interface RainFillEvent extends IBlockEvent {

    }

    interface NeighborChangedEvent extends IBlockEvent {
        IPos getChangedPos();
    }

    interface InitEvent extends IBlockEvent {

    }

    /**
     * @hookName tick
     */
    interface UpdateEvent extends IBlockEvent {

    }

    interface ClickedEvent extends IBlockEvent {
        IPlayer getPlayer();
    }

    @Cancelable
    interface HarvestedEvent extends IBlockEvent {
        IPlayer getPlayer();
    }

    interface CollidedEvent extends IBlockEvent {
        IEntity getEntity();
    }

    interface TimerEvent extends IBlockEvent {
        int getId();
    }
}
