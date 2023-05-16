package noppes.npcs.client;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import noppes.npcs.AnimationData;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.customitem.ImageData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.util.CustomNPCsScheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Client {
	public static OverlayQuestTracking questTrackingOverlay = null;
	public static HashMap<Integer, OverlayCustom> customOverlays = new HashMap<>();
	public static HashMap<UUID, HashMap<Integer, SkinOverlay>> skinOverlays = new HashMap<>();
	public static HashMap<UUID, AnimationData> playerAnimations = new HashMap<>();
	private static HashMap<String, ImageData> imageDataCache = new HashMap<>();

	public static void sendData(final EnumPacketServer enu, final Object... obs) {
		CustomNPCsScheduler.runTack(() -> {
			ByteBuf buffer = Unpooled.buffer();
			try {
				if(!Server.fillBuffer(buffer, enu, obs))
					return;
				CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static ImageData getImageData(String directory) {
		if (!imageDataCache.containsKey(directory)) {
			ImageData downloadData = new ImageData(directory);
			imageDataCache.put(directory, downloadData);
		}
		return imageDataCache.get(directory);
	}
}
