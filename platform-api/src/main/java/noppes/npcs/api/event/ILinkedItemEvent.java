package noppes.npcs.api.event;

/**
 * Events fired for linked items when their version changes or when they are being built.
 */
public interface ILinkedItemEvent extends IItemEvent {

    /**
     * Fired when the linked item's version changes.
     * @hookName versionChanged
     */
    interface VersionChangeEvent extends IItemEvent {
        /** @return the new version number. */
        int getVersion();

        /** @return the previous version number. */
        int getPreviousVersion();
    }

    /**
     * Fired when the linked item is being built/constructed.
     * @hookName buildingItem
     */
    interface BuildEvent extends IItemEvent {
    }
}
