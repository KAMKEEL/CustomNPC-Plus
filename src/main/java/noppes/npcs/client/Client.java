package noppes.npcs.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.constants.EnumPacketServer;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import noppes.npcs.controllers.data.SkinOverlayData;

public class Client {
	public static HashMap<Integer, OverlayCustom> customOverlays = new HashMap<>();

	public static HashMap<UUID, HashMap<Integer, SkinOverlayData>> skinOverlays = new HashMap<>();
	public static HashMap<UUID, Long> entitySkinOverlayTicks = new HashMap<>();

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
