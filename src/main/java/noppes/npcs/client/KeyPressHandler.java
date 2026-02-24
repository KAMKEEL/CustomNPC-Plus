package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import kamkeel.npcs.network.packets.player.InputDevicePacket;
import kamkeel.npcs.network.packets.player.SpecialKeyStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.inventory.GuiCNPCInventory;
import noppes.npcs.controllers.data.PlayerData;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tconstruct.client.tabs.InventoryTabCustomNpc;

/**
 * Centralized input handler for CustomNPC+ keybindings.
 * Handles both keyboard and mouse input events so keybinds work
 * regardless of whether they're bound to a key or mouse button.
 * <p>
 * All keybind state queries (pressed, held, released) should go through
 * this class to avoid crashes with mouse-bound keys.
 * <p>
 * Mouse button states are tracked from MouseEvent (Forge bus) via
 * {@link #trackMouseButton}, because canceled MouseEvents cause Minecraft
 * to skip FML's MouseInputEvent entirely. This is the only reliable source.
 */
public class KeyPressHandler {

    // ── Mouse Button Tracking ────────────────────────────────────────────
    // Tracked from MouseEvent (Forge bus, via ClientEventHandler.onMouse)
    // which fires BEFORE cancellation can skip MouseInputEvent.
    private static final boolean[] mouseButtonsDown = new boolean[16];

    // ── Tracked Held States ──────────────────────────────────────────────
    private static boolean specialKeyHeld = false;
    private static boolean hudKeyHeld = false;

    // ── Event Handlers ──────────────────────────────────────────────────

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        updateHeldStates(mc);
        handleNPCButton(mc);

        if (!Keyboard.isRepeatEvent()) {
            int key = Keyboard.getEventKey();
            boolean keyDown = Keyboard.isKeyDown(key);

            // Block movement keys during ability lock (only when no GUI is open).
            if (mc.theWorld != null && mc.currentScreen == null && keyDown
                && ClientAbilityState.shouldSuppressMovementInput() && isMovementKey(key)) {
                KeyBinding.setKeyBindState(key, false);
                return;
            }

            InputDevicePacket.sendKeyboard(key, keyDown);
        }
    }

    @SubscribeEvent
    public void onMousePress(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Button state already tracked by trackMouseButton() from MouseEvent.
        // Just update held states and handle keybind actions.
        updateHeldStates(mc);
        handleNPCButton(mc);

        int button = Mouse.getEventButton();
        if (button == -1 && Mouse.getEventDWheel() == 0)
            return;

        InputDevicePacket.sendMouse(button, Mouse.getEventDWheel(),
            button >= 0 && Mouse.getEventButtonState());
    }

    /**
     * Per-tick safety net: sync tracked mouse button states with hardware
     * in case an event was missed (e.g. window focus loss).
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        // Correct any stuck mouse buttons from missed release events
        for (int i = 0; i < mouseButtonsDown.length; i++) {
            if (mouseButtonsDown[i] && i < Mouse.getButtonCount() && !Mouse.isButtonDown(i)) {
                mouseButtonsDown[i] = false;
            }
        }

        updateHeldStates(Minecraft.getMinecraft());
    }

    // ── Mouse Button Tracking (called from Forge MouseEvent) ─────────────

    /**
     * Called from {@code ClientEventHandler.onMouse(MouseEvent)} at the very
     * start, BEFORE any cancellation logic. This is the primary source of
     * mouse button state because canceled MouseEvents cause Minecraft to
     * skip {@code FMLCommonHandler.fireMouseInput()} entirely, so
     * MouseInputEvent never fires for those events.
     */
    public static void trackMouseButton(int button, boolean down) {
        if (button >= 0 && button < mouseButtonsDown.length) {
            mouseButtonsDown[button] = down;
        }
        updateHeldStates(Minecraft.getMinecraft());
    }

    // ── State Updates ────────────────────────────────────────────────────

    /**
     * Refresh all tracked held states.
     * Called on every input event and every tick.
     */
    private static void updateHeldStates(Minecraft mc) {
        boolean noScreen = mc.currentScreen == null;

        // ── SpecialKey (edge-detected, sends packet on change) ──
        boolean specialDown = noScreen && isKeyBindDown(ClientProxy.SpecialKey);
        if (specialDown != specialKeyHeld) {
            specialKeyHeld = specialDown;
            if (mc.thePlayer != null) {
                PlayerData data = CustomNpcs.proxy.getPlayerData(mc.thePlayer);
                if (data != null) {
                    data.setSpecialKeyDown(specialDown);
                }
                SpecialKeyStatePacket.send(specialDown);
            }
        }

        // ── AbilityHudKey ──
        hudKeyHeld = noScreen && isKeyBindDown(ClientProxy.AbilityHudKey);
    }

    // ── Keybind Actions ─────────────────────────────────────────────────

    private static void handleNPCButton(Minecraft mc) {
        if (ClientProxy.NPCButton == null) return;
        if (ClientProxy.NPCButton.isPressed()) {
            if (mc.currentScreen == null) {
                InventoryTabCustomNpc.tabHelper();
            } else if (mc.currentScreen instanceof GuiCNPCInventory) {
                mc.setIngameFocus();
            }
        }
    }

    // ── Public State Queries ─────────────────────────────────────────────

    /**
     * Whether the SpecialKey is currently held down.
     */
    public static boolean isSpecialKeyHeld() {
        return specialKeyHeld;
    }

    /**
     * Whether the AbilityHudKey is currently held down.
     */
    public static boolean isHudKeyHeld() {
        return hudKeyHeld;
    }

    /**
     * Check if a KeyBinding is currently held down.
     * Works for both keyboard keys (positive codes) and mouse buttons (negative codes).
     * <p>
     * Mouse buttons use event-tracked state (from MouseEvent via trackMouseButton)
     * so the result is accurate even when called during keyboard event processing.
     */
    public static boolean isKeyBindDown(KeyBinding keyBinding) {
        if (keyBinding == null) return false;
        int keyCode = keyBinding.getKeyCode();
        if (keyCode == 0) return false;

        if (keyCode < 0) {
            int mouseButton = keyCode + 100;
            return mouseButton >= 0 && mouseButton < mouseButtonsDown.length
                && mouseButtonsDown[mouseButton];
        }

        return keyCode < Keyboard.getKeyCount() && Keyboard.isKeyDown(keyCode);
    }

    // ── Internal Helpers ────────────────────────────────────────────────

    private static boolean isMovementKey(int keyCode) {
        Minecraft mc = Minecraft.getMinecraft();
        return keyCode == mc.gameSettings.keyBindForward.getKeyCode()
            || keyCode == mc.gameSettings.keyBindBack.getKeyCode()
            || keyCode == mc.gameSettings.keyBindLeft.getKeyCode()
            || keyCode == mc.gameSettings.keyBindRight.getKeyCode()
            || keyCode == mc.gameSettings.keyBindJump.getKeyCode()
            || keyCode == mc.gameSettings.keyBindSneak.getKeyCode()
            || keyCode == mc.gameSettings.keyBindSprint.getKeyCode();
    }
}
