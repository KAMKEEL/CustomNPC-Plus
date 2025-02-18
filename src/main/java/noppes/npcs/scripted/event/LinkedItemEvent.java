package noppes.npcs.scripted.event;

import noppes.npcs.api.event.ILinkedItemEvent;
import noppes.npcs.api.item.IItemLinked;
import noppes.npcs.constants.EnumScriptType;

public class LinkedItemEvent extends ItemEvent implements ILinkedItemEvent {

    public LinkedItemEvent(IItemLinked item) {
        super(item);
    }

    public static class VersionChangeEvent extends ItemEvent implements ILinkedItemEvent.VersionChangeEvent {
        public final int version, prevVersion;

        public VersionChangeEvent(IItemLinked item, int version, int prevVersion) {
            super(item);
            this.version = version;
            this.prevVersion = prevVersion;
        }

        @Override
        public int getVersion() {
            return this.version;
        }

        @Override
        public int getPreviousVersion() {
            return this.prevVersion;
        }

        public String getHookName() {
            return EnumScriptType.LINKED_ITEM_VERSION.function;
        }
    }

    public static class BuildEvent extends ItemEvent implements ILinkedItemEvent.BuildEvent {
        public BuildEvent(IItemLinked item) {
            super(item);
        }

        public String getHookName() {
            return EnumScriptType.LINKED_ITEM_BUILD.function;
        }
    }
}
