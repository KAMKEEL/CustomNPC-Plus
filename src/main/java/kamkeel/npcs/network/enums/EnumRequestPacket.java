package kamkeel.npcs.network.enums;

import noppes.npcs.CustomNpcsPermissions;

public enum EnumRequestPacket {

    ok;

    public CustomNpcsPermissions.Permission permission = null;
    public boolean needsNpc = false;

    EnumRequestPacket() {}
    EnumRequestPacket(noppes.npcs.CustomNpcsPermissions.Permission permission, boolean npc) {
        this(permission);
    }
    EnumRequestPacket(boolean npc) {
        needsNpc = npc;
    }
    EnumRequestPacket(CustomNpcsPermissions.Permission permission) {
        this.permission = permission;
    }
    public boolean hasPermission() {
        return permission != null;
    }

}
