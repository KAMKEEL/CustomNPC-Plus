package noppes.npcs.scripted;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.api.*;

public class ScriptTileEntity<T extends TileEntity> implements ITileEntity {
    protected T tileEntity;
    protected IWorld world;

    public ScriptTileEntity(T tileEntity) {
        this.tileEntity = tileEntity;
        this.world = NpcAPI.Instance().getIWorld(tileEntity.getWorldObj());
    }

    public int getBlockMetadata(){
        return tileEntity.getBlockMetadata();
    }

    public IWorld getWorld(){
        return world;
    }

    public void setWorld(IWorld world) {
        this.tileEntity.setWorldObj(world.getMCWorld());
        this.world = world;
    }

    public TileEntity getMCTileEntity() {
        return this.tileEntity;
    }

    public void markDirty()
    {
        this.tileEntity.markDirty();
    }

    public void readFromNBT(INbt nbt){
        this.tileEntity.readFromNBT(nbt.getMCNBT());
    }

    public double getDistanceFrom(double x, double y, double z){
        return this.tileEntity.getDistanceFrom(x,y,z);
    }

    public double getDistanceFrom(IPos pos){
        return this.getDistanceFrom(pos.getX(),pos.getY(),pos.getZ());
    }

    public IBlock getBlockType(){
        return NpcAPI.Instance().getIBlock(world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    }

    public boolean isInvalid(){
        return tileEntity.isInvalid();
    }

    public void invalidate(){
        tileEntity.invalidate();
    }

    public void validate() {
        tileEntity.validate();
    }

    public void updateContainingBlockInfo(){
        tileEntity.updateContainingBlockInfo();
    }

    public INbt getNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        this.tileEntity.writeToNBT(compound);
        return NpcAPI.Instance().getINbt(compound);
    }
}
