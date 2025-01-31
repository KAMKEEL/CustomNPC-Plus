package kamkeel.npcs.network.packets.data.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.fx.CustomFX;
import noppes.npcs.scripted.ScriptParticle;

import java.io.IOException;

public final class ScriptedParticlePacket extends AbstractPacket {
    public static final String packetName = "Data|ScriptedParticle";

    private NBTTagCompound compound;

    public ScriptedParticlePacket() {}

    public ScriptedParticlePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SCRIPTED_PARTICLE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        spawnScriptedParticle(player, in);
    }

    @SideOnly(Side.CLIENT)
    public static void spawnScriptedParticle(EntityPlayer player, ByteBuf buffer){
        Minecraft minecraft =  Minecraft.getMinecraft();

        NBTTagCompound compound;
        ScriptParticle particle;
        try {
            compound = ByteBufUtils.readNBT(buffer);
            particle = ScriptParticle.fromNBT(compound);
        } catch (IOException ignored) {
            return;
        }

        World worldObj = player.worldObj;
        if (worldObj == null) {
            return;
        }

        Entity entity = null;
        if (compound.hasKey("EntityID")) {
            entity = worldObj.getEntityByID(compound.getInteger("EntityID"));
            if (entity != null)
                worldObj = entity.worldObj;
            else return;
        }

        CustomFX fx = CustomFX.fromScriptedParticle(particle, worldObj, entity);

        for(int i = 0; i < particle.amount; i++){
            minecraft.effectRenderer.addEffect(fx);
        }
    }
}
