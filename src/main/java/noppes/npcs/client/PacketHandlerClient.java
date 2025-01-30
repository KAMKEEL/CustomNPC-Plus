package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.PacketHandlerServer;

import java.io.IOException;

public class PacketHandlerClient extends PacketHandlerServer {

	@SubscribeEvent
	public void onPacketData(FMLNetworkEvent.ClientCustomPacketEvent event) {
		// Need this here for testing and registry for now until
        // conversion is done
	}
}
