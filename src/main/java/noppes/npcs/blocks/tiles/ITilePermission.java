package noppes.npcs.blocks.tiles;

import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcsPermissions;

public interface ITilePermission {

    CustomNpcsPermissions.Permission getPermission();

    static boolean doesIDMatch(Class<? extends TileEntity> tileClass, String givenID) {
        if (givenID == null)
            return false;

        Object entry = TileEntity.classToNameMap.get(tileClass);
        return givenID.equals(entry);
    }
}
