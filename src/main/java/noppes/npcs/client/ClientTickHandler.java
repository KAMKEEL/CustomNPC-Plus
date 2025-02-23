package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.CheckPlayerValue;
import kamkeel.npcs.network.packets.player.InputDevicePacket;
import kamkeel.npcs.network.packets.player.ScreenSizePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.ScriptSoundController;
import noppes.npcs.client.gui.hud.ClientHudManager;
import noppes.npcs.client.gui.hud.CompassHudComponent;
import noppes.npcs.client.gui.hud.EnumHudComponent;
import noppes.npcs.client.gui.hud.HudComponent;
import noppes.npcs.client.gui.player.inventory.GuiCNPCInventory;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.constants.MarkType;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tconstruct.client.tabs.InventoryTabCustomNpc;

import java.util.ArrayList;

import static noppes.npcs.client.ClientEventHandler.renderCNPCPlayer;

public class ClientTickHandler{
	private World prevWorld;
	private int prevWidth = 0;
	private int prevHeight = 0;
	private boolean otherContainer = false;
	private int buttonPressed = -1;
	private long buttonTime = 0L;
	private final int[] ignoreKeys = new int[]{157, 29, 54, 42, 184, 56, 220, 219};

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientTick(TickEvent.ClientTickEvent event){
		Minecraft mc = Minecraft.getMinecraft();
		if ((this.prevWorld == null || mc.theWorld == null) && this.prevWorld != mc.theWorld) {
            if (mc.theWorld == null) {
                ClientCacheHandler.clearCache();
            }
			this.prevWorld = mc.theWorld;
		}
		if(event.phase == Phase.END) {
			if (mc.thePlayer != null && mc.theWorld != null && !mc.isGamePaused() && ClientEventHandler.hasOverlays(mc.thePlayer)) {
				renderCNPCPlayer.itemRenderer.updateEquippedItem();
			}
			return;
		}
		if(mc.thePlayer != null && mc.thePlayer.openContainer instanceof ContainerPlayer){
			if(otherContainer){
                PacketClient.sendClient(new CheckPlayerValue(CheckPlayerValue.Type.CheckQuestCompletion));
				otherContainer = false;
			}
		}
		else
			otherContainer = true;
		CustomNpcs.ticks++;
		RenderNPCInterface.LastTextureTick++;
		if(prevWorld != mc.theWorld){
			prevWorld = mc.theWorld;
			MusicController.Instance.stopAllSounds();
		} else if (MusicController.Instance.isPlaying() && MusicController.Instance.getEntity() != null) {
            Entity entity = MusicController.Instance.getEntity();
            if (MusicController.Instance.getOffRange() > 0 &&
                (Minecraft.getMinecraft().thePlayer == null ||
                    Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity) > MusicController.Instance.getOffRange() ||
                    entity.dimension != Minecraft.getMinecraft().thePlayer.dimension)) {
                MusicController.Instance.stopMusic();
            }
        }
        MusicController.Instance.onUpdate();
		ScriptSoundController.Instance.onUpdate();
		if(Minecraft.getMinecraft().thePlayer!=null && (prevWidth!=mc.displayWidth || prevHeight!=mc.displayHeight)){
			prevWidth = mc.displayWidth;
			prevHeight = mc.displayHeight;
            PacketClient.sendClient(new ScreenSizePacket(mc.displayWidth,mc.displayHeight));
		}

        if(mc.theWorld == null)
            return;

        if(mc.theWorld.getTotalWorldTime() % 20 == 0) { // Update every second
            updateCompassMarks();
        }
	}

	@SubscribeEvent
	public void onMouse(InputEvent.MouseInputEvent event){
		if(Mouse.getEventButton() == -1 && Mouse.getEventDWheel() == 0)
			return;

		InputDevicePacket.sendMouse(Mouse.getEventButton(),Mouse.getEventDWheel(),Mouse.isButtonDown(Mouse.getEventButton()));
	}

	@SubscribeEvent
	public void onKey(InputEvent.KeyInputEvent event){
		if(ClientProxy.NPCButton.isPressed()){
			Minecraft mc = Minecraft.getMinecraft();
			if(mc.currentScreen == null){
                InventoryTabCustomNpc.tabHelper();
            }
			else if(mc.currentScreen instanceof GuiCNPCInventory)
				mc.setIngameFocus();
		}

		if(!Keyboard.isRepeatEvent()) {
			int key = Keyboard.getEventKey();
			boolean keyDown = Keyboard.isKeyDown(key);

            InputDevicePacket.sendKeyboard(key, keyDown);
		}
	}

	private boolean isIgnoredKey(int key) {
		int[] var2 = this.ignoreKeys;
		int var3 = var2.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			int i = var2[var4];
			if(i == key) {
				return true;
			}
		}

		return false;
	}

    private final int SCAN_RANGE = 128;
    private void updateCompassMarks() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        // Update compass
        HudComponent compass = ClientHudManager.getInstance()
            .getHudComponents().get(EnumHudComponent.QuestCompass);

        if(!compass.enabled || mc.thePlayer == null)
            return;

        ArrayList<CompassHudComponent.MarkTargetEntry> marks = new ArrayList<>();

        // Scan entities in loaded chunks
        for(Object entity : mc.theWorld.loadedEntityList) {
            if(entity instanceof EntityNPCInterface) {
                EntityNPCInterface npc = (EntityNPCInterface) entity;

                if(npc.dimension != player.dimension)
                    continue;

                // Check distance
                if(player.getDistanceToEntity(npc) > SCAN_RANGE)
                    continue;

                // Get marks
                MarkData markData = MarkData.get(npc);
                for(MarkData.Mark mark : markData.marks) {
                    if(mark.getType() != MarkType.NONE &&
                        mark.availability.isAvailable(player)) {
                        marks.add(new CompassHudComponent.MarkTargetEntry(
                            (int)npc.posX,
                            (int)npc.posZ,
                            mark.getType(),
                            mark.color
                        ));
                        break; // Only show first valid mark per NPC
                    }
                }
            }
        }

        ((CompassHudComponent) compass).updateMarkTargets(marks);
    }
}
