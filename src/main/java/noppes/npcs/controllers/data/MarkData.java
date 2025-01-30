package noppes.npcs.controllers.data;

import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.client.MarkDataPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.constants.MarkType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

public class MarkData implements IExtendedEntityProperties {

    private static final String CNPCMARK = "cnpcmark";
	private EntityLivingBase entity;

	public List<Mark> marks = new ArrayList<Mark>();

	public void setNBT(NBTTagCompound compound){
		List<Mark> marks = new ArrayList<Mark>();
		NBTTagList list = compound.getTagList("marks", 10);
		for(int i = 0; i < list.tagCount(); i++){
			NBTTagCompound c = list.getCompoundTagAt(i);
			Mark m = new Mark();
			m.type = c.getInteger("type");
			m.color = c.getInteger("color");
			m.availability.readFromNBT(c.getCompoundTag("availability"));
			marks.add(m);
		}
		this.marks = marks;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(Mark m : marks){
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("type", m.type);
			c.setInteger("color", m.color);
			c.setTag("availability", m.availability.writeToNBT(new NBTTagCompound()));
			list.appendTag(c);
		}
		compound.setTag("marks", list);
		return compound;
	}

    public static boolean has(EntityLivingBase entity) {
        return entity.getExtendedProperties(CNPCMARK) != null;
    }

    public static MarkData get(EntityNPCInterface npc){
        if(!MarkData.has(npc)){
            npc.registerExtendedProperties(CNPCMARK, new MarkData());
        }

        MarkData data = (MarkData) npc.getExtendedProperties(CNPCMARK);
        if(data.entity == null){
            data.entity = npc;
            data.setNBT(npc.getEntityData().getCompoundTag(CNPCMARK));
        }
        return data;
    }

    public IMark addMark(int type) {
        Mark m = new Mark();
        m.type = type;
        marks.add(m);
        if (!entity.worldObj.isRemote)
            syncClients();
        return m;
    }

    public IMark addMark(int type, int color) {
        Mark m = new Mark();
        m.type = type;
        m.color = color;
        marks.add(m);
        if (!entity.worldObj.isRemote)
            syncClients();
        return m;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        if (!(this.entity instanceof EntityNPCInterface)) {
            return;
        }
        this.entity.getEntityData().setTag(CNPCMARK, this.getNBT());
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {}

    @Override
    public void init(Entity entity, World world) {}

    public void syncClients() {
        PacketHandler.Instance.sendToAll(new MarkDataPacket(entity.getEntityId(), getNBT()));
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
