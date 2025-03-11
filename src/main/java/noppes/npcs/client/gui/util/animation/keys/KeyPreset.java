package noppes.npcs.client.gui.util.animation.keys;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
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

    public int keyCode = -1;
    public int pressTime;
    public boolean isDown;

    public boolean hasCtrl, hasAlt, hasShift;

    public String name, description;
    public Consumer<Integer> task;
    public KeyState defaultState = new KeyState();

    public KeyPreset(String name) {
        this.name = name;
    }

    public KeyPreset setDefaultState(int keyCode, boolean hasCtrl, boolean hasAlt, boolean hasShift) {
        defaultState.setState(keyCode, hasCtrl, hasAlt, hasShift);
        return this;
    }

    public KeyPreset setDescription(String description) {
        this.description = description;
        return this;
    }

    public KeyPreset setTask(Consumer<Integer> task) {
        this.task = task;
        return this;
    }

    public void tick() {
        if (keyCode == -1)
            return;

        boolean isDown = isMouseKey() ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
        isDown = isDown && (hasCtrl ? isCtrlKeyDown() : true) && (hasAlt ? isAltKeyDown() : true && (hasShift ? isShiftKeyDown() : true));

        setDown(isDown);
    }

    public void setDown(boolean down) {
        if (down && !isDown)
            onAction(PRESS, PRESS_RELEASE);

        if (!down && isDown) {
            if (pressTime <= 5)
                onAction(SINGLE_PRESS);

            onAction(RELEASE, PRESS_RELEASE);
            pressTime = 0;
        }

        if (isDown || down) //to fire in both press & release
            onAction(HOLD);

        if (isDown)
            pressTime++;

        this.isDown = down;
    }

    public KeyPreset onAction(int... pressTypes) {
        for (int pressType : pressTypes)
            this.task.accept(pressType);
        return this;
    }

    public boolean isMouseKey() {
        return keyCode < -1;
    }

    public String getKeyName() {
        return getKeyName(this);
    }

    public void clear() {
        this.keyCode = -1;
        hasCtrl = hasAlt = hasShift = false;
    }

    //call on the last key added, to load saved presets
    public KeyPreset markDone(KeyPresetManager manager) {
        manager.load();
        return this;
    }

    public void writeToNbt(NBTTagCompound c) {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("keyCode", keyCode);
        compound.setBoolean("hasCtrl", hasCtrl);
        compound.setBoolean("hasShift", hasShift);
        compound.setBoolean("hasAlt", hasAlt);
        c.setTag(name, compound);
    }

    public void readFromNbt(NBTTagCompound c) {
        NBTTagCompound compound = c.getCompoundTag(name);

        this.keyCode = compound.getInteger("keyCode");
        this.hasCtrl = compound.getBoolean("hasCtrl");
        this.hasShift = compound.getBoolean("hasShift");
        this.hasAlt = compound.getBoolean("hasAlt");
    }

    public boolean equals(Object preset) {
        if (preset == this)
            return true;

        if (preset instanceof KeyPreset) {
            KeyPreset key = (KeyPreset) preset;
            return key.keyCode == keyCode && key.hasCtrl == hasCtrl && key.hasAlt == hasAlt && key.hasShift == hasShift;
        }

        return false;
    }


    public static class KeyState {
        public int keyCode = -1;
        public boolean hasCtrl, hasAlt, hasShift;

        public void setState(int keyCode, boolean hasCtrl, boolean hasAlt, boolean hasShift) {
            this.keyCode = keyCode;
            this.hasCtrl = hasCtrl;
            this.hasAlt = hasAlt;
            this.hasShift = hasShift;
        }

        public void saveState(KeyPreset key) {
            keyCode = key.keyCode;
            hasCtrl = key.hasCtrl;
            hasShift = key.hasShift;
            hasAlt = key.hasAlt;
        }

        public void loadState(KeyPreset key) {
            key.keyCode = keyCode;
            key.hasCtrl = hasCtrl;
            key.hasShift = hasShift;
            key.hasAlt = hasAlt;
        }

        public boolean hasState() {
            return keyCode != -1;
        }
    }

    public static boolean isCtrlKeyDown() {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    public static boolean isNotCtrlShiftAlt(int key) {
        return key != 219 && key != 220 && key != 29 && key != 157 && key != 42 && key != 54 && key != 56 && key != 184;
    }

    public static String getKeyName(KeyPreset key) {
        int code = key.keyCode;
        String name = "";

        if (code == -100)
            name = "Left Mouse";
        else if (code == -99)
            name = "Right Mouse";
        else if (code == -98)
            name = "Middle Mouse";
        else {
            name = code == -1 ? "" : GameSettings.getKeyDisplayString(code);
            if (name.contains("Button"))
                name = name.replace("Button", "Mouse");
        }

        return (key.hasCtrl ? "CTRL " : "") + (key.hasAlt ? "ALT " : "") + (key.hasShift ? "SHIFT " : "") + name;
    }
}
