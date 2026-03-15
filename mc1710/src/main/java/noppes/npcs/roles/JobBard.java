package noppes.npcs.roles;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.constants.EnumBardInstrument;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

public class JobBard extends JobInterface {
    public int minRange = 2;
    public int maxRange = 64;

    public boolean isStreamer = true;
    public boolean hasOffRange = true;

    public String song = "";

    private EnumBardInstrument instrument = EnumBardInstrument.Banjo;

    public JobBard(EntityNPCInterface npc) {
        super(npc);
        if (CustomItems.banjo != null) {
            mainhand = new ItemStack(CustomItems.banjo);
            overrideMainHand = overrideOffHand = true;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("BardSong", song);
        nbttagcompound.setInteger("BardMinRange", minRange);
        nbttagcompound.setInteger("BardMaxRange", maxRange);
        nbttagcompound.setInteger("BardInstrument", instrument.ordinal());
        nbttagcompound.setBoolean("BardStreamer", isStreamer);
        nbttagcompound.setBoolean("BardHasOff", hasOffRange);

        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        song = nbttagcompound.getString("BardSong");
        minRange = nbttagcompound.getInteger("BardMinRange");
        maxRange = nbttagcompound.getInteger("BardMaxRange");
        setInstrument(nbttagcompound.getInteger("BardInstrument"));
        isStreamer = nbttagcompound.getBoolean("BardStreamer");
        hasOffRange = nbttagcompound.getBoolean("BardHasOff");
    }

    public void setInstrument(int i) {
        if (CustomItems.banjo == null)
            return;
        instrument = EnumBardInstrument.values()[i];
        overrideMainHand = overrideOffHand = instrument != EnumBardInstrument.None;
        switch (instrument) {
            case None:
                this.mainhand = null;
                this.offhand = null;
                break;
            case Banjo:
                this.mainhand = new ItemStack(CustomItems.banjo);
                this.offhand = null;
                break;
            case Violin:
                this.mainhand = new ItemStack(CustomItems.violin);
                this.offhand = new ItemStack(CustomItems.violinbow);
                break;
            case Guitar:
                this.mainhand = new ItemStack(CustomItems.guitar);
                this.offhand = null;
                break;
            case Harp:
                this.mainhand = new ItemStack(CustomItems.harp);
                this.offhand = null;
                break;
            case FrenchHorn:
                this.mainhand = new ItemStack(CustomItems.frenchHorn);
                this.offhand = null;
                break;
        }
    }

    public EnumBardInstrument getInstrument() {
        return instrument;
    }

    public void onLivingUpdate() {
        if (!npc.isRemote() || song.isEmpty())
            return;

        if (!MusicController.Instance.isPlaying() && Minecraft.getMinecraft().currentScreen == null) {
            if (!this.play()) return;
        } else if (MusicController.Instance.getEntity() != npc) {
            EntityPlayer player = CustomNpcs.proxy.getPlayer();
            double distanceToPlayer = npc.getDistanceToEntity(player);
            double distanceToMusic = MusicController.Instance.getDistance();
            if (Math.ceil(distanceToPlayer) < Math.ceil(distanceToMusic)) {
                if (!this.play()) return;
            }
        } else if (hasOffRange) {
            List<EntityPlayer> list = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(maxRange, maxRange, maxRange));
            if (!list.contains(CustomNpcs.proxy.getPlayer()))
                MusicController.Instance.stopMusic();
        }

        if (MusicController.Instance.isPlaying(song)) {
            Minecraft.getMinecraft().mcMusicTicker.field_147676_d = 12000;
        }
    }

    private boolean play() {
        List<EntityPlayer> list = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(minRange, minRange, minRange));
        if (!list.contains(CustomNpcs.proxy.getPlayer()))
            return false;

        if (isStreamer)
            MusicController.Instance.playMusicJukebox(song, npc, hasOffRange ? maxRange : 0);
        else
            MusicController.Instance.playMusicBackground(song, npc, hasOffRange ? maxRange : 0);
        return true;
    }

    @Override
    public void killed() {
        delete();
    }

    @Override
    public void delete() {
        if (npc.worldObj.isRemote && hasOffRange) {
            if (MusicController.Instance.isPlaying(song)) {
                MusicController.Instance.stopAllSounds();
            }
        }
    }
}
