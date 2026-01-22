package noppes.npcs.blocks.tiles;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.packets.request.script.BlockScriptPacket;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.TextBlock;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.client.renderer.blocks.BlockScriptedRenderer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.IScriptHandlerPacket;
import noppes.npcs.controllers.data.IScriptBlockHandler;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.scripted.BlockScriptedWrapper;
import noppes.npcs.util.ValueUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TileScripted extends TileEntity implements IScriptBlockHandler, IScriptHandlerPacket {
    public List<IScriptUnit> scripts = new ArrayList<>();

    public Map<String, Object> tempData = new HashMap<>();

    private NBTTagCompound customTileData;

    public String scriptLanguage = "ECMAScript";
    public boolean enabled = false;

    private IBlock blockDummy = null;
    public DataTimers timers = new DataTimers(this);

    public long lastInited = -1;

    private short ticksExisted = 0;

    public ItemStack itemModel = new ItemStack(CustomItems.scripted);
    public Block blockModel = null;
    private boolean hideModel;
    private int metadata;

    public boolean needsClientUpdate = false;

    public int powering = 0;
    public int activePowering = 0;
    public int newPower = 0; //used for block redstone event
    public int prevPower = 0; //used for block redstone event

    public boolean isPassible = false;
    public boolean isLadder = false;
    public int lightValue = 0;

    public float blockHardness = 5;
    public float blockResistance = 10;

    public int rotationX = 0, rotationY = 0, rotationZ = 0;
    public float scaleX = 1, scaleY = 1, scaleZ = 1;

    public TileEntity renderTile;
    public boolean renderTileErrored = true;
    public boolean renderFullBlock = true;
    public ITickable renderTileUpdate = null;

    public TextPlane text1 = new TextPlane();
    public TextPlane text2 = new TextPlane();
    public TextPlane text3 = new TextPlane();
    public TextPlane text4 = new TextPlane();
    public TextPlane text5 = new TextPlane();
    public TextPlane text6 = new TextPlane();

    public IBlock getBlock() {
        if (blockDummy == null)
            blockDummy = new BlockScriptedWrapper(worldObj, this.getBlockType(), xCoord, yCoord, zCoord);
        return blockDummy;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setNBT(compound);
        setDisplayNBT(compound);
        timers.readFromNBT(compound);
    }

    public void setNBT(NBTTagCompound compound) {
        scripts = NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this);
        scriptLanguage = compound.getString("ScriptLanguage");
        enabled = compound.getBoolean("ScriptEnabled");
        activePowering = powering = compound.getInteger("BlockPowering");
        prevPower = compound.getInteger("BlockPrevPower");

        if (compound.hasKey("BlockHardness")) {
            blockHardness = compound.getFloat("BlockHardness");
            blockResistance = compound.getFloat("BlockResistance");
        }

    }

    public void setDisplayNBT(NBTTagCompound compound) {
        itemModel = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("ScriptBlockModel"));
        if (itemModel == null || itemModel.stackSize == 0)
            itemModel = new ItemStack(Item.getItemFromBlock(CustomItems.scripted));
        if (compound.hasKey("ScriptBlockModelBlock"))
            blockModel = Block.getBlockFromName(compound.getString("ScriptBlockModelBlock"));
        renderTileUpdate = null;
        renderTile = null;
        renderTileErrored = false;

        lightValue = compound.getInteger("LightValue");
        isLadder = compound.getBoolean("IsLadder");
        isPassible = compound.getBoolean("IsPassible");

        rotationX = compound.getInteger("RotationX");
        rotationY = compound.getInteger("RotationY");
        rotationZ = compound.getInteger("RotationZ");

        scaleX = compound.getFloat("ScaleX");
        scaleY = compound.getFloat("ScaleY");
        scaleZ = compound.getFloat("ScaleZ");

        if (scaleX <= 0)
            scaleX = 1;
        if (scaleY <= 0)
            scaleY = 1;
        if (scaleZ <= 0)
            scaleZ = 1;

        if (compound.hasKey("Text1"))
            text1.setNBT(compound.getCompoundTag("Text1"));
        if (compound.hasKey("Text2"))
            text2.setNBT(compound.getCompoundTag("Text2"));
        if (compound.hasKey("Text3"))
            text3.setNBT(compound.getCompoundTag("Text3"));
        if (compound.hasKey("Text4"))
            text4.setNBT(compound.getCompoundTag("Text4"));
        if (compound.hasKey("Text5"))
            text5.setNBT(compound.getCompoundTag("Text5"));
        if (compound.hasKey("Text6"))
            text6.setNBT(compound.getCompoundTag("Text6"));
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.BLOCK;
    }

    @Override
    public void requestData() {
        BlockScriptPacket.Get(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        BlockScriptPacket.Save(this.xCoord, this.yCoord, this.zCoord, nbt);
    }

    @Override
    public GuiDataResult setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            return new GuiDataResult(GuiDataKind.LOAD_COMPLETE, -1);
        }

        setNBT(compound);
        return new GuiDataResult(GuiDataKind.METADATA, -1);
    }

    @Override
    public void sync() {
        sendSavePacket(-1, getScripts().size(), getNBT(new NBTTagCompound()));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        getNBT(compound);
        getDisplayNBT(compound);
        timers.writeToNBT(compound);
    }

    public NBTTagCompound getNBT(NBTTagCompound compound) {
        compound.setTag("Scripts", NBTTags.NBTScript(scripts));
        compound.setString("ScriptLanguage", scriptLanguage);
        compound.setBoolean("ScriptEnabled", enabled);
        compound.setInteger("BlockPowering", powering);
        compound.setInteger("BlockPrevPower", prevPower);
        compound.setFloat("BlockHardness", blockHardness);
        compound.setFloat("BlockResistance", blockResistance);
        return compound;
    }

    public void getDisplayNBT(NBTTagCompound compound) {
        NBTTagCompound itemcompound = new NBTTagCompound();
        itemModel.writeToNBT(itemcompound);
        if (blockModel != null) {
            compound.setString("ScriptBlockModelBlock", Block.blockRegistry.getNameForObject(blockModel));
        }
        compound.setTag("ScriptBlockModel", itemcompound);
        compound.setInteger("LightValue", lightValue);
        compound.setBoolean("IsLadder", isLadder);
        compound.setBoolean("IsPassible", isPassible);

        compound.setInteger("RotationX", rotationX);
        compound.setInteger("RotationY", rotationY);
        compound.setInteger("RotationZ", rotationZ);

        compound.setFloat("ScaleX", scaleX);
        compound.setFloat("ScaleY", scaleY);
        compound.setFloat("ScaleZ", scaleZ);

        compound.setTag("Text1", text1.getNBT());
        compound.setTag("Text2", text2.getNBT());
        compound.setTag("Text3", text3.getNBT());
        compound.setTag("Text4", text4.getNBT());
        compound.setTag("Text5", text5.getNBT());
        compound.setTag("Text6", text6.getNBT());

    }

    private boolean isEnabled() {
        return enabled && ScriptController.HasStart && !worldObj.isRemote;
    }

    @Override
    public void updateEntity() {
        if (renderTileUpdate != null) {
            try {
                renderTileUpdate.tick();
            } catch (Exception e) {
                renderTileUpdate = null;
            }
        }
        ticksExisted++;
        if (prevPower != newPower && powering <= 0) {
            EventHooks.onScriptBlockRedstonePower(this, prevPower, newPower);
            prevPower = newPower;
        }

        timers.update();
        if (ticksExisted >= 10) {
            EventHooks.onScriptBlockUpdate(this);
            ticksExisted = 0;
            if (needsClientUpdate) {
                worldObj.func_147451_t(xCoord, yCoord, zCoord);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, CustomItems.scripted);

                Chunk chunk = worldObj.getChunkFromChunkCoords(xCoord >> 4, zCoord >> 4);
                chunk.setBlockMetadata(xCoord & 15, yCoord, zCoord & 15, metadata);
                blockMetadata = metadata;

                needsClientUpdate = false;
            }
        }

        if (worldObj.isRemote) {
            boolean markForUpdate = false;
            boolean prevHideModel = this.hideModel;
            boolean hideModel = BlockScriptedRenderer.overrideModel();

            if (hideModel != prevHideModel) {
                this.hideModel = hideModel;
                markForUpdate = true;
            }

            if (markForUpdate) {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.func_148857_g());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRenderInPass(int arg0) {
        if (blockModel != null && blockModel.isOpaqueCube()
            && !BlockScriptedRenderer.overrideModel()) {
            return true;
        }
        return super.shouldRenderInPass(arg0);
    }

    public void handleUpdateTag(NBTTagCompound tag) {
        int light = lightValue;
        setDisplayNBT(tag);
        if (light != lightValue)
            checkLight(worldObj, xCoord, yCoord, zCoord);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean checkLight(World world, int x, int y, int z) {
        boolean flag = false;

        if (!world.provider.hasNoSky) {
            flag |= world.updateLightByType(EnumSkyBlock.Sky, x, y, z);
        }

        flag = flag | world.updateLightByType(EnumSkyBlock.Block, x, y, z);
        return flag;
    }

    @Override
    public Packet getDescriptionPacket() {
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, getUpdateTag());
    }

    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("x", xCoord);
        compound.setInteger("y", yCoord);
        compound.setInteger("z", zCoord);
        getDisplayNBT(compound);
        return compound;
    }

    public void setItemModel(ItemStack item, Block b) {
        if (item == null || item.stackSize == 0) {
            item = new ItemStack(CustomItems.scripted);
        }
        if (NoppesUtilPlayer.compareItems(item, itemModel, false, false) && b != blockModel)
            return;

        metadata = item.getItemDamage();
        blockMetadata = metadata;

        itemModel = item;
        blockModel = b;
        needsClientUpdate = true;
    }

    public void setLightValue(int value) {
        if (value == lightValue)
            return;
        lightValue = ValueUtil.clamp(value, 0, 15);
        needsClientUpdate = true;
    }

    public void setRedstonePower(int strength) {
        if (powering == strength)
            return;
        //using activePowering to prevent the RedstonePower script event from going crazy
        prevPower = activePowering = ValueUtil.clamp(strength, 0, 15);
        worldObj.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
        powering = activePowering;
    }

    public void setScale(float x, float y, float z) {
        if (scaleX == x && scaleY == y && scaleZ == z)
            return;
        scaleX = ValueUtil.clamp(x, 0, 10);
        scaleY = ValueUtil.clamp(y, 0, 10);
        scaleZ = ValueUtil.clamp(z, 0, 10);
        needsClientUpdate = true;
    }

    public void setRotation(int x, int y, int z) {
        if (rotationX == x && rotationY == y && rotationZ == z)
            return;
        rotationX = ValueUtil.clamp(x, 0, 359);
        rotationY = ValueUtil.clamp(y, 0, 359);
        rotationZ = ValueUtil.clamp(z, 0, 359);
        needsClientUpdate = true;
    }

    @Override
    public void callScript(EnumScriptType type, Event event) {
        if (!isEnabled())
            return;
        if (ScriptController.Instance.lastLoaded > lastInited) {
            lastInited = ScriptController.Instance.lastLoaded;
            if (type != EnumScriptType.INIT)
                EventHooks.onScriptBlockInit(this);
        }

        for (IScriptUnit script : scripts) {
            script.run(type, event);
        }
    }

    @Override
    public void callScript(String hookName, Event event) {
        callScript(EnumScriptType.valueOf(hookName), event);
    }

    @Override
    public boolean isClient() {
        return worldObj.isRemote;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean bo) {
        enabled = bo;
    }

    @Override
    public String noticeString() {
        return "x: " + xCoord + ", y: " + yCoord + ", z: " + zCoord;
    }

    @Override
    public String getLanguage() {
        return scriptLanguage;
    }

    @Override
    public void setLanguage(String lang) {
        scriptLanguage = lang;
    }

    @Override
    public void setScripts(List<IScriptUnit> list) {
        scripts = list;
    }

    @Override
    public List<IScriptUnit> getScripts() {
        return scripts;
    }
    

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            .offset(xCoord, yCoord, zCoord);
    }

    public NBTTagCompound getTileData() {
        if (this.customTileData == null) {
            this.customTileData = new NBTTagCompound();
        }
        return this.customTileData;
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    public class TextPlane implements ITextPlane {
        public boolean textHasChanged = true;
        public TextBlock textBlock;

        public String text = "";
        public int rotationX = 0, rotationY = 0, rotationZ = 0;
        public float offsetX = 0, offsetY = 0f, offsetZ = 0.5f;
        public float scale = 1;

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setText(String text) {
            if (this.text.equals(text))
                return;
            this.text = text;
            textHasChanged = true;
            needsClientUpdate = true;
        }

        @Override
        public int getRotationX() {
            return rotationX;
        }

        @Override
        public int getRotationY() {
            return rotationY;
        }

        @Override
        public int getRotationZ() {
            return rotationZ;
        }

        @Override
        public void setRotationX(int x) {
            x = ValueUtil.clamp(x % 360, 0, 359);
            if (rotationX == x)
                return;
            rotationX = x;
            needsClientUpdate = true;
        }

        @Override
        public void setRotationY(int y) {
            y = ValueUtil.clamp(y % 360, 0, 359);
            if (rotationY == y)
                return;
            rotationY = y;
            needsClientUpdate = true;
        }

        @Override
        public void setRotationZ(int z) {
            z = ValueUtil.clamp(z % 360, 0, 359);
            if (rotationZ == z)
                return;
            rotationZ = z;
            needsClientUpdate = true;
        }

        @Override
        public float getOffsetX() {
            return offsetX;
        }

        @Override
        public float getOffsetY() {
            return offsetY;
        }

        @Override
        public float getOffsetZ() {
            return offsetZ;
        }

        @Override
        public void setOffsetX(float x) {
            x = ValueUtil.clamp(x, -1, 1);
            if (offsetX == x)
                return;
            offsetX = x;
            needsClientUpdate = true;
        }

        @Override
        public void setOffsetY(float y) {
            y = ValueUtil.clamp(y, -1, 1);
            if (offsetY == y)
                return;
            offsetY = y;
            needsClientUpdate = true;
        }

        @Override
        public void setOffsetZ(float z) {
            z = ValueUtil.clamp(z, -1, 1);
            if (offsetZ == z)
                return;
            offsetZ = z;
            needsClientUpdate = true;
        }

        @Override
        public float getScale() {
            return scale;
        }

        @Override
        public void setScale(float scale) {
            if (this.scale == scale)
                return;
            this.scale = scale;
            needsClientUpdate = true;
        }

        public NBTTagCompound getNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("Text", text);
            compound.setInteger("RotationX", rotationX);
            compound.setInteger("RotationY", rotationY);
            compound.setInteger("RotationZ", rotationZ);
            compound.setFloat("OffsetX", offsetX);
            compound.setFloat("OffsetY", offsetY);
            compound.setFloat("OffsetZ", offsetZ);
            compound.setFloat("Scale", scale);
            return compound;
        }

        public void setNBT(NBTTagCompound compound) {
            setText(compound.getString("Text"));
            rotationX = compound.getInteger("RotationX");
            rotationY = compound.getInteger("RotationY");
            rotationZ = compound.getInteger("RotationZ");
            offsetX = compound.getFloat("OffsetX");
            offsetY = compound.getFloat("OffsetY");
            offsetZ = compound.getFloat("OffsetZ");
            scale = compound.getFloat("Scale");
        }
    }
}
