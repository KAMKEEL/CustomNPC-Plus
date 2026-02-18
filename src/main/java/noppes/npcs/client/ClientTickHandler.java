package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import kamkeel.npcs.client.renderer.lightning.LightningBolt;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.telegraph.TelegraphManager;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.CheckPlayerValue;
import kamkeel.npcs.network.packets.player.InputDevicePacket;
import kamkeel.npcs.network.packets.player.ScreenSizePacket;
import kamkeel.npcs.network.packets.player.SpecialKeyStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
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
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.MarkType;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleMount;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tconstruct.client.tabs.InventoryTabCustomNpc;

import java.util.ArrayList;

import static noppes.npcs.client.ClientEventHandler.renderCNPCPlayer;

public class ClientTickHandler {
    private World prevWorld;
    private int prevWidth = 0;
    private int prevHeight = 0;
    private boolean otherContainer = false;
    private int buttonPressed = -1;
    private long buttonTime = 0L;
    private final int[] ignoreKeys = new int[]{157, 29, 54, 42, 184, 56, 220, 219};
    private boolean lastSpecialKeyDown = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if ((this.prevWorld == null || mc.theWorld == null) && this.prevWorld != mc.theWorld) {
            if (mc.theWorld == null) {
                ClientCacheHandler.clearCache();
                ClientAbilityState.reset();
            }
            this.prevWorld = mc.theWorld;
        }
        if (event.phase == Phase.START) {
            EntityPlayer player = mc.thePlayer;
            if (player != null) {
                // Only process special key when no GUI screen is open
                boolean specialKeyDown = mc.currentScreen == null
                    && ClientProxy.SpecialKey != null && Keyboard.isKeyDown(ClientProxy.SpecialKey.getKeyCode());
                if (specialKeyDown != lastSpecialKeyDown) {
                    PlayerData data = CustomNpcs.proxy.getPlayerData(player);
                    if (data != null) {
                        data.setSpecialKeyDown(specialKeyDown);
                    }
                    SpecialKeyStatePacket.send(specialKeyDown);
                    lastSpecialKeyDown = specialKeyDown;
                }

                // Suppress player input during ability-controlled phases.
                // Only suppress when no GUI screen is open (screens already capture input).
                if (mc.currentScreen == null && ClientAbilityState.shouldSuppressMovementInput()) {
                    // Unpress movement keybinds at the source BEFORE updatePlayerMoveState() reads them.
                    // This is more robust than just zeroing movementInput fields, because
                    // updatePlayerMoveState() reads directly from keybind pressed state.
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);

                    // Also zero the movementInput values directly as a safety net
                    mc.thePlayer.movementInput.moveForward = 0;
                    mc.thePlayer.movementInput.moveStrafe = 0;
                    mc.thePlayer.movementInput.jump = false;
                    mc.thePlayer.movementInput.sneak = false;
                }
                if (mc.currentScreen == null && ClientAbilityState.shouldLockRotation()) {
                    mc.thePlayer.rotationYaw = ClientAbilityState.lockedYaw;
                    mc.thePlayer.rotationPitch = ClientAbilityState.lockedPitch;
                    mc.thePlayer.prevRotationYaw = ClientAbilityState.lockedYaw;
                    mc.thePlayer.prevRotationPitch = ClientAbilityState.lockedPitch;
                }

                if (player.ridingEntity instanceof EntityNPCInterface) {
                    EntityNPCInterface mount = (EntityNPCInterface) player.ridingEntity;
                    if (mount.advanced.role == EnumRoleType.Mount && mount.roleInterface instanceof RoleMount) {
                        RoleMount role = (RoleMount) mount.roleInterface;
                        if (!role.isSprintAllowed() && player.isSprinting()) {
                            player.setSprinting(false);
                        }
                    }
                }
            }
        }
        if (event.phase == Phase.END) {
            if (mc.thePlayer != null) {
                // Re-enforce ability locks AFTER entity update.
                // EntityPlayerSP.onLivingUpdate() calls updatePlayerMoveState() which
                // overwrites Phase.START suppression with actual keyboard state, causing
                // client-predicted movement that fights the server lock. Undo it here.
                // Skip when a GUI screen is open (screens already capture all input).
                if (mc.currentScreen == null && ClientAbilityState.shouldSuppressMovementInput()) {
                    mc.thePlayer.movementInput.moveForward = 0;
                    mc.thePlayer.movementInput.moveStrafe = 0;
                    mc.thePlayer.movementInput.jump = false;
                    mc.thePlayer.movementInput.sneak = false;

                    // Zero horizontal motion (unless ability provides its own movement)
                    if (!ClientAbilityState.hasAbilityMovement) {
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionZ = 0;
                    }

                    // Prevent jumping when locked (allow gravity only if not flying)
                    if ((ClientAbilityState.movementLocked || ClientAbilityState.positionLocked)
                            && !ClientAbilityState.hasAbilityMovement
                            && !AbilityController.Instance.isPlayerFlying(mc.thePlayer)) {
                        mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY, 0);
                    }
                }

                if (mc.currentScreen == null && ClientAbilityState.shouldLockRotation()) {
                    mc.thePlayer.rotationYaw = ClientAbilityState.lockedYaw;
                    mc.thePlayer.rotationPitch = ClientAbilityState.lockedPitch;
                    mc.thePlayer.prevRotationYaw = ClientAbilityState.lockedYaw;
                    mc.thePlayer.prevRotationPitch = ClientAbilityState.lockedPitch;
                    mc.thePlayer.rotationYawHead = ClientAbilityState.lockedYaw;
                }

                if (mc.theWorld != null && !mc.isGamePaused() && ClientEventHandler.hasOverlays(mc.thePlayer)) {
                    renderCNPCPlayer.itemRenderer.updateEquippedItem();
                }
            }
            return;
        }
        if (mc.thePlayer != null && mc.thePlayer.openContainer instanceof ContainerPlayer) {
            if (otherContainer) {
                PacketClient.sendClient(new CheckPlayerValue(CheckPlayerValue.Type.CheckQuestCompletion));
                otherContainer = false;
            }
        } else
            otherContainer = true;
        CustomNpcs.ticks++;
        RenderNPCInterface.LastTextureTick++;

        if (MusicController.Instance.isPlaying() && MusicController.Instance.getEntity() != null) {
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

        // Tick telegraph manager for ability warnings
        if (TelegraphManager.ClientInstance != null) {
            TelegraphManager.ClientInstance.tick(mc.theWorld);
        }

        // Update lightning bolts for ability effects
        LightningBolt.updateAll();
        if (Minecraft.getMinecraft().thePlayer != null && (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight)) {
            prevWidth = mc.displayWidth;
            prevHeight = mc.displayHeight;
            PacketClient.sendClient(new ScreenSizePacket(mc.displayWidth, mc.displayHeight));
        }

        if (mc.theWorld == null)
            return;

        if (mc.theWorld.getTotalWorldTime() % 20 == 0) { // Update every second
            updateCompassMarks();
        }
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseInputEvent event) {
        if (Mouse.getEventButton() == -1 && Mouse.getEventDWheel() == 0)
            return;

        InputDevicePacket.sendMouse(Mouse.getEventButton(), Mouse.getEventDWheel(), Mouse.isButtonDown(Mouse.getEventButton()));
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (ClientProxy.NPCButton.isPressed()) {
            if (mc.currentScreen == null) {
                InventoryTabCustomNpc.tabHelper();
            } else if (mc.currentScreen instanceof GuiCNPCInventory)
                mc.setIngameFocus();
        }

        if (!Keyboard.isRepeatEvent()) {
            int key = Keyboard.getEventKey();
            boolean keyDown = Keyboard.isKeyDown(key);

            // Block movement keys during ability lock (only when no GUI is open and world exists).
            // Non-movement keys (ESC, chat, inventory, etc.) always pass through.
            if (mc.theWorld != null && mc.currentScreen == null && keyDown
                    && ClientAbilityState.shouldSuppressMovementInput() && isMovementKey(key)) {
                // Unpress the keybind so vanilla doesn't process it
                KeyBinding.setKeyBindState(key, false);
                return;
            }

            InputDevicePacket.sendKeyboard(key, keyDown);
        }
    }

    /**
     * Check if a key code corresponds to a movement keybind (WASD, jump, sneak, sprint).
     */
    private boolean isMovementKey(int keyCode) {
        Minecraft mc = Minecraft.getMinecraft();
        return keyCode == mc.gameSettings.keyBindForward.getKeyCode()
            || keyCode == mc.gameSettings.keyBindBack.getKeyCode()
            || keyCode == mc.gameSettings.keyBindLeft.getKeyCode()
            || keyCode == mc.gameSettings.keyBindRight.getKeyCode()
            || keyCode == mc.gameSettings.keyBindJump.getKeyCode()
            || keyCode == mc.gameSettings.keyBindSneak.getKeyCode()
            || keyCode == mc.gameSettings.keyBindSprint.getKeyCode();
    }

    private boolean isIgnoredKey(int key) {
        int[] var2 = this.ignoreKeys;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            if (i == key) {
                return true;
            }
        }

        return false;
    }

    private final int SCAN_RANGE = 128;

    private void updateCompassMarks() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null || mc.theWorld == null)
            return;

        if (ClientHudManager.getInstance() == null || ClientHudManager.getInstance().getHudComponents() == null)
            return;

        // Update compass
        HudComponent compass = ClientHudManager.getInstance()
            .getHudComponents().get(EnumHudComponent.QuestCompass);

        if (!(compass instanceof CompassHudComponent))
            return;

        if (!compass.enabled)
            return;

        ArrayList<CompassHudComponent.MarkTargetEntry> marks = new ArrayList<>();

        // Scan entities in loaded chunks
        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityNPCInterface) {
                EntityNPCInterface npc = (EntityNPCInterface) entity;

                if (npc.dimension != player.dimension)
                    continue;

                // Check distance
                if (player.getDistanceToEntity(npc) > SCAN_RANGE)
                    continue;

                // Get marks
                MarkData markData = MarkData.get(npc);
                for (MarkData.Mark mark : markData.marks) {
                    if (mark.getType() != MarkType.NONE &&
                        mark.availability.isAvailable(player)) {
                        marks.add(new CompassHudComponent.MarkTargetEntry(
                            npc.posX,
                            npc.posZ,
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
