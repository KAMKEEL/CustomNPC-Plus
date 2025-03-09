package noppes.npcs.client.gui.util.animation.keys;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.function.Consumer;

public class KeyPreset {
    /**
     * @field SINGLE_PRESS => on releasing before 5 ticks press time
     * @field PRESS_RELEASE => on pressing and releasing
     */
    public static final int PRESS = 0, HOLD = 1, RELEASE = 2, SINGLE_PRESS = 3, PRESS_RELEASE = 4;

    public int keyCode, defaultKeyCode;
    public int pressTime;
    public boolean isDown;

    public boolean isMouseKey, hasCtrl, hasAlt, hasShift;

    public String name;
    public Consumer<Integer> task;

    public KeyPreset(String name, int keyCode, boolean isMouseKey) {
        this.name = name;
        this.keyCode = defaultKeyCode = keyCode;
        this.isMouseKey = isMouseKey;
    }

    public void setDown(boolean down) {
        if (down && !isDown) {
            onAction(PRESS);
            onAction(PRESS_RELEASE);
        }
        if (!down && isDown) {
            if (pressTime <= 5)
                onAction(SINGLE_PRESS);

            onAction(RELEASE);
            onAction(PRESS_RELEASE);
            pressTime = 0;
        }

        if (isDown || down) //to fire in both press & release
            onAction(KeyPreset.HOLD);

        if (isDown)
            pressTime++;

        this.isDown = down;
    }

    public KeyPreset onAction(int pressType) {
        this.task.accept(pressType);
        return this;
    }

    public KeyPreset setTask(Consumer<Integer> task) {
        this.task = task;
        return this;
    }

    public void tick() {
        boolean isDown = isMouseKey ? Mouse.isButtonDown(keyCode) : Keyboard.isKeyDown(keyCode);
        isDown = isDown && (hasCtrl ? isCtrlKeyDown() : true) && (hasShift ? isShiftKeyDown() : true) && (hasAlt ? isAltKeyDown() : true);

        setDown(isDown);
    }

    public void writeToNbt(NBTTagCompound c) {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("keyCode", keyCode);
        compound.setBoolean("isMouseKey", isMouseKey);
        compound.setBoolean("hasCtrl", hasCtrl);
        compound.setBoolean("hasShift", hasShift);
        compound.setBoolean("hasAlt", hasAlt);
        c.setTag(name, compound);
    }

    public void readFromNbt(NBTTagCompound c) {
        NBTTagCompound compound = c.getCompoundTag(name);

        this.keyCode = compound.getInteger("keyCode");
        this.isMouseKey = compound.getBoolean("isMouseKey");
        this.hasCtrl = compound.getBoolean("hasCtrl");
        this.hasShift = compound.getBoolean("hasShift");
        this.hasAlt = compound.getBoolean("hasAlt");
    }

    public static boolean isCtrlKeyDown() {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }
}
