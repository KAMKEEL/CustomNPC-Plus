package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.EventHooks;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class InputDevicePacket extends AbstractPacket {
    public static final String packetName = "Player|InputDevice";

    private Type type;

    private int key, scroll;
    private boolean isKeyPressed, ctrlPressed, shiftPressed, altPressed, metaPressed;
    private String keysDownList;

    public InputDevicePacket() {

    }

    private InputDevicePacket(Type type, int key, boolean isDown) {
        this.type = type;
        this.key = key;
        this.isKeyPressed = isDown;
    }

    public static void sendMouse(int mouseButton, int scroll, boolean isDown) {
        InputDevicePacket packet = new InputDevicePacket(Type.MOUSE, mouseButton, isDown);
        packet.scroll = scroll;
        packet.commonDataGrabber();

        PacketClient.sendClient(packet);
    }

    public static void sendKeyboard(int key, boolean isDown) {
        InputDevicePacket packet = new InputDevicePacket(Type.KEYBOARD, key, isDown);
        packet.commonDataGrabber();

        PacketClient.sendClient(packet);
    }

    private void commonDataGrabber() {
        ctrlPressed = Keyboard.isKeyDown(157) || Keyboard.isKeyDown(29);
        shiftPressed = Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42);
        altPressed = Keyboard.isKeyDown(184) || Keyboard.isKeyDown(56);
        metaPressed = Keyboard.isKeyDown(220) || Keyboard.isKeyDown(219);

        StringBuilder keysDownString = new StringBuilder();
        for (int i = 0; i < Keyboard.getKeyCount(); i++) {//Creates a comma separated string of the integer IDs of held keys
            if (Keyboard.isKeyDown(i)) {
                keysDownString.append(Integer.valueOf(i)).append(",");
            }
        }
        if (keysDownString.length() > 0) {//Removes last comma for later parsing
            keysDownString.deleteCharAt(keysDownString.length() - 1);
        }
        keysDownList = keysDownString.toString();
    }


    @Override
    public Enum getType() {
        return EnumPlayerPacket.InputDevice;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());

        out.writeInt(key);
        out.writeBoolean(isKeyPressed);
        if (type == Type.MOUSE) {
            out.writeInt(scroll);
        }

        out.writeBoolean(ctrlPressed);
        out.writeBoolean(shiftPressed);
        out.writeBoolean(altPressed);
        out.writeBoolean(metaPressed);
        ByteBufUtils.writeString(out, keysDownList);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        Type incomingType = Type.values()[in.readInt()];
        boolean isMouse = incomingType == Type.MOUSE;

        int inputKey = in.readInt();
        boolean isKeyDown = in.readBoolean();
        int scroll = isMouse ? in.readInt() : 0;
        boolean ctrl = in.readBoolean();
        boolean shift = in.readBoolean();
        boolean alt = in.readBoolean();
        boolean meta = in.readBoolean();
        int[] pressedKeys = getPressedArray(ByteBufUtils.readString(in));

        if (isMouse)
            EventHooks.onPlayerMouseClicked(playerMP, inputKey, scroll, isKeyDown, ctrl, shift, alt, meta, pressedKeys);
        else
            EventHooks.onPlayerKeyPressed(playerMP, inputKey, ctrl, shift, alt, meta, isKeyDown, pressedKeys);
    }

    private static int[] getPressedArray(String pressedKeys) {
        if (pressedKeys == null)
            return new int[0];

        String[] split = pressedKeys.split(",");
        int[] keysDown;

        if (pressedKeys.length() > 0) {
            keysDown = new int[split.length];
            try {
                for (int i = 0; i < split.length; i++) {
                    keysDown[i] = Integer.parseInt(split[i]);
                }
            } catch (NumberFormatException ignored) {
            }
        } else {
            keysDown = new int[0];
        }
        return keysDown;
    }

    private enum Type {
        MOUSE,
        KEYBOARD
    }
}
