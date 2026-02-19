package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.Entity;

/**
 * Interface for routing entity spawn/kill calls during preview mode.
 * In preview mode, entities are not added to the world but instead
 * tracked by the GUI for rendering in the 3D viewport.
 */
public interface PreviewEntityHandler {
    void onEntitySpawned(Entity entity);

    void onEntityRemoved(Entity entity);
}
