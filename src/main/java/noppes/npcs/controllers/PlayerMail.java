package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;

public class PlayerMail implements IInventory{
	public String subject = "";
	public String sender = "";
	public NBTTagCompound message = new NBTTagCompound();
	public long time = 0;
	public boolean beenRead = false;
	public int questId = -1;
	public String questTitle = "";
	public ItemStack[] items = new ItemStack[4];
	
	public long timePast;
	
	public void readNBT(NBTTagCompound compound) {
		subject = compound.getString("Subject");
		sender = compound.getString("Sender");
		time = compound.getLong("Time");
		beenRead = compound.getBoolean("BeenRead");
		message = compound.getCompoundTag("Message");
		timePast = compound.getLong("TimePast");
		if(compound.hasKey("MailQuest"))
			questId = compound.getInteger("MailQuest");
		questTitle = compound.getString("MailQuestTitle");

        this.items = new ItemStack[this.getSizeInventory()];

        NBTTagList nbttaglist = compound.getTagList("MailItems", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.items.length)
            {
                this.items[j] = NoppesUtilServer.readItem(nbttagcompound1);
            }
        }
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("Subject", subject);
		compound.setString("Sender", sender);
		compound.setLong("Time", time);
		compound.setBoolean("BeenRead", beenRead);
		compound.setTag("Message", message);
		compound.setLong("TimePast", System.currentTimeMillis() - time);
		compound.setInteger("MailQuest", questId);

		if(hasQuest())
			compound.setString("MailQuestTitle", getQuest().title);

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.items.length; ++i)
        {
            if (this.items[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
        		NoppesUtilServer.writeItem(this.items[i], nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("MailItems", nbttaglist);
		return compound;
	}
	
	public boolean isValid(){
		return !subject.isEmpty() && !message.hasNoTags() && !sender.isEmpty();
	}

	public boolean hasQuest() {
		return getQuest() != null;
	}
	public Quest getQuest() {
		return  QuestController.instance != null?QuestController.instance.quests.get(questId):null;
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
	public ItemStack getStackInSlot(int var1) {
        return this.items[var1];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2) {
        if (this.items[par1] != null)
        {
            ItemStack itemstack;

            if (this.items[par1].stackSize <= par2)
            {
                itemstack = this.items[par1];
                this.items[par1] = null;
                this.markDirty();
                return itemstack;
            }
            else
            {
                itemstack = this.items[par1].splitStack(par2);

                if (this.items[par1].stackSize == 0)
                {
                    this.items[par1] = null;
                }

                this.markDirty();
                return itemstack;
            }
        }
        else
        {
            return null;
        }
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
        if (this.items[var1] != null)
        {
            ItemStack itemstack = this.items[var1];
            this.items[var1] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
        this.items[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
	}

	@Override
	public String getInventoryName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void openInventory() {
		
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	public PlayerMail copy() {
		PlayerMail mail = new PlayerMail();
		mail.readNBT(writeNBT());
		return mail;
	}
}
