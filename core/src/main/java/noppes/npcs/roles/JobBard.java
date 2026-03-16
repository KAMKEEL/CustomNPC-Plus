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
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumBardInstrument;
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
            mainhand = new IItemStack(CustomItems.banjo);
            overrideMainHand = overrideOffHand = true;
        }
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setString("BardSong", song);
        INbt.setInteger("BardMinRange", minRange);
        INbt.setInteger("BardMaxRange", maxRange);
        INbt.setInteger("BardInstrument", instrument.ordinal());
        INbt.setBoolean("BardStreamer", isStreamer);
        INbt.setBoolean("BardHasOff", hasOffRange);

        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        song = INbt.getString("BardSong");
        minRange = INbt.getInteger("BardMinRange");
        maxRange = INbt.getInteger("BardMaxRange");
        setInstrument(INbt.getInteger("BardInstrument"));
        isStreamer = INbt.getBoolean("BardStreamer");
        hasOffRange = INbt.getBoolean("BardHasOff");
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
                this.mainhand = new IItemStack(CustomItems.banjo);
                this.offhand = null;
                break;
            case Violin:
                this.mainhand = new IItemStack(CustomItems.violin);
                this.offhand = new IItemStack(CustomItems.violinbow);
                break;
            case Guitar:
                this.mainhand = new IItemStack(CustomItems.guitar);
                this.offhand = null;
                break;
            case Harp:
                this.mainhand = new IItemStack(CustomItems.harp);
                this.offhand = null;
                break;
            case FrenchHorn:
                this.mainhand = new IItemStack(CustomItems.frenchHorn);
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
            IPlayer player = CustomNpcs.proxy.getPlayer();
            double distanceToPlayer = npc.getDistanceToEntity(player);
            double distanceToMusic = MusicController.Instance.getDistance();
            if (Math.ceil(distanceToPlayer) < Math.ceil(distanceToMusic)) {
                if (!this.play()) return;
            }
        } else if (hasOffRange) {
            List<IPlayer> list = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(maxRange, maxRange, maxRange));
            if (!list.contains(CustomNpcs.proxy.getPlayer()))
                MusicController.Instance.stopMusic();
        }

        if (MusicController.Instance.isPlaying(song)) {
            Minecraft.getMinecraft().mcMusicTicker.field_147676_d = 12000;
        }
    }

    private boolean play() {
        List<IPlayer> list = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(minRange, minRange, minRange));
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
