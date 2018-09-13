package noppes.npcs.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;



public class ItemNpcMovingPath extends Item{
    public ItemNpcMovingPath(){
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		if(par2World.isRemote || !CustomNpcsPermissions.Instance.hasPermission(par3EntityPlayer, CustomNpcsPermissions.TOOL_MOUNTER))
			return par1ItemStack;
		EntityNPCInterface npc = getNpc(par1ItemStack, par2World);
		if(npc != null)
			NoppesUtilServer.sendOpenGui(par3EntityPlayer, EnumGuiType.MovingPath, npc);
        return par1ItemStack;
    }
    
	@Override
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer player, World par3World, int x, int y, int z, int par7, float par8, float par9, float par10){		
		if(par3World.isRemote || !CustomNpcsPermissions.Instance.hasPermission(player, CustomNpcsPermissions.TOOL_MOUNTER))
			return false;
		EntityNPCInterface npc = getNpc(par1ItemStack, par3World);
		if(npc == null)
			return true;
		List<int[]> list = npc.ai.getMovingPath();
		int[] pos = list.get(list.size() - 1);
		list.add(new int[]{x,y,z});
		
        double d3 = x - pos[0];
        double d4 = y - pos[1];
        double d5 = z - pos[2];
        double distance = (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
		
		player.addChatMessage(new ChatComponentText("Added point x:" + x + " y:"+ y + " z:" + z + " to npc " + npc.getCommandSenderName()));
        if(distance > CustomNpcs.NpcNavRange)
        	player.addChatMessage(new ChatComponentText("Warning: point is too far away from previous point. Max block walk distance = " + CustomNpcs.NpcNavRange));
		
		return true;
    }
	
	private EntityNPCInterface getNpc(ItemStack item, World world){
		if(world.isRemote || item.stackTagCompound == null)
			return null;
		
		Entity entity = world.getEntityByID(item.stackTagCompound.getInteger("NPCID"));
		if(entity == null || !(entity instanceof EntityNPCInterface))
			return null;
		
		return (EntityNPCInterface) entity;
	}
	
    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
		return 0x8B4513;
    }

    @Override
    public boolean requiresMultipleRenderPasses(){
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        this.itemIcon = Items.iron_sword.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name){
		GameRegistry.registerItem(this, name);
    	return super.setUnlocalizedName(name);
    }
}
