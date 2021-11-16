package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.constants.EnumPlayerPacket;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClientTickHandler{
	private World prevWorld;
	private boolean otherContainer = false;
	private int buttonPressed = -1;
	private long buttonTime = 0L;
	private final int[] ignoreKeys = new int[]{157, 29, 54, 42, 184, 56, 220, 219};
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase == Phase.END)
			return;
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.thePlayer != null && mc.thePlayer.openContainer instanceof ContainerPlayer){
			if(otherContainer){
		    	NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion);
				otherContainer = false;
			}
		}
		else
			otherContainer = true;
		CustomNpcs.ticks++;
		RenderNPCInterface.LastTextureTick++;
		if(prevWorld != mc.theWorld){
			prevWorld = mc.theWorld;
			MusicController.Instance.stopMusic();
		}
	}

	@SubscribeEvent
	public void onMouse(InputEvent.MouseInputEvent event){
		if(Mouse.getEventButton() == -1 && Mouse.getDWheel() == 0)
			return;

		NoppesUtilPlayer.sendData(EnumPlayerPacket.MouseClicked, new Object[]{Mouse.getEventButton(),Mouse.getEventDWheel(),Mouse.isButtonDown(Mouse.getEventButton())});
	}

	@SubscribeEvent
	public void onKey(InputEvent.KeyInputEvent event){
		if(ClientProxy.QuestLog.isPressed()){
			Minecraft mc = Minecraft.getMinecraft();
			if(mc.currentScreen == null)
				NoppesUtil.openGUI(mc.thePlayer, new GuiQuestLog(mc.thePlayer));
			else if(mc.currentScreen instanceof GuiQuestLog)
				mc.setIngameFocus();
		}

		int key = Keyboard.getEventKey();
		long time = Keyboard.getEventNanoseconds();

		if(Keyboard.getEventKeyState()) {
			if(!this.isIgnoredKey(key)) {
				this.buttonTime = time;
			}
		}

		if(time-this.buttonTime == 0 || !Keyboard.getEventKeyState()) {
			boolean isCtrlPressed = Keyboard.isKeyDown(157) || Keyboard.isKeyDown(29);
			boolean isShiftPressed = Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42);
			boolean isAltPressed = Keyboard.isKeyDown(184) || Keyboard.isKeyDown(56);
			boolean isMetaPressed = Keyboard.isKeyDown(220) || Keyboard.isKeyDown(219);
			boolean keyDown = Keyboard.isKeyDown(key);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, new Object[]{Integer.valueOf(key), Boolean.valueOf(isCtrlPressed), Boolean.valueOf(isShiftPressed), Boolean.valueOf(isAltPressed), Boolean.valueOf(isMetaPressed), Boolean.valueOf(keyDown)});
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
}
