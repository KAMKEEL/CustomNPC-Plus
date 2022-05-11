package noppes.npcs.scripted;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import noppes.npcs.scripted.interfaces.INbt;
import noppes.npcs.scripted.interfaces.ITileEntity;
import noppes.npcs.scripted.wrapper.NpcAPI;

public class ScriptTileEntity<T extends TileEntity> implements ITileEntity {
    protected T tileEntity;

    public ScriptTileEntity(T tileEntity) { this.tileEntity = tileEntity; }

    public int getBlockMetadata(){
        return tileEntity.getBlockMetadata();
    }

    public ScriptWorld getWorld(){
        return (ScriptWorld) NpcAPI.Instance().getIWorld((WorldServer) tileEntity.getWorldObj());
    }

    public TileEntity getMCTileEntity() {
        return this.tileEntity;
    }

    public void readFromNBT(INbt nbt){
        this.tileEntity.readFromNBT(nbt.getMCNBT());
    }

    public double getDistanceFrom(double x, double y, double z){
        return this.tileEntity.getDistanceFrom(x,y,z);
    }

    public ScriptBlock getBlockType(){
        return new ScriptBlock(tileEntity.getWorldObj(), tileEntity.getBlockType(), new BlockPos(tileEntity.xCoord,tileEntity.yCoord,tileEntity.zCoord));
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
