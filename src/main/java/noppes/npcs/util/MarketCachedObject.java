package noppes.npcs.util;

import foxz.utils.Market;
import net.minecraft.nbt.NBTTagCompound;

public class MarketCachedObject extends CacheHashMap.CachedObject<NBTTagCompound> {
    public MarketCachedObject(NBTTagCompound object) {
        super(object);
    }

    @Override
    public void save() {
        Market.saveFile(getObject());
    }
}
