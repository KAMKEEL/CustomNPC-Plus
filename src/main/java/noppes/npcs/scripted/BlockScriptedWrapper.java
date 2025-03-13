package noppes.npcs.scripted;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.ITileEntity;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileScripted;

import java.util.Set;

public class BlockScriptedWrapper extends ScriptBlock implements IBlockScripted {
    private TileScripted tile;

    public BlockScriptedWrapper(World world, Block block, int x, int y, int z) {
        super(world, block, new BlockPos(x, y, z));
        tile = (TileScripted) super.tile.getMCTileEntity();
    }

    @Override
    public void setModel(IItemStack item) {
        if (item == null)
            tile.setItemModel(null, null);
        else
            tile.setItemModel(item.getMCItemStack(), Block.getBlockFromItem(item.getMCItemStack().getItem()));
    }

    @Override
    public void setModel(String name) {
        if (name == null)
            tile.setItemModel(null, null);
        else {
            Block block = Block.getBlockFromName(name);
            if (block != null) {
                tile.setItemModel(new ItemStack(Item.getItemFromBlock(block)), block);
            } else {
                if (Item.itemRegistry.containsKey(name)) {
                    tile.setItemModel(new ItemStack((Item) Item.itemRegistry.getObject(name)), null);
                } else {
                    tile.setItemModel(null, null);
                }
            }
        }
    }

    @Override
    public IItemStack getModel() {
        return NpcAPI.Instance().getIItemStack(tile.itemModel);
    }

    @Override
    public void setRedstonePower(int strength) {
        tile.setRedstonePower(strength);
    }

    @Override
    public int getRedstonePower() {
        return tile.powering;
    }

    @Override
    public void setIsLadder(boolean bo) {
        tile.isLadder = bo;
        tile.needsClientUpdate = true;
    }

    @Override
    public boolean getIsLadder() {
        return tile.isLadder;
    }

    @Override
    public void setIsPassible(boolean bo) {
        setIsPassable(bo);
    }

    @Override
    public boolean getIsPassible() {
        return getIsPassable();
    }

    @Override
    public void setIsPassable(boolean bo) {
        tile.isPassible = bo;
        tile.needsClientUpdate = true;
    }

    @Override
    public boolean getIsPassable() {
        if (tile == null)
            return false;
        return tile.isPassible;
    }

    @Override
    public void setLight(int value) {
        tile.setLightValue(value);
    }

    @Override
    public int getLight() {
        if (tile == null)
            return 0;
        return tile.lightValue;
    }

    @Override
    public void setScale(float x, float y, float z) {
        tile.setScale(x, y, z);
    }

    @Override
    public float getScaleX() {
        return tile.scaleX;
    }

    @Override
    public float getScaleY() {
        return tile.scaleY;
    }

    @Override
    public float getScaleZ() {
        return tile.scaleZ;
    }

    @Override
    public void setRotation(int x, int y, int z) {
        tile.setRotation(x % 360, y % 360, z % 360);
    }

    @Override
    public int getRotationX() {
        return tile.rotationX;
    }

    @Override
    public int getRotationY() {
        return tile.rotationY;
    }

    @Override
    public int getRotationZ() {
        return tile.rotationZ;
    }

    @Override
    public float getHardness() {
        return tile.blockHardness;
    }

    @Override
    public void setHardness(float hardness) {
        tile.blockHardness = hardness;
    }

    @Override
    public float getResistance() {
        return tile.blockResistance;
    }

    @Override
    public void setResistance(float resistance) {
        tile.blockResistance = resistance;
    }

    @Override
    public void executeCommand(String command) {
        if (!MinecraftServer.getServer().isCommandBlockEnabled())
            throw new CustomNPCsException("Command blocks need to be enabled to executeCommands");
        NoppesUtilServer.runCommand(world.getMCWorld(), "ScriptedBlock", command);
    }

    @Override
    public ITextPlane getTextPlane() {
        return tile.text1;
    }

    @Override
    public ITextPlane getTextPlane2() {
        return tile.text2;
    }

    @Override
    public ITextPlane getTextPlane3() {
        return tile.text3;
    }

    @Override
    public ITextPlane getTextPlane4() {
        return tile.text4;
    }

    @Override
    public ITextPlane getTextPlane5() {
        return tile.text5;
    }

    @Override
    public ITextPlane getTextPlane6() {
        return tile.text6;
    }

    @Override
    public ITimers getTimers() {
        return tile.timers;
    }

    @Override
    public void setTileEntity(ITileEntity tile) {
        this.tile = (TileScripted) tile;
        super.setTileEntity(tile);
    }

    private NBTTagCompound getNBT() {
        if (tile == null)
            return null;
        NBTTagCompound compound = tile.getTileData().getCompoundTag("CustomNPCsData");
        if (compound.hasNoTags() && !tile.getTileData().hasKey("CustomNPCsData")) {
            tile.getTileData().setTag("CustomNPCsData", compound);
        }
        return compound;
    }

    @Override
    public void setStoredData(String key, Object value) {
        NBTTagCompound compound = getNBT();
        if (compound == null)
            return;
        if (value instanceof Number)
            compound.setDouble(key, ((Number) value).doubleValue());
        else if (value instanceof String)
            compound.setString(key, (String) value);
    }

    @Override
    public Object getStoredData(String key) {
        NBTTagCompound compound = getNBT();
        if (compound == null)
            return null;
        if (!compound.hasKey(key))
            return null;
        NBTBase base = compound.getTag(key);
        if (base instanceof NBTBase.NBTPrimitive)
            return ((NBTBase.NBTPrimitive) base).func_150286_g();
        return ((NBTTagString) base).func_150285_a_();
    }

    @Override
    public void removeStoredData(String key) {
        NBTTagCompound compound = getNBT();
        if (compound == null)
            return;
        compound.removeTag(key);
    }

    @Override
    public boolean hasStoredData(String key) {
        NBTTagCompound compound = getNBT();
        if (compound == null)
            return false;
        return compound.hasKey(key);
    }

    @Override
    public void clearStoredData() {
        if (tile == null)
            return;
        tile.getTileData().setTag("CustomNPCsData", new NBTTagCompound());
    }

    @Override
    public String[] getStoredDataKeys() {
        NBTTagCompound compound = getNBT();
        if (compound == null)
            return new String[0];
        return ((Set<String>) compound.func_150296_c()).toArray(new String[0]);
    }

    @Override
    public void removeTempData(String key) {
        if (tile == null)
            return;
        tile.tempData.remove(key);
    }

    @Override
    public void setTempData(String key, Object value) {
        if (tile == null)
            return;
        tile.tempData.put(key, value);
    }

    @Override
    public boolean hasTempData(String key) {
        if (tile == null)
            return false;
        return tile.tempData.containsKey(key);
    }

    @Override
    public Object getTempData(String key) {
        if (tile == null)
            return null;
        return tile.tempData.get(key);
    }

    @Override
    public void clearTempData() {
        if (tile == null)
            return;
        tile.tempData.clear();
    }

    @Override
    public String[] getTempDataKeys() {
        return tile.tempData.keySet().toArray(new String[tile.tempData.size()]);
    }
}

