package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IQuestDialog;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.*;

import static noppes.npcs.PacketHandlerServer.sendInviteData;

public class QuestDialog extends QuestInterface implements IQuestDialog {

	public HashMap<Integer,Integer> dialogs = new HashMap<Integer,Integer>();

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		dialogs = NBTTags.getIntegerIntegerMap(compound.getTagList("QuestDialogs", 10));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("QuestDialogs", NBTTags.nbtIntegerIntegerMap(dialogs));
	}

	@Override
	public boolean isCompleted(PlayerData playerData) {
		for(int dialogId : dialogs.values())
			if(!playerData.dialogData.dialogsRead.contains(dialogId))
				return false;
		return true;
	}

	@Override
	public void handleComplete(EntityPlayer player) {
		super.handleComplete(player);
	}

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();
		for(int dialogId : dialogs.values()){
			Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
			if(dialog == null)
				continue;
			String title = dialog.title;
			if(PlayerDataController.Instance.getPlayerData(player).dialogData.dialogsRead.contains(dialogId))
				title += " (read)";
			else
				title += " (unread)";
			vec.add(title);
		}

		return vec;
	}

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList();

		for(int i = 0; i < 3; ++i) {
			if (this.dialogs.containsKey(i)) {
				Dialog dialog = (Dialog)DialogController.Instance.dialogs.get(this.dialogs.get(i));
				if (dialog != null) {
					list.add(new noppes.npcs.quests.QuestDialog.QuestDialogObjective(this, player, dialog));
				}
			}
		}

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
	}

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList();

        for(int i = 0; i < 3; ++i) {
            if (this.dialogs.containsKey(i)) {
                Dialog dialog = (Dialog)DialogController.Instance.dialogs.get(this.dialogs.get(i));
                if (dialog != null) {
                    list.add(new noppes.npcs.quests.QuestDialog.QuestDialogObjective(this, party, dialog));
                }
            }
        }

        return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        for(int dialogId : dialogs.values()){
            Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
            if(dialog == null)
                continue;

            ArrayList<String> unread = new ArrayList<>();
            for (UUID uuid: party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                PlayerData playerData;
                if(player != null){
                    playerData = PlayerDataController.Instance.getPlayerData(player);
                }
                else {
                    playerData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                }
                if(playerData != null){
                    if(!playerData.dialogData.dialogsRead.contains(dialogId))
                        unread.add(playerData.playername);
                }
            }

            String title = dialog.title;
            if(!unread.isEmpty()){
                title += " (unread)";
            }
            else {
                title += " (read)";
            }
            vec.add(title);

            if(!unread.isEmpty()){
                StringBuilder unreaders = new StringBuilder("> ");
                for(String name : unread){
                    unreaders.append(" ").append(name);
                }
                vec.add(unreaders.toString());
            }
        }

        return vec;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if(party == null)
            return false;

        for (UUID uuid: party.getPlayerUUIDs()) {
            EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
            PlayerData playerData;
            if(player != null){
                playerData = PlayerDataController.Instance.getPlayerData(player);
            }
            else {
                playerData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
            }
            if(playerData != null){
                for(int dialogId : dialogs.values()){
                    if(!playerData.dialogData.dialogsRead.contains(dialogId)){
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    class QuestDialogObjective implements IQuestObjective {
		private final QuestDialog parent;
		private final EntityPlayer player;
        private final Party party;
		private final Dialog dialog;

		public QuestDialogObjective(QuestDialog this$0, EntityPlayer player, Dialog dialog) {
			this.parent = this$0;
			this.player = player;
			this.dialog = dialog;
            this.party = null;
		}

        public QuestDialogObjective(QuestDialog this$0, Party party, Dialog dialog) {
            this.parent = this$0;
            this.party = party;
            this.dialog = dialog;
            this.player = null;
        }

		public int getProgress() {
			return this.isCompleted() ? 1 : 0;
		}

		public void setProgress(int progress) {
			if (progress >= 0 && progress <= 1) {
				PlayerData data = PlayerDataController.Instance.getPlayerData(player);
				boolean completed = data.dialogData.dialogsRead.contains(this.dialog.id);
				if (progress == 0 && completed) {
					data.dialogData.dialogsRead.remove(this.dialog.id);
					data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
					data.save();
				}

				if (progress == 1 && !completed) {
					data.dialogData.dialogsRead.add(this.dialog.id);
					data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
					data.save();
				}

			} else {
				throw new CustomNPCsException("Progress has to be 0 or 1", new Object[0]);
			}
		}

		public int getMaxProgress() {
			return 1;
		}

		public boolean isCompleted() {
			PlayerData data = PlayerDataController.Instance.getPlayerData(player);
			return data.dialogData.dialogsRead.contains(this.dialog.id);
		}

		public String getText() {
			return this.dialog.title + (this.isCompleted() ? " (read)" : " (unread)");
		}

        @Override
        public String getAdditionalText() {
            return null;
        }
    }

}
