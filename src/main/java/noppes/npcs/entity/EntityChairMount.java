package noppes.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityChairMount extends Entity{

	public EntityChairMount(World world) {
		super(world);
		setSize(0, 0);
	}
	
    public double getMountedYOffset(){
        return 0.5f;
    }

	@Override
	protected void entityInit() {
	}
	
    public void onEntityUpdate(){
    	super.onEntityUpdate();
    	if(this.worldObj != null && !this.worldObj.isRemote && riddenByEntity == null)
    		isDead = true;
    }
    
    @Override
    public boolean isEntityInvulnerable(){
        return true;
    }
    
    @Override
    public boolean isInvisible(){
        return true;
    }
    @Override
    public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_){
    	
    }
	@Override
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		
	}

	@Override
    public boolean canBeCollidedWith(){
        return false;
    }

	@Override
    public boolean canBePushed(){
        return false;
    }

	@Override
    protected void fall(float p_70069_1_){
    	
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_){
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
    }
}
