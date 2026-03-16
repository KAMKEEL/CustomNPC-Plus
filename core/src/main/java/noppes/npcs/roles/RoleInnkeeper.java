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
import noppes.npcs.controllers.data.InnDoorData;
import java.util.HashMap;

public class RoleInnkeeper extends RoleInterface {

    private String innName = "Inn";
    private HashMap<String, InnDoorData> doors = new HashMap<String, InnDoorData>();

    public RoleInnkeeper(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setString("InnName", innName);
        INbt.setTag("InnDoors", nbtInnDoors(doors));
        return INbt;
    }

    private INbtBase nbtInnDoors(HashMap<String, InnDoorData> doors1) {
        INbtList INbtList = new INbtList();
        if (doors1 == null)
            return INbtList;
        HashMap<String, InnDoorData> doors2 = doors1;
        for (String name : doors2.keySet()) {
            InnDoorData door = doors2.get(name);
            if (door == null)
                continue;
            INbt INbt = new INbt();
            INbt.setString("Name", name);
            INbt.setInteger("posX", door.x);
            INbt.setInteger("posY", door.y);
            INbt.setInteger("posZ", door.z);

            INbtList.appendTag(INbt);
        }
        return INbtList;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        innName = INbt.getString("InnName");
        doors = getInnDoors(INbt.getTagList("InnDoors", 10));
    }

    private HashMap<String, InnDoorData> getInnDoors(INbtList tagList) {
        HashMap<String, InnDoorData> list = new HashMap<String, InnDoorData>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            INbt INbt = tagList.getCompoundTagAt(i);
            String name = INbt.getString("Name");

            InnDoorData door = new InnDoorData();
            door.x = INbt.getInteger("posX");
            door.y = INbt.getInteger("posY");
            door.z = INbt.getInteger("posZ");
            list.put(name, door);
        }
        return list;
    }

    @Override
    public void interact(IPlayer player) {
        npc.say(player, npc.advanced.getInteractLine());
        if (doors.isEmpty()) {
            player.addChatMessage(new ITextComponent("No Rooms available"));
        }
    }

}
