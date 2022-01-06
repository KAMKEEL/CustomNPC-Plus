package noppes.npcs.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.Quest;

@Cancelable
public class PlayerCompleteQuestEvent extends Event {

    private EntityPlayer entityPlayer;
    private Quest quest;

    public PlayerCompleteQuestEvent(EntityPlayer player, Quest quest) {
        this.entityPlayer = player;
        this.quest = quest;
    }

    public EntityPlayer getPlayer() {
        return entityPlayer;
    }

    public Quest getQuest() {
        return quest;
    }

}
