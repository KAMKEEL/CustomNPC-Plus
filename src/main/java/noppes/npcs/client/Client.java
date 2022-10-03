package noppes.npcs.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.constants.EnumPacketServer;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import noppes.npcs.controllers.data.SkinOverlay;

public class Client {
	public static OverlayQuestTracking questTrackingOverlay = null;
	public static HashMap<Integer, OverlayCustom> customOverlays = new HashMap<>();
	public static HashMap<UUID, HashMap<Integer, SkinOverlay>> skinOverlays = new HashMap<>();

	public static void sendData(EnumPacketServer enu, Object... obs) {
		ByteBuf buffer = Unpooled.buffer();
		try {
			if(!Server.fillBuffer(buffer, enu, obs))
				return;
			CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
