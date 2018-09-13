package noppes.npcs;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileNpcContainer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.containers.ContainerCrate;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.containers.ContainerMerchantAdd;
import noppes.npcs.containers.ContainerNPCBankLarge;
import noppes.npcs.containers.ContainerNPCBankSmall;
import noppes.npcs.containers.ContainerNPCBankUnlock;
import noppes.npcs.containers.ContainerNPCBankUpgrade;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.entity.EntityNPCInterface;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	
	public boolean newVersionAvailable = false;
	public int revision = 4;

	public void load() {
		CustomNpcs.Channel.register(new PacketHandlerServer());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID > EnumGuiType.values().length)
			return null;
		EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
		EnumGuiType gui = EnumGuiType.values()[ID];
		return getContainer(gui, player, x, y, z, npc);
	}
	public Container getContainer(EnumGuiType gui,EntityPlayer player, int x, int y, int z,EntityNPCInterface npc){
		if(gui == EnumGuiType.MainMenuInv)
			return new ContainerNPCInv(npc, player);

		if(gui == EnumGuiType.PlayerBankSmall)
			return new ContainerNPCBankSmall(player, x , y);
		
		if(gui == EnumGuiType.PlayerBankUnlock)
			return new ContainerNPCBankUnlock(player, x , y);

		if(gui == EnumGuiType.PlayerBankUprade)
			return new ContainerNPCBankUpgrade(player, x , y);
		
		if(gui == EnumGuiType.PlayerBankLarge)
			return new ContainerNPCBankLarge(player, x , y);

		if(gui == EnumGuiType.PlayerFollowerHire)
			return new ContainerNPCFollowerHire(npc, player);
		
		if(gui == EnumGuiType.PlayerFollower)
			return new ContainerNPCFollower(npc, player);
		
		if(gui == EnumGuiType.PlayerTrader)
			return  new ContainerNPCTrader(npc, player);

		if(gui == EnumGuiType.PlayerAnvil)
			return new ContainerCarpentryBench(player.inventory, player.worldObj, x, y, z);
		
		if(gui == EnumGuiType.SetupItemGiver)
			return new ContainerNpcItemGiver(npc, player);
		
		if(gui == EnumGuiType.SetupTrader)
			return new ContainerNPCTraderSetup(npc, player);

		if(gui == EnumGuiType.SetupFollower)
			return new ContainerNPCFollowerSetup(npc, player);

		if(gui == EnumGuiType.QuestReward)
			return new ContainerNpcQuestReward(player);
		
		if(gui == EnumGuiType.QuestItem)
			return new ContainerNpcQuestTypeItem(player);
		
		if(gui == EnumGuiType.ManageRecipes)
			return new ContainerManageRecipes(player,x);

		if(gui == EnumGuiType.ManageBanks)
			return new ContainerManageBanks(player);

		if(gui == EnumGuiType.MerchantAdd)
			return new ContainerMerchantAdd(player, ServerEventsHandler.Merchant, player.worldObj);

		if(gui == EnumGuiType.Crate)
			return new ContainerCrate(player.inventory, (TileNpcContainer)player.worldObj.getTileEntity(x, y, z));
		
		if(gui == EnumGuiType.PlayerMailman)
			return new ContainerMail(player, x == 1, y == 1);
		
		if(gui == EnumGuiType.CompanionInv)
			return new ContainerNPCCompanion(npc, player);
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		// TODO Auto-generated method stub
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
		// TODO Auto-generated method stub
	}


	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
		
	}

	public void openGui(EntityPlayer player, Object guiscreen) {
		// TODO Auto-generated method stub
		
	}

	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
		// TODO Auto-generated method stub
		
	}

	public boolean hasClient() {
		return false;
	}

	public EntityPlayer getPlayer() {
		return null;
	}

	public void registerItem(Item item) {
		// TODO Auto-generated method stub
		
	}

	public ModelBiped getSkirtModel() {
		return null;
	}

	public void spawnParticle(String particle, double x, double y, double z,
			double motionX, double motionY, double motionZ, float scale) {
		
	}

}
