package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IPlayerMail;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.QuestController;
import java.util.ArrayList;

public class PlayerMail implements IInventory, IPlayerMail {
    public String subject = "";
    public String sender = "";
    public INbt message = new INbt();
    public long time = 0;
    public boolean beenRead = false;
    public int questId = -1;
    public String questTitle = "";
    public IItemStack[] items = new IItemStack[4];

    public long timePast;

    public void readNBT(INbt compound) {
        subject = compound.getString("Subject");
        sender = compound.getString("Sender");
        time = compound.getLong("Time");
        beenRead = compound.getBoolean("BeenRead");
        message = compound.getCompoundTag("Message");
        timePast = compound.getLong("TimePast");
        if (compound.hasKey("MailQuest"))
            questId = compound.getInteger("MailQuest");
        questTitle = compound.getString("MailQuestTitle");

        this.items = new IItemStack[this.getSizeInventory()];

        INbtList INbtList = compound.getTagList("MailItems", 10);
        for (int i = 0; i < INbtList.tagCount(); ++i) {
            INbt nbttagcompound1 = INbtList.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.items.length) {
                this.items[j] = NoppesUtilServer.readItem(nbttagcompound1);
            }
        }
    }

    public INbt writeNBT() {
        INbt compound = new INbt();
        compound.setString("Subject", subject);
        compound.setString("Sender", sender);
        compound.setLong("Time", time);
        compound.setBoolean("BeenRead", beenRead);
        compound.setTag("Message", message);
        compound.setLong("TimePast", System.currentTimeMillis() - time);
        compound.setInteger("MailQuest", questId);

        if (hasQuest())
            compound.setString("MailQuestTitle", getQuest().title);

        INbtList INbtList = new INbtList();

        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] != null) {
                INbt nbttagcompound1 = new INbt();
                nbttagcompound1.setByte("Slot", (byte) i);
                NoppesUtilServer.writeItem(this.items[i], nbttagcompound1);
                INbtList.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("MailItems", INbtList);
        return compound;
    }

    public boolean isValid() {
        return !subject.isEmpty() && !message.hasNoTags() && !sender.isEmpty();
    }

    public boolean hasQuest() {
        return getQuest() != null;
    }

    public Quest getQuest() {
        return QuestController.Instance != null ? QuestController.Instance.quests.get(questId) : null;
    }

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public IItemStack getStackInSlot(int var1) {
        return this.items[var1];
    }

    @Override
    public IItemStack decrStackSize(int par1, int par2) {
        if (this.items[par1] != null) {
            IItemStack IItemStack;

            if (this.items[par1].stackSize <= par2) {
                IItemStack = this.items[par1];
                this.items[par1] = null;
                this.markDirty();
                return IItemStack;
            } else {
                IItemStack = this.items[par1].splitStack(par2);

                if (this.items[par1].stackSize == 0) {
                    this.items[par1] = null;
                }

                this.markDirty();
                return IItemStack;
            }
        } else {
            return null;
        }
    }

    @Override
    public IItemStack getStackInSlotOnClosing(int var1) {
        if (this.items[var1] != null) {
            IItemStack IItemStack = this.items[var1];
            this.items[var1] = null;
            return IItemStack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int par1, IItemStack par2ItemStack) {
        this.items[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit()) {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getInventoryName() {

        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {

        return false;
    }

    @Override
    public void markDirty() {


    }

    @Override
    public boolean isUseableByPlayer(IPlayer var1) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int var1, IItemStack var2) {
        return true;
    }

    public PlayerMail copy() {
        PlayerMail mail = new PlayerMail();
        mail.readNBT(writeNBT());
        return mail;
    }

    public void setPageText(String[] pages) {
        INbtList bookPages = new INbtList();
        for (String text : pages) {
            bookPages.appendTag(new NBTTagString(text));
        }

        message.setTag("pages", bookPages);
    }

    public String[] getPageText() {
        INbtList bookPages = new INbtList();

        if (message.hasKey("pages"))
            bookPages = message.getTagList("pages", 8);

        if (bookPages != null) {
            bookPages = (INbtList) bookPages.copy();
            ArrayList<String> pageStrings = new ArrayList<>();

            for (int i = 0; i < bookPages.tagCount(); i++) {
                pageStrings.add(bookPages.getStringTagAt(i));
            }

            return pageStrings.toArray(new String[0]);
        } else {
            return new String[]{""};
        }
    }

    public int getPageCount() {
        INbtList bookPages = new INbtList();

        if (message.hasKey("pages"))
            bookPages = message.getTagList("pages", 8);

        int bookTotalPages = 0;
        if (bookPages != null) {
            bookPages = (INbtList) bookPages.copy();
            bookTotalPages = bookPages.tagCount();

            if (bookTotalPages < 1) {
                bookTotalPages = 1;
            }

            return bookTotalPages;
        }

        return 0;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public long getTimePast() {
        return timePast;
    }

    public long getTimeSent() {
        return time;
    }

    public IItemStack[] getItems() {
        ArrayList<IItemStack> list = new ArrayList<>();
        for (IItemStack IItemStack : items) {
            list.add(NpcAPI.Instance().getIItemStack(IItemStack));
        }

        return list.toArray(new IItemStack[0]);
    }

    public void setItems(IItemStack[] items) {
        ArrayList<IItemStack> list = new ArrayList<>();
        for (IItemStack IItemStack : items) {
            list.add(IItemStack.getMCItemStack());
        }

        this.items = list.toArray(new IItemStack[0]);
    }
}
