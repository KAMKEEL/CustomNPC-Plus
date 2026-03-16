package noppes.npcs.controllers.data;

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
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.constants.MarkType;
import java.util.ArrayList;
import java.util.List;

public class MarkData implements IExtendedEntityProperties {

    private static final String CNPCMARK = "cnpcmark";
    private IEntityLivingBase IEntity;

    public List<Mark> marks = new ArrayList<Mark>();

    public void setNBT(INbt compound) {
        List<Mark> marks = new ArrayList<Mark>();
        INbtList list = compound.getTagList("marks", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            INbt c = list.getCompoundTagAt(i);
            Mark m = new Mark();
            m.type = c.getInteger("type");
            m.color = c.getInteger("color");
            m.availability.readFromNBT(c.getCompoundTag("availability"));
            marks.add(m);
        }
        this.marks = marks;
    }

    public INbt getNBT() {
        INbt compound = new INbt();
        INbtList list = new INbtList();
        for (Mark m : marks) {
            INbt c = new INbt();
            c.setInteger("type", m.type);
            c.setInteger("color", m.color);
            c.setTag("availability", m.availability.writeToNBT(new INbt()));
            list.appendTag(c);
        }
        compound.setTag("marks", list);
        return compound;
    }

    public static boolean has(IEntityLivingBase IEntity) {
        return IEntity.getExtendedProperties(CNPCMARK) != null;
    }

    public static MarkData get(EntityNPCInterface npc) {
        if (!MarkData.has(npc)) {
            npc.registerExtendedProperties(CNPCMARK, new MarkData());
        }

        MarkData data = (MarkData) npc.getExtendedProperties(CNPCMARK);
        if (data.IEntity == null) {
            data.IEntity = npc;
            data.setNBT(npc.getEntityData().getCompoundTag(CNPCMARK));
        }
        return data;
    }

    public IMark addMark(int type) {
        Mark m = new Mark();
        m.type = type;
        marks.add(m);
        if (!IEntity.worldObj.isRemote)
            syncClients();
        return m;
    }

    public IMark addMark(int type, int color) {
        Mark m = new Mark();
        m.type = type;
        m.color = color;
        marks.add(m);
        if (!IEntity.worldObj.isRemote)
            syncClients();
        return m;
    }

    @Override
    public void saveNBTData(INbt compound) {
        if (!(this.IEntity instanceof EntityNPCInterface)) {
            return;
        }
        this.IEntity.getEntityData().setTag(CNPCMARK, this.getNBT());
    }

    @Override
    public void loadNBTData(INbt compound) {
    }

    @Override
    public void init(IEntity IEntity, IWorld IWorld) {
    }

    public void syncClients() {
        PacketHandler.Instance.sendToAll(new MarkDataPacket(IEntity.getEntityId(), getNBT()));
    }

    public class Mark implements IMark {
        public int type = MarkType.NONE;
        public Availability availability = new Availability();
        public int color = 0xFFED51;

        @Override
        public IAvailability getAvailability() {
            return availability;
        }

        @Override
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public void setType(int type) {
            this.type = type;
        }

        @Override
        public void update() {
            MarkData.this.syncClients();
        }
    }
}
