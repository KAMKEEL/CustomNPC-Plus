package noppes.npcs;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.blocks.tiles.TileBook;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.BankData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerFactionData;
import noppes.npcs.controllers.PlayerMail;
import noppes.npcs.controllers.PlayerMailData;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;

public class PacketHandlerPlayer{
	
	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
		ByteBuf buffer = event.packet.payload();
		try {
			player(buffer, player, EnumPlayerPacket.values()[buffer.readInt()]);
		} catch (IOException e) {
			LogWriter.except(e);
		}
	}

	private void player(ByteBuf buffer, EntityPlayerMP player, EnumPlayerPacket type) throws IOException {
		if(type == EnumPlayerPacket.CompanionTalentExp){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Companion || player != npc.getOwner())
				return;
			int id = buffer.readInt();
			int exp = buffer.readInt();
			RoleCompanion role = (RoleCompanion) npc.roleInterface;
			if(exp <= 0 || !role.canAddExp(-exp) || id < 0 || id >= EnumCompanionTalent.values().length) //should never happen unless hacking
				return;
			EnumCompanionTalent talent = EnumCompanionTalent.values()[id];
			role.addExp(-exp);
			role.addTalentExp(talent, exp);
		}
		else if(type == EnumPlayerPacket.CompanionOpenInv){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Companion || player != npc.getOwner())
				return;
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionInv, npc);
		}
		else if(type == EnumPlayerPacket.FollowerHire){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Follower)
				return;
			NoppesUtilPlayer.hireFollower(player,npc);
		}
		else if(type == EnumPlayerPacket.FollowerExtend){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Follower)
				return;
			NoppesUtilPlayer.extendFollower(player,npc);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPlayerPacket.FollowerState){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Follower)
				return;
			NoppesUtilPlayer.changeFollowerState(player,npc);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPlayerPacket.RoleGet){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role == EnumRoleType.None)
				return;
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPlayerPacket.Transport){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Transporter)
				return;
			NoppesUtilPlayer.transport(player, npc, Server.readString(buffer));
		}
		else if(type == EnumPlayerPacket.BankUpgrade){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Bank)
				return;
			NoppesUtilPlayer.bankUpgrade(player, npc);
		}
		else if(type == EnumPlayerPacket.BankUnlock){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Bank)
				return;
			NoppesUtilPlayer.bankUnlock(player, npc);
		}
		else if(type == EnumPlayerPacket.BankSlotOpen){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null || npc.advanced.role != EnumRoleType.Bank)
				return;
			int slot = buffer.readInt();
			int bankId = buffer.readInt();
			BankData data = PlayerDataController.instance.getBankData(player,bankId).getBankOrDefault(bankId);
			data.openBankGui(player, npc, bankId, slot);
		}
		else if(type == EnumPlayerPacket.Dialog){
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(npc == null)
				return;
			NoppesUtilPlayer.dialogSelected(buffer.readInt(),buffer.readInt(),player,npc);
		}
		else if(type == EnumPlayerPacket.CheckQuestCompletion){
			PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
			playerdata.checkQuestCompletion(player,null);
		}
		else if(type == EnumPlayerPacket.QuestLog){
			NoppesUtilPlayer.sendQuestLogData(player);
		}
		else if(type == EnumPlayerPacket.QuestCompletion){
			NoppesUtilPlayer.questCompletion(player,buffer.readInt());
		}
		else if(type == EnumPlayerPacket.FactionsGet){
			PlayerFactionData data = PlayerDataController.instance.getPlayerData(player).factionData;	
			Server.sendData(player, EnumPacketClient.GUI_DATA, data.getPlayerGuiData());
		}
		else if(type == EnumPlayerPacket.MailGet){
			PlayerMailData data = PlayerDataController.instance.getPlayerData(player).mailData;
			Server.sendData(player, EnumPacketClient.GUI_DATA, data.saveNBTData(new NBTTagCompound()));
		}
		else if(type == EnumPlayerPacket.MailDelete){
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			PlayerMailData data = PlayerDataController.instance.getPlayerData(player).mailData;
			
			Iterator<PlayerMail> it = data.playermail.iterator();
			while(it.hasNext()){
				PlayerMail mail = it.next();
				if(mail.time == time && mail.sender.equals(username)){
					it.remove();
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, data.saveNBTData(new NBTTagCompound()));
		}
		else if(type == EnumPlayerPacket.MailSend){
			if(!(player.openContainer instanceof ContainerMail))
				return;
			String username = PlayerDataController.instance.hasPlayer(Server.readString(buffer));
			if(username.isEmpty()){
				NoppesUtilServer.sendGuiError(player, 0);
				return;
			}
			
			PlayerMail mail = new PlayerMail();
            //String s = player.func_145748_c_().getFormattedText();
            String s = player.getDisplayName();
            if(!s.equals(player.getCommandSenderName()))
            	s += "(" + player.getCommandSenderName() + ")";
			mail.readNBT(Server.readNBT(buffer));
			mail.sender = s;
			mail.items = ((ContainerMail)player.openContainer).mail.items;

			if(mail.subject.isEmpty()){
				NoppesUtilServer.sendGuiError(player, 1);
				return;
			}
			PlayerDataController.instance.addPlayerMessage(username, mail);
			
			NBTTagCompound comp = new NBTTagCompound();
			comp.setString("username", username);
			NoppesUtilServer.sendGuiClose(player, 1,comp);
		}
		else if(type == EnumPlayerPacket.MailboxOpenMail){
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			player.closeContainer();
			PlayerMailData data = PlayerDataController.instance.getPlayerData(player).mailData;
			
			Iterator<PlayerMail> it = data.playermail.iterator();
			while(it.hasNext()){
				PlayerMail mail = it.next();
				if(mail.time == time && mail.sender.equals(username)){
					ContainerMail.staticmail = mail;
					player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.worldObj, 0, 0, 0);
					break;
				}
			}
		}
		else if(type == EnumPlayerPacket.MailRead){
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			PlayerMailData data = PlayerDataController.instance.getPlayerData(player).mailData;
			
			Iterator<PlayerMail> it = data.playermail.iterator();
			while(it.hasNext()){
				PlayerMail mail = it.next();
				if(mail.time == time && mail.sender.equals(username)){
					mail.beenRead = true;
					if(mail.hasQuest())
						PlayerQuestController.addActiveQuest(mail.getQuest(), player);
				}
			}
		}
		else if(type == EnumPlayerPacket.SignSave){
			int x = buffer.readInt(), y = buffer.readInt(), z = buffer.readInt();
			TileEntity tile = player.worldObj.getTileEntity(x, y, z);
			if(tile == null || !(tile instanceof TileBigSign))
				return;
			TileBigSign sign = (TileBigSign) tile;
			if(sign.canEdit){
				sign.setText(Server.readString(buffer));
				sign.canEdit = false;
				player.worldObj.markBlockForUpdate(x, y, z);
			}
		}
		else if(type == EnumPlayerPacket.SaveBook){
			int x = buffer.readInt(), y = buffer.readInt(), z = buffer.readInt();
			TileEntity tileentity = player.worldObj.getTileEntity(x, y, z);
			if(!(tileentity instanceof TileBook))
				return;
			TileBook tile = (TileBook) tileentity;
			if(tile.book.getItem() == Items.written_book)
				return;
			boolean sign = buffer.readBoolean();
			ItemStack book = ItemStack.loadItemStackFromNBT(Server.readNBT(buffer));
			if(book == null)
				return;
			if(book.getItem() == Items.writable_book && !sign && ItemWritableBook.func_150930_a(book.getTagCompound())){
				tile.book.setTagInfo("pages", book.getTagCompound().getTagList("pages", 8));
			}
			if(book.getItem() == Items.written_book && sign && ItemEditableBook.validBookTagContents(book.getTagCompound())){
				tile.book.setTagInfo("author", new NBTTagString(player.getCommandSenderName()));
				tile.book.setTagInfo("title", new NBTTagString(book.getTagCompound().getString("title")));
                tile.book.setTagInfo("pages", book.getTagCompound().getTagList("pages", 8));
                tile.book.func_150996_a(Items.written_book);
			}
		}
	}
}
