package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcs.client.renderer.lightning.LightningBolt;
import kamkeel.npcs.controllers.data.energycharge.EnergyChargePreviewManager;
import kamkeel.npcs.controllers.data.telegraph.TelegraphManager;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.data.RequestProperSpawnData;
import kamkeel.npcs.network.packets.player.CheckPlayerValue;
import kamkeel.npcs.network.packets.player.ScreenSizePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.world.World;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.ScriptSoundController;
import noppes.npcs.client.gui.hud.ClientHudManager;
import noppes.npcs.client.gui.hud.CompassHudComponent;
import noppes.npcs.client.gui.hud.EnumHudComponent;
import noppes.npcs.client.gui.hud.HudComponent;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.MarkType;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleMount;

import java.util.ArrayList;

import static noppes.npcs.client.ClientEventHandler.renderCNPCPlayer;

public class ClientTickHandler {
    private World prevWorld;
    private int prevWidth = 0;
    private int prevHeight = 0;
    private boolean otherContainer = false;
    private boolean wasMovementSuppressed = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if ((this.prevWorld == null || mc.theWorld == null) && this.prevWorld != mc.theWorld) {
            if (mc.theWorld == null) {
                ClientCacheHandler.clearCache();
                ClientAbilityState.reset();
                RequestProperSpawnData.clear();
                if (EnergyChargePreviewManager.ClientInstance != null) {
                    EnergyChargePreviewManager.ClientInstance.clear();
                }
            }
            this.prevWorld = mc.theWorld;
        }
        if (event.phase == Phase.START) {
            EntityPlayer player = mc.thePlayer;
            if (player != null) {
                boolean shouldSuppress = ClientAbilityState.shouldSuppressMovementInput();
                boolean suppressInput = mc.currentScreen == null && shouldSuppress;
                if (!suppressInput && wasMovementSuppressed) {
                    syncMovementKeyStates(mc);
                }
                wasMovementSuppressed = suppressInput;

                // Suppress player keyboard input during ability-controlled phases.
                // Only suppress key binds when no GUI screen is open (screens already capture input).
                if (suppressInput) {
                    // Unpress movement keybinds at the source BEFORE updatePlayerMoveState() reads them.
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
                }

                // Zero movement input regardless of GUI state — the ability lock must
                // hold the player in place even when chat or other screens are open.
                if (shouldSuppress) {
                    mc.thePlayer.movementInput.moveForward = 0;
                    mc.thePlayer.movementInput.moveStrafe = 0;
                    mc.thePlayer.movementInput.jump = false;
                    mc.thePlayer.movementInput.sneak = false;
                }
                if (ClientAbilityState.shouldLockRotation()) {
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
                // Motion zeroing runs regardless of GUI state so the player doesn't fall
                // when opening chat or other screens during an ability lock.
                if (ClientAbilityState.shouldSuppressMovementInput()) {
                    mc.thePlayer.movementInput.moveForward = 0;
                    mc.thePlayer.movementInput.moveStrafe = 0;
                    mc.thePlayer.movementInput.jump = false;
                    mc.thePlayer.movementInput.sneak = false;

                    // Zero all motion — position lock holds the player in place (server is authoritative)
                    if (!ClientAbilityState.hasAbilityMovement) {
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionZ = 0;
                    }

                    // Zero vertical motion: grounded players can't jump, flying players are frozen in air
                    if ((ClientAbilityState.movementLocked || ClientAbilityState.positionLocked)
                        && !ClientAbilityState.hasAbilityMovement) {
                        if (ClientAbilityState.wasFlyingAtLock) {
                            mc.thePlayer.motionY = 0;
                        } else {
                            mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY, 0);
                        }
                    }
                }

                if (ClientAbilityState.shouldLockRotation()) {
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

            // Tick telegraph manager AFTER entity updates so telegraph positions
            // match entity interpolation (Phase.END runs after world tick)
            if (TelegraphManager.ClientInstance != null) {
                TelegraphManager.ClientInstance.tick(mc.theWorld);
            }

            // Tick packet-driven energy charge previews
            if (EnergyChargePreviewManager.ClientInstance != null) {
                EnergyChargePreviewManager.ClientInstance.tick(mc.theWorld);
            }

            // Update lightning bolts for ability effects
            LightningBolt.updateAll();

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

    /**
     * Enforce ability lock motion constraints right before entity update.
     * The Phase.END zeroing in onClientTick can be overwritten by other mods'
     * ClientTickEvent Phase.START handlers (e.g. flight mods re-applying gravity).
     * PlayerTickEvent fires after all ClientTickEvent handlers but before
     * player.onUpdate(), making this the definitive point to enforce motion locks.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != Phase.START || event.side != Side.CLIENT)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null)
            return;

        if (!ClientAbilityState.shouldSuppressMovementInput())
            return;

        if (!ClientAbilityState.hasAbilityMovement) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        if ((ClientAbilityState.movementLocked || ClientAbilityState.positionLocked)
            && !ClientAbilityState.hasAbilityMovement) {
            if (ClientAbilityState.wasFlyingAtLock) {
                mc.thePlayer.motionY = 0;
            } else {
                mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY, 0);
            }
        }
    }

    private void syncMovementKeyStates(Minecraft mc) {
        syncKeyBindingState(mc.gameSettings.keyBindForward);
        syncKeyBindingState(mc.gameSettings.keyBindBack);
        syncKeyBindingState(mc.gameSettings.keyBindLeft);
        syncKeyBindingState(mc.gameSettings.keyBindRight);
        syncKeyBindingState(mc.gameSettings.keyBindJump);
        syncKeyBindingState(mc.gameSettings.keyBindSneak);
        syncKeyBindingState(mc.gameSettings.keyBindSprint);
    }

    private void syncKeyBindingState(KeyBinding keyBinding) {
        KeyBinding.setKeyBindState(keyBinding.getKeyCode(), KeyPressHandler.isKeyBindDown(keyBinding));
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
