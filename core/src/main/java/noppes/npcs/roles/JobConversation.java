package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JobConversation extends JobInterface {
    public Availability availability = new Availability();

    private ArrayList<String> names = new ArrayList<String>();
    private HashMap<String, EntityNPCInterface> npcs = new HashMap<String, EntityNPCInterface>();

    public HashMap<Integer, ConversationLine> lines = new HashMap<Integer, ConversationLine>();

    public int quest = -1;
    public String questTitle = "";
    public int generalDelay = 400;
    public int ticks = 100;
    public int range = 20;

    private ConversationLine nextLine;

    private boolean hasStarted = false;
    private int startedTicks = 20;
    public int mode = 0; //0:Always, 1:Player near

    public JobConversation(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt compound) {
        compound.setTag("ConversationAvailability", availability.writeToNBT(new INbt()));
        compound.setInteger("ConversationQuest", quest);
        compound.setInteger("ConversationDelay", generalDelay);
        compound.setInteger("ConversationRange", range);
        compound.setInteger("ConversationMode", mode);

        INbtList INbtList = new INbtList();
        for (int slot : lines.keySet()) {
            ConversationLine line = lines.get(slot);
            INbt INbt = new INbt();
            INbt.setInteger("Slot", slot);
            line.writeEntityToNBT(INbt);

            INbtList.appendTag(INbt);
        }

        compound.setTag("ConversationLines", INbtList);
        if (hasQuest())
            compound.setString("ConversationQuestTitle", getQuest().title);

        return compound;
    }

    @Override
    public void readFromNBT(INbt compound) {
        names.clear();
        availability.readFromNBT(compound.getCompoundTag("ConversationAvailability"));
        quest = compound.getInteger("ConversationQuest");
        generalDelay = compound.getInteger("ConversationDelay");
        questTitle = compound.getString("ConversationQuestTitle");
        range = compound.getInteger("ConversationRange");
        mode = compound.getInteger("ConversationMode");

        INbtList INbtList = compound.getTagList("ConversationLines", 10);
        HashMap<Integer, ConversationLine> map = new HashMap<Integer, ConversationLine>();
        for (int i = 0; i < INbtList.tagCount(); i++) {
            INbt INbt = INbtList.getCompoundTagAt(i);
            ConversationLine line = new ConversationLine();
            line.readEntityFromNBT(INbt);
            if (!line.npc.isEmpty() && !names.contains(line.npc.toLowerCase()))
                names.add(line.npc.toLowerCase());

            map.put(INbt.getInteger("Slot"), line);
        }
        lines = map;
        ticks = generalDelay;
    }

    public boolean hasQuest() {
        return getQuest() != null;
    }

    public Quest getQuest() {
        if (npc.isRemote())
            return null;
        return QuestController.Instance.quests.get(quest);
    }

    @Override
    public void aiUpdateTask() {
        ticks--;
        if (ticks > 0 || nextLine == null)
            return;
        say(nextLine);
        boolean seenNext = false;
        ConversationLine compare = nextLine;
        nextLine = null;
        for (ConversationLine line : lines.values()) {
            if (line.isEmpty())
                continue;
            if (seenNext) {
                nextLine = line;
                break;
            }
            if (line == compare) {
                seenNext = true;
            }
        }
        if (nextLine != null)
            ticks = nextLine.delay;
        else if (hasQuest()) {
            List<IPlayer> inRange = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(range, range, range));

            for (IPlayer player : inRange) {
                if (availability.isAvailable(player))
                    PlayerQuestController.addActiveQuest(new QuestData(getQuest()), player);
            }
        }
    }


    @Override
    public boolean aiShouldExecute() {
        if (lines.isEmpty() || npc.isKilled() || npc.isAttacking() || !shouldRun())
            return false;
        if (!hasStarted && mode == 1) {
            if (startedTicks-- > 0)
                return false;
            startedTicks = 10;
            if (npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(range, range, range)).isEmpty()) {
                return false;
            }
        }

        for (ConversationLine line : lines.values()) {
            if (line == null || line.isEmpty())
                continue;
            nextLine = line;
            break;
        }
        return nextLine != null;
    }

    private boolean shouldRun() {
        ticks--;
        if (ticks > 0)
            return false;
        npcs.clear();
        List<EntityNPCInterface> list = npc.worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, npc.boundingBox.expand(10, 10, 10));
        for (EntityNPCInterface npc : list) {
            if (!npc.isKilled() && !npc.isAttacking() && names.contains(npc.getCommandSenderName().toLowerCase()))
                npcs.put(npc.getCommandSenderName().toLowerCase(), npc);
        }
        boolean bo = names.size() == npcs.size();
        if (!bo)
            ticks = 20;
        return bo;
    }

    @Override
    public boolean aiContinueExecute() {
        for (EntityNPCInterface npc : npcs.values()) {
            if (npc.isKilled() || npc.isAttacking())
                return false;
        }
        return nextLine != null;
    }

    @Override
    public void resetTask() {
        nextLine = null;
        ticks = generalDelay;
        hasStarted = false;
    }

    @Override
    public void aiStartExecuting() {
        startedTicks = 20;
        hasStarted = true;
    }

    private void say(ConversationLine line) {
        List<IPlayer> inRange = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(range, range, range));

        EntityNPCInterface npc = npcs.get(line.npc.toLowerCase());
        if (npc == null)
            return;
        for (IPlayer player : inRange) {
            if (availability.isAvailable(player))
                npc.say(player, line);
        }
    }

    @Override
    public void reset() {
        hasStarted = false;
        resetTask();
        ticks = 60;
    }

    @Override
    public void killed() {
        reset();
    }

    public class ConversationLine extends Line {
        public String npc = "";
        public int delay = 40;

        public void writeEntityToNBT(INbt compound) {
            compound.setString("Line", text);
            compound.setString("Npc", npc);
            compound.setString("Sound", sound);
            compound.setInteger("Delay", delay);
        }

        public void readEntityFromNBT(INbt compound) {
            text = compound.getString("Line");
            npc = compound.getString("Npc");
            sound = compound.getString("Sound");
            delay = compound.getInteger("Delay");
        }

        public boolean isEmpty() {
            return npc.isEmpty() || text.isEmpty();
        }
    }

    public ConversationLine getLine(int slot) {
        if (lines.containsKey(slot))
            return lines.get(slot);
        ConversationLine line = new ConversationLine();
        lines.put(slot, line);
        return line;
    }
}
